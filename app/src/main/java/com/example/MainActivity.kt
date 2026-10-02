package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddEditCampaignDialog
import com.example.ui.dialogs.AddEditContributionDialog
import com.example.ui.dialogs.AddEditExpenseDialog
import com.example.ui.dialogs.AddEditMemberDialog
import com.example.ui.dialogs.ChangePasswordDialog
import com.example.ui.dialogs.SaturdayDuesReminderDialog
import com.example.ui.dialogs.WhatsAppReceiptDialog
import com.example.util.SaturdayReminderScheduler
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CampaignsScreen
import com.example.ui.screens.ContributionsScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.MemberDashboardScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TreasurerOverviewScreen
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ChoirNavTab
import com.example.ui.viewmodel.ChoirUiState
import com.example.ui.viewmodel.ChoirViewModel
import com.example.ui.viewmodel.UserRole

class MainActivity : ComponentActivity() {

    private val viewModel: ChoirViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(isDarkTheme = state.isDarkTheme) {
                ChoirFundsApp(viewModel = viewModel, state = state)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(SaturdayReminderScheduler.EXTRA_OPEN_SATURDAY_REMINDERS, false) == true) {
            viewModel.openSaturdayRemindersDialog()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoirFundsApp(viewModel: ChoirViewModel, state: ChoirUiState) {
    val context = LocalContext.current
    val isFr = state.language == "FR"

    // Request notification permission on Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* granted or denied */ }

        LaunchedEffect(Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Toast message handling
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // If not signed in, show Auth Screen (First-time setup or Login)
    if (!state.session.isSignedIn) {
        AuthScreen(
            state = state,
            onSignInAsTreasurer = { pass -> viewModel.signInAsTreasurer(pass) },
            onSignInAsMember = { id, pass -> viewModel.signInAsMember(id, pass) },
            onCreateTreasurer = { name, phone, pass -> viewModel.createTreasurerAccount(name, phone, pass) },
            onToggleLanguage = { viewModel.toggleLanguage() },
            onToggleTheme = { viewModel.toggleTheme() }
        )
        return
    }

    val isTreasurer = state.session.role == UserRole.TREASURER

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = state.settings.choirName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Hymn Book",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 12.dp).size(20.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = state.language,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ForestGreenPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                val navItems = if (isTreasurer) {
                    listOf(
                        NavigationItem(
                            ChoirNavTab.OVERVIEW,
                            if (isFr) "Aperçu" else "Overview",
                            Icons.Default.Dashboard,
                            "tab_overview"
                        ),
                        NavigationItem(
                            ChoirNavTab.MEMBERS,
                            if (isFr) "Membres" else "Members",
                            Icons.Default.Group,
                            "tab_members"
                        ),
                        NavigationItem(
                            ChoirNavTab.CONTRIBUTIONS,
                            if (isFr) "Cotisations" else "Dues & Gifts",
                            Icons.Default.Payments,
                            "tab_contributions"
                        ),
                        NavigationItem(
                            ChoirNavTab.CAMPAIGNS,
                            if (isFr) "Projets" else "Campaigns",
                            Icons.Default.Campaign,
                            "tab_campaigns"
                        ),
                        NavigationItem(
                            ChoirNavTab.EXPENSES,
                            if (isFr) "Dépenses" else "Expenses",
                            Icons.Default.TrendingDown,
                            "tab_expenses"
                        ),
                        NavigationItem(
                            ChoirNavTab.SETTINGS,
                            if (isFr) "Paramètres" else "Settings",
                            Icons.Default.Settings,
                            "tab_settings"
                        )
                    )
                } else {
                    // Member tabs
                    listOf(
                        NavigationItem(
                            ChoirNavTab.OVERVIEW,
                            if (isFr) "Mon bilan" else "My Giving",
                            Icons.Default.Dashboard,
                            "tab_overview"
                        ),
                        NavigationItem(
                            ChoirNavTab.CAMPAIGNS,
                            if (isFr) "Projets" else "Campaigns",
                            Icons.Default.Campaign,
                            "tab_campaigns"
                        ),
                        NavigationItem(
                            ChoirNavTab.EXPENSES,
                            if (isFr) "Dépenses" else "Expenses",
                            Icons.Default.TrendingDown,
                            "tab_expenses"
                        ),
                        NavigationItem(
                            ChoirNavTab.SETTINGS,
                            if (isFr) "Paramètres" else "Settings",
                            Icons.Default.Settings,
                            "tab_settings"
                        )
                    )
                }

                navItems.forEach { item ->
                    val isSelected = state.selectedTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(item.tab) },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp))
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreenPrimary,
                            selectedTextColor = ForestGreenPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.selectedTab) {
                ChoirNavTab.OVERVIEW -> {
                    if (isTreasurer) {
                        TreasurerOverviewScreen(
                            state = state,
                            onTabSelect = { viewModel.selectTab(it) },
                            onAddContribution = { viewModel.openAddContributionDialog() },
                            onAddMember = { viewModel.openAddMemberDialog() },
                            onAddExpense = { viewModel.openAddExpenseDialog() },
                            onAddCampaign = { viewModel.openAddCampaignDialog() },
                            onOpenWhatsAppReceipt = {
                                val member = state.members.find { m -> m.id == it.memberId }
                                val phone = member?.phoneNumber ?: ""
                                viewModel.closeReceiptDialog()
                                viewModel.openAddContributionDialog(null)
                            },
                            onOpenSaturdayReminders = { viewModel.openSaturdayRemindersDialog() }
                        )
                    } else {
                        MemberDashboardScreen(state = state)
                    }
                }
                ChoirNavTab.MEMBERS -> {
                    if (isTreasurer) {
                        MembersScreen(
                            state = state,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onAddMember = { viewModel.openAddMemberDialog() },
                            onEditMember = { viewModel.openEditMemberDialog(it) },
                            onDeleteMember = { viewModel.deleteMember(it) },
                            onAddContributionForMember = { viewModel.openAddContributionDialog(it) },
                            onOpenSaturdayReminders = { viewModel.openSaturdayRemindersDialog() }
                        )
                    } else {
                        MemberDashboardScreen(state = state)
                    }
                }
                ChoirNavTab.CONTRIBUTIONS -> {
                    if (isTreasurer) {
                        ContributionsScreen(
                            state = state,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onTypeFilterChange = { viewModel.setContributionTypeFilter(it) },
                            onAddContribution = { viewModel.openAddContributionDialog() },
                            onEditContribution = { viewModel.openEditContributionDialog(it) },
                            onDeleteContribution = { viewModel.deleteContribution(it) },
                            onOpenWhatsAppReceipt = {
                                val member = state.members.find { m -> m.id == it.memberId }
                                val phone = member?.phoneNumber ?: ""
                                // Open receipt
                            }
                        )
                    } else {
                        MemberDashboardScreen(state = state)
                    }
                }
                ChoirNavTab.CAMPAIGNS -> {
                    CampaignsScreen(
                        state = state,
                        onAddCampaign = { viewModel.openAddCampaignDialog() },
                        onEditCampaign = { viewModel.openEditCampaignDialog(it) },
                        onDeleteCampaign = { viewModel.deleteCampaign(it) }
                    )
                }
                ChoirNavTab.EXPENSES -> {
                    ExpensesScreen(
                        state = state,
                        onCategoryFilterChange = { viewModel.setExpenseCategoryFilter(it) },
                        onAddExpense = { viewModel.openAddExpenseDialog() },
                        onEditExpense = { viewModel.openEditExpenseDialog(it) },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                }
                ChoirNavTab.SETTINGS -> {
                    SettingsScreen(
                        state = state,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onUpdateChoirIdentity = { c, p, t -> viewModel.updateChoirIdentity(c, p, t) },
                        onChangePassword = { viewModel.openChangePasswordDialog() },
                        onExportBackup = { callback -> viewModel.exportBackup(callback) },
                        onImportBackup = { json, cb -> viewModel.importBackup(json, cb) },
                        onSignOut = { viewModel.signOut() },
                        onOpenSaturdayReminders = { viewModel.openSaturdayRemindersDialog() }
                    )
                }
            }
        }
    }

    // DIALOGS
    // 1. Add Member
    if (state.isAddMemberOpen) {
        AddEditMemberDialog(
            language = state.language,
            onSave = { name, phone, email, password, notes, _ ->
                viewModel.addMember(name, phone, email, password, notes)
            },
            onDismiss = { viewModel.closeAddMemberDialog() }
        )
    }

    // 2. Edit Member
    if (state.memberToEdit != null) {
        val member = state.memberToEdit!!
        AddEditMemberDialog(
            initialMember = member,
            language = state.language,
            onSave = { name, phone, email, password, notes, isActive ->
                viewModel.updateMember(member, name, phone, email, password.ifBlank { null }, notes, isActive)
            },
            onDismiss = { viewModel.closeEditMemberDialog() }
        )
    }

    // 3. Add Contribution
    if (state.isAddContributionOpen) {
        AddEditContributionDialog(
            members = state.members,
            campaigns = state.campaigns.map { it.campaign },
            preselectedMemberId = state.preselectedMemberIdForContribution,
            language = state.language,
            onSave = { memId, donor, campId, type, amount, date, method, notes ->
                viewModel.addContribution(memId, donor, campId, type, amount, date, method, notes)
            },
            onDismiss = { viewModel.closeAddContributionDialog() }
        )
    }

    // 4. Edit Contribution
    if (state.contributionToEdit != null) {
        val contrib = state.contributionToEdit!!
        AddEditContributionDialog(
            members = state.members,
            campaigns = state.campaigns.map { it.campaign },
            initialContribution = contrib,
            language = state.language,
            onSave = { _, _, _, type, amount, date, method, notes ->
                viewModel.updateContribution(contrib, type, amount, date, method, notes)
            },
            onDismiss = { viewModel.closeEditContributionDialog() }
        )
    }

    // 5. Add Campaign
    if (state.isAddCampaignOpen) {
        AddEditCampaignDialog(
            language = state.language,
            onSave = { name, target, deadline, desc ->
                viewModel.addCampaign(name, target, deadline, desc)
            },
            onDismiss = { viewModel.closeAddCampaignDialog() }
        )
    }

    // 6. Edit Campaign
    if (state.campaignToEdit != null) {
        val camp = state.campaignToEdit!!
        AddEditCampaignDialog(
            initialCampaign = camp,
            language = state.language,
            onSave = { name, target, deadline, desc ->
                viewModel.updateCampaign(camp, name, target, deadline, desc)
            },
            onDismiss = { viewModel.closeEditCampaignDialog() }
        )
    }

    // 7. Add Expense
    if (state.isAddExpenseOpen) {
        AddEditExpenseDialog(
            language = state.language,
            onSave = { cat, desc, date, amount, method ->
                viewModel.addExpense(cat, desc, date, amount, method)
            },
            onDismiss = { viewModel.closeAddExpenseDialog() }
        )
    }

    // 8. Edit Expense
    if (state.expenseToEdit != null) {
        val exp = state.expenseToEdit!!
        AddEditExpenseDialog(
            initialExpense = exp,
            language = state.language,
            onSave = { cat, desc, date, amount, method ->
                viewModel.updateExpense(exp, cat, desc, date, amount, method)
            },
            onDismiss = { viewModel.closeEditExpenseDialog() }
        )
    }

    // 9. Change Password
    if (state.isChangePasswordOpen) {
        ChangePasswordDialog(
            language = state.language,
            onSave = { newPass -> viewModel.changeOwnPassword(newPass) },
            onDismiss = { viewModel.closeChangePasswordDialog() }
        )
    }

    // 10. WhatsApp Receipt popup (after contribution saved or tapped)
    if (state.lastCreatedContributionReceipt != null) {
        val receipt = state.lastCreatedContributionReceipt!!
        val member = state.members.find { it.id == receipt.memberId }
        val phone = member?.phoneNumber ?: ""
        WhatsAppReceiptDialog(
            contribution = receipt,
            choirName = state.settings.choirName,
            parishName = state.settings.parishName,
            memberPhone = phone,
            language = state.language,
            onDismiss = { viewModel.closeReceiptDialog() }
        )
    }

    // 11. Saturday Dues Reminders Dialog
    if (state.isSaturdayRemindersOpen) {
        val unpaidList = state.memberDuesList.filter { it.member.isActive && !it.duesThisWeekPaid }
        SaturdayDuesReminderDialog(
            unpaidMembers = unpaidList,
            allMembersCount = state.activeMemberCount,
            choirName = state.settings.choirName,
            parishName = state.settings.parishName,
            language = state.language,
            remindedMemberIds = state.remindedMemberIdsThisWeek,
            onMemberReminded = { viewModel.markMemberReminded(it) },
            onDismiss = { viewModel.closeSaturdayRemindersDialog() }
        )
    }
}

private data class NavigationItem(
    val tab: ChoirNavTab,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)
