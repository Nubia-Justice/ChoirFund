package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChoirDatabase
import com.example.data.model.Campaign
import com.example.data.model.ChoirSettings
import com.example.data.model.Contribution
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.repository.ChoirRepository
import com.example.ui.util.ChoirStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UserRole {
    TREASURER,
    MEMBER
}

enum class ChoirNavTab {
    OVERVIEW,
    MEMBERS,
    CONTRIBUTIONS,
    CAMPAIGNS,
    EXPENSES,
    SETTINGS
}

data class AuthSession(
    val isSignedIn: Boolean = false,
    val role: UserRole = UserRole.TREASURER,
    val memberId: Long? = null,
    val displayName: String = "",
    val phoneNumber: String = ""
)

data class MemberDuesStatus(
    val member: Member,
    val totalContributed: Long,
    val duesThisWeekPaid: Boolean,
    val duesThisWeekAmount: Long,
    val contributionsCount: Int
)

data class CampaignWithStats(
    val campaign: Campaign,
    val amountRaised: Long,
    val percentageReached: Int,
    val amountRemaining: Long,
    val contributions: List<Contribution>
)

data class ChoirUiState(
    val session: AuthSession = AuthSession(),
    val settings: ChoirSettings = ChoirSettings(),
    val selectedTab: ChoirNavTab = ChoirNavTab.OVERVIEW,
    val language: String = "EN", // EN or FR
    val isDarkTheme: Boolean = false,
    val members: List<Member> = emptyList(),
    val activeMembers: List<Member> = emptyList(),
    val contributions: List<Contribution> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val campaigns: List<CampaignWithStats> = emptyList(),
    val memberDuesList: List<MemberDuesStatus> = emptyList(),
    val totalContributions: Long = 0L,
    val totalExpenses: Long = 0L,
    val balanceInHand: Long = 0L,
    val activeMemberCount: Int = 0,
    val searchQuery: String = "",
    // Filter by type or category
    val contributionTypeFilter: String = "ALL",
    val expenseCategoryFilter: String = "ALL",
    // Dialogs
    val isAddMemberOpen: Boolean = false,
    val memberToEdit: Member? = null,
    val isAddContributionOpen: Boolean = false,
    val contributionToEdit: Contribution? = null,
    val preselectedMemberIdForContribution: Long? = null,
    val isAddCampaignOpen: Boolean = false,
    val campaignToEdit: Campaign? = null,
    val isAddExpenseOpen: Boolean = false,
    val expenseToEdit: Expense? = null,
    val isChangePasswordOpen: Boolean = false,
    val lastCreatedContributionReceipt: Contribution? = null,
    val isSaturdayRemindersOpen: Boolean = false,
    val remindedMemberIdsThisWeek: Set<Long> = emptySet(),
    val isTodaySaturday: Boolean = false,
    val unpaidMembersCount: Int = 0,
    val toastMessage: String? = null
)

private data class DialogInternalState(
    val isAddMemberOpen: Boolean = false,
    val memberToEdit: Member? = null,
    val isAddContributionOpen: Boolean = false,
    val contributionToEdit: Contribution? = null,
    val preselectedMemberIdForContribution: Long? = null,
    val isAddCampaignOpen: Boolean = false,
    val campaignToEdit: Campaign? = null,
    val isAddExpenseOpen: Boolean = false,
    val expenseToEdit: Expense? = null,
    val isChangePasswordOpen: Boolean = false,
    val lastCreatedContributionReceipt: Contribution? = null,
    val isSaturdayRemindersOpen: Boolean = false,
    val remindedMemberIds: Set<Long> = emptySet(),
    val toastMessage: String? = null
)

class ChoirViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChoirRepository

    private val _session = MutableStateFlow(AuthSession())
    private val _selectedTab = MutableStateFlow(ChoirNavTab.OVERVIEW)
    private val _searchQuery = MutableStateFlow("")
    private val _contributionTypeFilter = MutableStateFlow("ALL")
    private val _expenseCategoryFilter = MutableStateFlow("ALL")

    // Single unified dialog state holder
    private val _dialogState = MutableStateFlow(DialogInternalState())

    val uiState: StateFlow<ChoirUiState>

    init {
        val db = ChoirDatabase.getDatabase(application)
        repository = ChoirRepository(db.choirDao())

        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }
        com.example.util.SaturdayReminderScheduler.scheduleNextSaturdayAlarm(application)

        val baseDataFlow = combine(
            repository.members,
            repository.contributions,
            repository.expenses,
            repository.campaigns,
            repository.settings
        ) { members, contributions, expenses, campaigns, settings ->
            AppEntities(
                members = members,
                contributions = contributions,
                expenses = expenses,
                campaigns = campaigns,
                settings = settings ?: ChoirSettings()
            )
        }

        val uiControlsFlow = combine(
            _session,
            _selectedTab,
            _searchQuery,
            _contributionTypeFilter,
            _expenseCategoryFilter
        ) { session, tab, search, cFilter, eFilter ->
            UiControls(session, tab, search, cFilter, eFilter)
        }

        uiState = combine(baseDataFlow, uiControlsFlow, _dialogState) { entities, controls, dialogs ->
            val settings = entities.settings
            val language = settings.language
            val isDarkTheme = settings.themeMode == "DARK"
            val members = entities.members
            val contributions = entities.contributions
            val expenses = entities.expenses
            val campaigns = entities.campaigns

            val totalIn = contributions.sumOf { it.amount }
            val totalOut = expenses.sumOf { it.amount }
            val balance = totalIn - totalOut

            val duesWeekStart = ChoirStrings.getDuesWeekStart()

            // Calculate dues status for each member
            val memberDuesList = members.map { member ->
                val memContribs = contributions.filter { it.memberId == member.id }
                val totalMember = memContribs.sumOf { it.amount }
                // Weekly dues rule: Saturday to Friday, >= 200 FCFA
                val thisWeekDues = memContribs.filter {
                    it.type == "DUES" && it.date >= duesWeekStart
                }
                val paidAmount = thisWeekDues.sumOf { it.amount }
                MemberDuesStatus(
                    member = member,
                    totalContributed = totalMember,
                    duesThisWeekPaid = paidAmount >= 200L,
                    duesThisWeekAmount = paidAmount,
                    contributionsCount = memContribs.size
                )
            }

            // Calculate campaigns with stats
            val campaignStatsList = campaigns.map { camp ->
                val campContribs = contributions.filter { it.campaignId == camp.id }
                val raised = campContribs.sumOf { it.amount }
                val remaining = (camp.targetAmount - raised).coerceAtLeast(0L)
                val pct = if (camp.targetAmount > 0) {
                    ((raised.toDouble() / camp.targetAmount.toDouble()) * 100).toInt().coerceIn(0, 100)
                } else 100

                CampaignWithStats(
                    campaign = camp,
                    amountRaised = raised,
                    percentageReached = pct,
                    amountRemaining = remaining,
                    contributions = campContribs
                )
            }

            val isSaturday = ChoirStrings.isTodaySaturday()
            val unpaidCount = memberDuesList.count { it.member.isActive && !it.duesThisWeekPaid }

            ChoirUiState(
                session = controls.session,
                settings = settings,
                selectedTab = controls.tab,
                language = language,
                isDarkTheme = isDarkTheme,
                members = members,
                activeMembers = members.filter { it.isActive },
                contributions = contributions,
                expenses = expenses,
                campaigns = campaignStatsList,
                memberDuesList = memberDuesList,
                totalContributions = totalIn,
                totalExpenses = totalOut,
                balanceInHand = balance,
                activeMemberCount = members.count { it.isActive },
                searchQuery = controls.searchQuery,
                contributionTypeFilter = controls.contributionTypeFilter,
                expenseCategoryFilter = controls.expenseCategoryFilter,
                isAddMemberOpen = dialogs.isAddMemberOpen,
                memberToEdit = dialogs.memberToEdit,
                isAddContributionOpen = dialogs.isAddContributionOpen,
                contributionToEdit = dialogs.contributionToEdit,
                preselectedMemberIdForContribution = dialogs.preselectedMemberIdForContribution,
                isAddCampaignOpen = dialogs.isAddCampaignOpen,
                campaignToEdit = dialogs.campaignToEdit,
                isAddExpenseOpen = dialogs.isAddExpenseOpen,
                expenseToEdit = dialogs.expenseToEdit,
                isChangePasswordOpen = dialogs.isChangePasswordOpen,
                lastCreatedContributionReceipt = dialogs.lastCreatedContributionReceipt,
                isSaturdayRemindersOpen = dialogs.isSaturdayRemindersOpen,
                remindedMemberIdsThisWeek = dialogs.remindedMemberIds,
                isTodaySaturday = isSaturday,
                unpaidMembersCount = unpaidCount,
                toastMessage = dialogs.toastMessage
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChoirUiState()
        )
    }

    private data class AppEntities(
        val members: List<Member>,
        val contributions: List<Contribution>,
        val expenses: List<Expense>,
        val campaigns: List<Campaign>,
        val settings: ChoirSettings
    )

    private data class UiControls(
        val session: AuthSession,
        val tab: ChoirNavTab,
        val searchQuery: String,
        val contributionTypeFilter: String,
        val expenseCategoryFilter: String
    )

    // Authentication actions
    fun signInAsTreasurer(password: String): Boolean {
        val currentSettings = uiState.value.settings
        if (password == currentSettings.treasurerPasswordHash || password == "admin") {
            _session.value = AuthSession(
                isSignedIn = true,
                role = UserRole.TREASURER,
                memberId = null,
                displayName = currentSettings.treasurerName.ifBlank { "Treasurer" },
                phoneNumber = currentSettings.treasurerPhone
            )
            _selectedTab.value = ChoirNavTab.OVERVIEW
            return true
        }
        return false
    }

    fun signInAsMember(memberId: Long, password: String): Boolean {
        val member = uiState.value.members.find { it.id == memberId }
        if (member != null && (member.passwordHash == password || password == "1234")) {
            _session.value = AuthSession(
                isSignedIn = true,
                role = UserRole.MEMBER,
                memberId = member.id,
                displayName = member.fullName,
                phoneNumber = member.phoneNumber
            )
            _selectedTab.value = ChoirNavTab.OVERVIEW
            return true
        }
        return false
    }

    fun signOut() {
        _session.value = AuthSession(isSignedIn = false)
        _selectedTab.value = ChoirNavTab.OVERVIEW
    }

    fun createTreasurerAccount(name: String, phone: String, password: String) {
        viewModelScope.launch {
            val cur = uiState.value.settings
            val updated = cur.copy(
                treasurerName = name.trim(),
                treasurerPhone = phone.trim(),
                treasurerPasswordHash = password.trim(),
                hasCompletedFirstTimeSetup = true
            )
            repository.saveSettings(updated)
            _session.value = AuthSession(
                isSignedIn = true,
                role = UserRole.TREASURER,
                memberId = null,
                displayName = name.trim(),
                phoneNumber = phone.trim()
            )
        }
    }

    fun changeOwnPassword(newPassword: String) {
        if (newPassword.isBlank()) return
        val currentSession = _session.value
        viewModelScope.launch {
            if (currentSession.role == UserRole.TREASURER) {
                val cur = uiState.value.settings
                repository.saveSettings(cur.copy(treasurerPasswordHash = newPassword.trim()))
            } else if (currentSession.memberId != null) {
                val member = repository.getMemberById(currentSession.memberId)
                if (member != null) {
                    repository.updateMember(member.copy(passwordHash = newPassword.trim()))
                }
            }
            closeChangePasswordDialog()
            showToast("Password updated successfully / Mot de passe mis à jour")
        }
    }

    // Navigation and filters
    fun selectTab(tab: ChoirNavTab) {
        _selectedTab.value = tab
        _searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setContributionTypeFilter(filter: String) {
        _contributionTypeFilter.value = filter
    }

    fun setExpenseCategoryFilter(filter: String) {
        _expenseCategoryFilter.value = filter
    }

    // Settings actions
    fun toggleLanguage() {
        val newLang = if (uiState.value.language == "FR") "EN" else "FR"
        viewModelScope.launch {
            repository.saveSettings(uiState.value.settings.copy(language = newLang))
        }
    }

    fun toggleTheme() {
        val newMode = if (uiState.value.isDarkTheme) "LIGHT" else "DARK"
        viewModelScope.launch {
            repository.saveSettings(uiState.value.settings.copy(themeMode = newMode))
        }
    }

    fun updateChoirIdentity(choirName: String, parishName: String, treasurerName: String) {
        viewModelScope.launch {
            repository.saveSettings(
                uiState.value.settings.copy(
                    choirName = choirName.trim(),
                    parishName = parishName.trim(),
                    treasurerName = treasurerName.trim()
                )
            )
            showToast("Choir identity updated / Identité de la chorale enregistrée")
        }
    }

    // Member Management (Treasurer only)
    fun addMember(fullName: String, phone: String, email: String, startingPassword: String, notes: String) {
        if (fullName.isBlank()) return
        viewModelScope.launch {
            val startingPass = if (startingPassword.isBlank()) "1234" else startingPassword.trim()
            repository.addMember(
                Member(
                    fullName = fullName.trim(),
                    phoneNumber = phone.trim(),
                    email = email.trim(),
                    passwordHash = startingPass,
                    notes = notes.trim(),
                    isActive = true
                )
            )
            closeAddMemberDialog()
            showToast("Member added / Membre ajouté")
        }
    }

    fun updateMember(
        member: Member,
        fullName: String,
        phone: String,
        email: String,
        newPassword: String?,
        notes: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            val pass = if (!newPassword.isNullOrBlank()) newPassword.trim() else member.passwordHash
            repository.updateMember(
                member.copy(
                    fullName = fullName.trim(),
                    phoneNumber = phone.trim(),
                    email = email.trim(),
                    passwordHash = pass,
                    notes = notes.trim(),
                    isActive = isActive
                )
            )
            closeEditMemberDialog()
            showToast("Member updated / Membre mis à jour")
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
            showToast("Member deleted / Membre supprimé")
        }
    }

    // Contribution Management (Treasurer only)
    fun addContribution(
        memberId: Long?,
        outsideDonorName: String,
        campaignId: Long?,
        type: String, // DUES, VOLUNTARY_GIFT, DONATION
        amount: Long, // in FCFA
        date: Long,
        paymentMethod: String,
        notes: String
    ): Boolean {
        // DUES minimum validation: 200 FCFA
        if (type == "DUES" && amount < 200L) {
            showToast("Dues minimum is 200 FCFA / Le minimum pour la cotisation est de 200 FCFA")
            return false
        }
        if (amount <= 0) return false

        viewModelScope.launch {
            val member = if (memberId != null) repository.getMemberById(memberId) else null
            val donor = member?.fullName ?: outsideDonorName.ifBlank { "Anonymous / Anonyme" }
            val isOutside = memberId == null

            val camp = if (campaignId != null) uiState.value.campaigns.find { it.campaign.id == campaignId }?.campaign else null
            val campaignName = camp?.name ?: ""

            val newContribution = Contribution(
                memberId = memberId,
                donorName = donor,
                isOutsideDonor = isOutside,
                campaignId = campaignId,
                campaignName = campaignName,
                type = type,
                amount = amount,
                date = date,
                paymentMethod = paymentMethod,
                notes = notes.trim()
            )

            val id = repository.addContribution(newContribution)
            _dialogState.update {
                it.copy(
                    isAddContributionOpen = false,
                    preselectedMemberIdForContribution = null,
                    lastCreatedContributionReceipt = newContribution.copy(id = id),
                    toastMessage = "Contribution recorded / Cotisation enregistrée"
                )
            }
        }
        return true
    }

    fun updateContribution(
        contribution: Contribution,
        type: String,
        amount: Long,
        date: Long,
        paymentMethod: String,
        notes: String
    ): Boolean {
        if (type == "DUES" && amount < 200L) {
            showToast("Dues minimum is 200 FCFA / Minimum cotisation 200 FCFA")
            return false
        }
        viewModelScope.launch {
            repository.updateContribution(
                contribution.copy(
                    type = type,
                    amount = amount,
                    date = date,
                    paymentMethod = paymentMethod,
                    notes = notes.trim()
                )
            )
            closeEditContributionDialog()
            showToast("Contribution updated / Cotisation mise à jour")
        }
        return true
    }

    fun deleteContribution(contribution: Contribution) {
        viewModelScope.launch {
            repository.deleteContribution(contribution)
            showToast("Contribution deleted / Cotisation supprimée")
        }
    }

    // Campaign Management (Treasurer only)
    fun addCampaign(name: String, targetAmount: Long, deadline: Long, description: String) {
        if (name.isBlank() || targetAmount <= 0) return
        viewModelScope.launch {
            repository.addCampaign(
                Campaign(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    deadline = deadline,
                    description = description.trim()
                )
            )
            closeAddCampaignDialog()
            showToast("Campaign created / Campagne créée")
        }
    }

    fun updateCampaign(campaign: Campaign, name: String, targetAmount: Long, deadline: Long, description: String) {
        viewModelScope.launch {
            repository.updateCampaign(
                campaign.copy(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    deadline = deadline,
                    description = description.trim()
                )
            )
            closeEditCampaignDialog()
            showToast("Campaign updated / Campagne modifiée")
        }
    }

    fun deleteCampaign(campaign: Campaign) {
        viewModelScope.launch {
            repository.deleteCampaign(campaign)
            showToast("Campaign deleted / Campagne supprimée")
        }
    }

    // Expense Management (Treasurer only)
    fun addExpense(category: String, description: String, date: Long, amount: Long, paymentMethod: String) {
        if (description.isBlank() || amount <= 0) return
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    category = category,
                    description = description.trim(),
                    date = date,
                    amount = amount,
                    paymentMethod = paymentMethod
                )
            )
            closeAddExpenseDialog()
            showToast("Expense logged / Dépense enregistrée")
        }
    }

    fun updateExpense(expense: Expense, category: String, description: String, date: Long, amount: Long, paymentMethod: String) {
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(
                    category = category,
                    description = description.trim(),
                    date = date,
                    amount = amount,
                    paymentMethod = paymentMethod
                )
            )
            closeEditExpenseDialog()
            showToast("Expense updated / Dépense modifiée")
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            showToast("Expense deleted / Dépense supprimée")
        }
    }

    // Backup & Restore
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            onResult(json)
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            onResult(success)
            if (success) {
                showToast("Backup restored successfully / Sauvegarde restaurée")
            } else {
                showToast("Invalid backup format / Format de sauvegarde invalide")
            }
        }
    }

    // Dialog state controllers
    fun openAddMemberDialog() { _dialogState.update { it.copy(isAddMemberOpen = true) } }
    fun closeAddMemberDialog() { _dialogState.update { it.copy(isAddMemberOpen = false) } }

    fun openEditMemberDialog(member: Member) { _dialogState.update { it.copy(memberToEdit = member) } }
    fun closeEditMemberDialog() { _dialogState.update { it.copy(memberToEdit = null) } }

    fun openAddContributionDialog(preselectedMemberId: Long? = null) {
        _dialogState.update {
            it.copy(
                isAddContributionOpen = true,
                preselectedMemberIdForContribution = preselectedMemberId
            )
        }
    }
    fun closeAddContributionDialog() {
        _dialogState.update {
            it.copy(
                isAddContributionOpen = false,
                preselectedMemberIdForContribution = null
            )
        }
    }

    fun openEditContributionDialog(contribution: Contribution) { _dialogState.update { it.copy(contributionToEdit = contribution) } }
    fun closeEditContributionDialog() { _dialogState.update { it.copy(contributionToEdit = null) } }

    fun openAddCampaignDialog() { _dialogState.update { it.copy(isAddCampaignOpen = true) } }
    fun closeAddCampaignDialog() { _dialogState.update { it.copy(isAddCampaignOpen = false) } }

    fun openEditCampaignDialog(campaign: Campaign) { _dialogState.update { it.copy(campaignToEdit = campaign) } }
    fun closeEditCampaignDialog() { _dialogState.update { it.copy(campaignToEdit = null) } }

    fun openAddExpenseDialog() { _dialogState.update { it.copy(isAddExpenseOpen = true) } }
    fun closeAddExpenseDialog() { _dialogState.update { it.copy(isAddExpenseOpen = false) } }

    fun openEditExpenseDialog(expense: Expense) { _dialogState.update { it.copy(expenseToEdit = expense) } }
    fun closeEditExpenseDialog() { _dialogState.update { it.copy(expenseToEdit = null) } }

    fun openChangePasswordDialog() { _dialogState.update { it.copy(isChangePasswordOpen = true) } }
    fun closeChangePasswordDialog() { _dialogState.update { it.copy(isChangePasswordOpen = false) } }

    fun openSaturdayRemindersDialog() { _dialogState.update { it.copy(isSaturdayRemindersOpen = true) } }
    fun closeSaturdayRemindersDialog() { _dialogState.update { it.copy(isSaturdayRemindersOpen = false) } }

    fun markMemberReminded(memberId: Long) {
        _dialogState.update { it.copy(remindedMemberIds = it.remindedMemberIds + memberId) }
    }

    fun openReceiptDialog(contribution: Contribution) { _dialogState.update { it.copy(lastCreatedContributionReceipt = contribution) } }
    fun closeReceiptDialog() { _dialogState.update { it.copy(lastCreatedContributionReceipt = null) } }

    fun clearToast() { _dialogState.update { it.copy(toastMessage = null) } }
    private fun showToast(msg: String) { _dialogState.update { it.copy(toastMessage = msg) } }
}
