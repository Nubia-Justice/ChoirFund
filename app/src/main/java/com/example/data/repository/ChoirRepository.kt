package com.example.data.repository

import com.example.data.local.ChoirDao
import com.example.data.model.Campaign
import com.example.data.model.ChoirSettings
import com.example.data.model.Contribution
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.ui.util.ChoirStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ChoirRepository(private val choirDao: ChoirDao) {

    val members: Flow<List<Member>> = choirDao.getAllMembers()
    val contributions: Flow<List<Contribution>> = choirDao.getAllContributions()
    val expenses: Flow<List<Expense>> = choirDao.getAllExpenses()
    val campaigns: Flow<List<Campaign>> = choirDao.getAllCampaigns()
    val settings: Flow<ChoirSettings?> = choirDao.getSettings()
    val totalContributions: Flow<Long?> = choirDao.getTotalContributionsSum()
    val totalExpenses: Flow<Long?> = choirDao.getTotalExpensesSum()
    val activeMemberCount: Flow<Int> = choirDao.countActiveMembers()

    fun getMemberContributions(memberId: Long): Flow<List<Contribution>> {
        return choirDao.getContributionsForMember(memberId)
    }

    fun getCampaignContributions(campaignId: Long): Flow<List<Contribution>> {
        return choirDao.getContributionsForCampaign(campaignId)
    }

    suspend fun getMemberById(id: Long): Member? = withContext(Dispatchers.IO) {
        choirDao.getMemberById(id)
    }

    suspend fun addMember(member: Member): Long = withContext(Dispatchers.IO) {
        choirDao.insertMember(member)
    }

    suspend fun updateMember(member: Member) = withContext(Dispatchers.IO) {
        choirDao.updateMember(member)
    }

    suspend fun deleteMember(member: Member) = withContext(Dispatchers.IO) {
        choirDao.deleteMember(member)
    }

    suspend fun addContribution(contribution: Contribution): Long = withContext(Dispatchers.IO) {
        choirDao.insertContribution(contribution)
    }

    suspend fun updateContribution(contribution: Contribution) = withContext(Dispatchers.IO) {
        choirDao.updateContribution(contribution)
    }

    suspend fun deleteContribution(contribution: Contribution) = withContext(Dispatchers.IO) {
        choirDao.deleteContribution(contribution)
    }

    suspend fun addCampaign(campaign: Campaign): Long = withContext(Dispatchers.IO) {
        choirDao.insertCampaign(campaign)
    }

    suspend fun updateCampaign(campaign: Campaign) = withContext(Dispatchers.IO) {
        choirDao.updateCampaign(campaign)
    }

    suspend fun deleteCampaign(campaign: Campaign) = withContext(Dispatchers.IO) {
        choirDao.deleteCampaign(campaign)
    }

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        choirDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) = withContext(Dispatchers.IO) {
        choirDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        choirDao.deleteExpense(expense)
    }

    suspend fun saveSettings(settings: ChoirSettings) = withContext(Dispatchers.IO) {
        choirDao.saveSettings(settings)
    }

    suspend fun getSettingsOnce(): ChoirSettings? = withContext(Dispatchers.IO) {
        choirDao.getSettingsOnce()
    }

    suspend fun populateInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentSettings = choirDao.getSettingsOnce()
        if (currentSettings == null) {
            // First time setup settings
            val initialSettings = ChoirSettings(
                id = 1,
                choirName = "Your Choir Name",
                parishName = "Parish / Church name",
                treasurerName = "Jean-Paul Mbarga",
                treasurerPhone = "677123456",
                treasurerPasswordHash = "admin123",
                hasCompletedFirstTimeSetup = true,
                themeMode = "LIGHT",
                language = "EN"
            )
            choirDao.saveSettings(initialSettings)

            val sampleMembers = listOf(
                Member(
                    fullName = "Pauline Ngono",
                    phoneNumber = "675891234",
                    email = "pauline.ngono@gmail.com",
                    notes = "Chorale Sainte Cécile",
                    passwordHash = "1234"
                ),
                Member(
                    fullName = "Henriette Fouda",
                    phoneNumber = "699451230",
                    email = "henriette.f@yahoo.com",
                    notes = "Ancienne trésorière",
                    passwordHash = "1234"
                ),
                Member(
                    fullName = "Alain Kemgang",
                    phoneNumber = "674201948",
                    email = "alain.kemgang@gmail.com",
                    notes = "Responsable pupitre",
                    passwordHash = "1234"
                ),
                Member(
                    fullName = "Brice Eyenga",
                    phoneNumber = "690112233",
                    email = "b.eyenga@gmail.com",
                    notes = "Membre actif",
                    passwordHash = "1234"
                ),
                Member(
                    fullName = "Chantal Mvondo",
                    phoneNumber = "677998877",
                    email = "chantal.mvondo@gmail.com",
                    notes = "Comité d'organisation",
                    passwordHash = "1234"
                )
            )

            val memberIds = mutableListOf<Long>()
            for (m in sampleMembers) {
                memberIds.add(choirDao.insertMember(m))
            }

            // Target Campaign: "New Speakers"
            val campaignId = choirDao.insertCampaign(
                Campaign(
                    name = "New Speakers",
                    targetAmount = 500000L, // 500 000 FCFA
                    deadline = System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000,
                    description = "Acquisition of two Yamaha active speakers & cables for the cathedral"
                )
            )

            val now = System.currentTimeMillis()
            val day = 86_400_000L
            val currentWeekSaturday = ChoirStrings.getDuesWeekStart(now)

            // Sample contributions
            val sampleContributions = listOf(
                // Paid dues this week (Saturday)
                Contribution(
                    memberId = memberIds[0],
                    donorName = "Pauline Ngono",
                    isOutsideDonor = false,
                    type = "DUES",
                    amount = 500L,
                    date = currentWeekSaturday + 3600000L,
                    paymentMethod = "MTN_MOMO",
                    notes = "Weekly dues Saturday rehearsal"
                ),
                Contribution(
                    memberId = memberIds[1],
                    donorName = "Henriette Fouda",
                    isOutsideDonor = false,
                    type = "DUES",
                    amount = 200L,
                    date = currentWeekSaturday + 7200000L,
                    paymentMethod = "ORANGE_MONEY",
                    notes = "Weekly dues"
                ),
                // Campaign gift
                Contribution(
                    memberId = memberIds[2],
                    donorName = "Alain Kemgang",
                    isOutsideDonor = false,
                    campaignId = campaignId,
                    campaignName = "New Speakers",
                    type = "DONATION",
                    amount = 50000L,
                    date = now - day * 3,
                    paymentMethod = "MTN_MOMO",
                    notes = "Contribution for the new sound system"
                ),
                // Outside donor
                Contribution(
                    memberId = null,
                    donorName = "Elder Joseph Tabi",
                    isOutsideDonor = true,
                    campaignId = campaignId,
                    campaignName = "New Speakers",
                    type = "VOLUNTARY_GIFT",
                    amount = 100000L,
                    date = now - day * 5,
                    paymentMethod = "CASH",
                    notes = "Parish patron donation for speakers"
                ),
                Contribution(
                    memberId = memberIds[3],
                    donorName = "Brice Eyenga",
                    isOutsideDonor = false,
                    type = "DUES",
                    amount = 200L,
                    date = now - day * 8, // Previous week dues
                    paymentMethod = "CASH",
                    notes = "Previous week dues"
                )
            )

            for (c in sampleContributions) {
                choirDao.insertContribution(c)
            }

            // Sample expenses
            val sampleExpenses = listOf(
                Expense(
                    category = "SHEET_MUSIC",
                    description = "Photocopies and choir hymn sheets for Saint Joseph feast",
                    date = now - day * 4,
                    amount = 15000L,
                    paymentMethod = "CASH"
                ),
                Expense(
                    category = "REFRESHMENTS",
                    description = "Saturday rehearsal mineral water & snacks",
                    date = now - day * 2,
                    amount = 8000L,
                    paymentMethod = "ORANGE_MONEY"
                ),
                Expense(
                    category = "TRANSPORT",
                    description = "Transport refund for parish choir exchange concert",
                    date = now - day * 10,
                    amount = 25000L,
                    paymentMethod = "MTN_MOMO"
                )
            )

            for (e in sampleExpenses) {
                choirDao.insertExpense(e)
            }
        }
    }

    // Export JSON Backup
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val settings = choirDao.getSettingsOnce()
        if (settings != null) {
            val settingsObj = JSONObject().apply {
                put("choirName", settings.choirName)
                put("parishName", settings.parishName)
                put("treasurerName", settings.treasurerName)
                put("treasurerPhone", settings.treasurerPhone)
            }
            root.put("settings", settingsObj)
        }

        val membersList = choirDao.getAllMembers().firstOrNull() ?: emptyList()
        val membersArr = JSONArray()
        membersList.forEach { m ->
            membersArr.put(JSONObject().apply {
                put("id", m.id)
                put("fullName", m.fullName)
                put("phoneNumber", m.phoneNumber)
                put("email", m.email)
                put("joiningDate", m.joiningDate)
                put("isActive", m.isActive)
                put("notes", m.notes)
                put("passwordHash", m.passwordHash)
            })
        }
        root.put("members", membersArr)

        val contribList = choirDao.getAllContributions().firstOrNull() ?: emptyList()
        val contribArr = JSONArray()
        contribList.forEach { c ->
            contribArr.put(JSONObject().apply {
                put("id", c.id)
                put("memberId", c.memberId ?: JSONObject.NULL)
                put("donorName", c.donorName)
                put("isOutsideDonor", c.isOutsideDonor)
                put("campaignId", c.campaignId ?: JSONObject.NULL)
                put("campaignName", c.campaignName)
                put("type", c.type)
                put("amount", c.amount)
                put("date", c.date)
                put("paymentMethod", c.paymentMethod)
                put("notes", c.notes)
            })
        }
        root.put("contributions", contribArr)

        val expList = choirDao.getAllExpenses().firstOrNull() ?: emptyList()
        val expArr = JSONArray()
        expList.forEach { e ->
            expArr.put(JSONObject().apply {
                put("id", e.id)
                put("category", e.category)
                put("description", e.description)
                put("date", e.date)
                put("amount", e.amount)
                put("paymentMethod", e.paymentMethod)
            })
        }
        root.put("expenses", expArr)

        val campList = choirDao.getAllCampaigns().firstOrNull() ?: emptyList()
        val campArr = JSONArray()
        campList.forEach { cp ->
            campArr.put(JSONObject().apply {
                put("id", cp.id)
                put("name", cp.name)
                put("targetAmount", cp.targetAmount)
                put("deadline", cp.deadline)
                put("description", cp.description)
                put("isArchived", cp.isArchived)
            })
        }
        root.put("campaigns", campArr)

        root.toString(2)
    }

    // Import JSON Backup
    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                val cur = choirDao.getSettingsOnce() ?: ChoirSettings()
                choirDao.saveSettings(
                    cur.copy(
                        choirName = s.optString("choirName", cur.choirName),
                        parishName = s.optString("parishName", cur.parishName),
                        treasurerName = s.optString("treasurerName", cur.treasurerName),
                        treasurerPhone = s.optString("treasurerPhone", cur.treasurerPhone)
                    )
                )
            }

            if (root.has("members")) {
                val arr = root.getJSONArray("members")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    choirDao.insertMember(
                        Member(
                            id = o.optLong("id", 0),
                            fullName = o.getString("fullName"),
                            phoneNumber = o.optString("phoneNumber", ""),
                            email = o.optString("email", ""),
                            joiningDate = o.optLong("joiningDate", System.currentTimeMillis()),
                            isActive = o.optBoolean("isActive", true),
                            notes = o.optString("notes", ""),
                            passwordHash = o.optString("passwordHash", "1234")
                        )
                    )
                }
            }

            if (root.has("campaigns")) {
                val arr = root.getJSONArray("campaigns")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    choirDao.insertCampaign(
                        Campaign(
                            id = o.optLong("id", 0),
                            name = o.getString("name"),
                            targetAmount = o.getLong("targetAmount"),
                            deadline = o.optLong("deadline", System.currentTimeMillis()),
                            description = o.optString("description", ""),
                            isArchived = o.optBoolean("isArchived", false)
                        )
                    )
                }
            }

            if (root.has("contributions")) {
                val arr = root.getJSONArray("contributions")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val memId = if (o.isNull("memberId")) null else o.optLong("memberId")
                    val campId = if (o.isNull("campaignId")) null else o.optLong("campaignId")
                    choirDao.insertContribution(
                        Contribution(
                            id = o.optLong("id", 0),
                            memberId = memId,
                            donorName = o.getString("donorName"),
                            isOutsideDonor = o.optBoolean("isOutsideDonor", false),
                            campaignId = campId,
                            campaignName = o.optString("campaignName", ""),
                            type = o.optString("type", "DUES"),
                            amount = o.getLong("amount"),
                            date = o.optLong("date", System.currentTimeMillis()),
                            paymentMethod = o.optString("paymentMethod", "CASH"),
                            notes = o.optString("notes", "")
                        )
                    )
                }
            }

            if (root.has("expenses")) {
                val arr = root.getJSONArray("expenses")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    choirDao.insertExpense(
                        Expense(
                            id = o.optLong("id", 0),
                            category = o.getString("category"),
                            description = o.getString("description"),
                            date = o.optLong("date", System.currentTimeMillis()),
                            amount = o.getLong("amount"),
                            paymentMethod = o.optString("paymentMethod", "CASH")
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
