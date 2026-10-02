package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChoirDao
import com.example.data.local.ChoirDatabase
import com.example.data.model.Contribution
import com.example.data.model.Expense
import com.example.data.model.Member
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: ChoirDatabase
    private lateinit var dao: ChoirDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ChoirDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.choirDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Choir Funds", appName)
    }

    @Test
    fun `insert member and contribution registers correctly`() = runBlocking {
        val member = Member(
            fullName = "Mary Johnson",
            phoneNumber = "677123456",
            email = "mary@example.com",
            notes = "Active"
        )
        val memberId = dao.insertMember(member)

        val members = dao.getAllMembers().first()
        assertEquals(1, members.size)
        assertEquals("Mary Johnson", members[0].fullName)

        val contribution = Contribution(
            memberId = memberId,
            donorName = "Mary Johnson",
            type = "DUES",
            amount = 1000L,
            paymentMethod = "MTN_MOMO"
        )
        dao.insertContribution(contribution)

        val contributions = dao.getAllContributions().first()
        assertEquals(1, contributions.size)
        assertEquals(1000L, contributions[0].amount)

        val totalSum = dao.getTotalContributionsSum().first()
        assertEquals(1000L, totalSum ?: 0L)
    }

    @Test
    fun `insert expense registers correctly`() = runBlocking {
        val expense = Expense(
            category = "SHEET_MUSIC",
            description = "Hymn sheets printing",
            amount = 5000L,
            paymentMethod = "CASH"
        )
        dao.insertExpense(expense)

        val expenses = dao.getAllExpenses().first()
        assertEquals(1, expenses.size)
        assertEquals(5000L, expenses[0].amount)

        val totalExpensesSum = dao.getTotalExpensesSum().first()
        assertEquals(5000L, totalExpensesSum ?: 0L)
    }

    @Test
    fun `cameroon phone normalization formats 9 digit phone numbers`() {
        val phone9 = "677123456"
        val normalized = com.example.ui.util.ChoirStrings.normalizeCameroonPhone(phone9)
        assertEquals("+237677123456", normalized)

        val alreadyWithPlus = "+237677123456"
        val normalizedPlus = com.example.ui.util.ChoirStrings.normalizeCameroonPhone(alreadyWithPlus)
        assertEquals("+237677123456", normalizedPlus)
    }

    @Test
    fun `whatsapp dues reminder contains member name and minimum 200 FCFA`() {
        val reminderEn = com.example.ui.util.ChoirStrings.buildWhatsAppDuesReminder(
            memberName = "Jean Claude",
            choirName = "Voices of Praise",
            parishName = "St. Paul Parish",
            language = "EN"
        )
        assertNotNull(reminderEn)
        org.junit.Assert.assertTrue(reminderEn.contains("Jean Claude"))
        org.junit.Assert.assertTrue(reminderEn.contains("200 FCFA"))
        org.junit.Assert.assertTrue(reminderEn.contains("MTN Mobile Money"))

        val reminderFr = com.example.ui.util.ChoirStrings.buildWhatsAppDuesReminder(
            memberName = "Pauline Mbarga",
            choirName = "Chœur Sainte Cécile",
            parishName = "Paroisse Saint Esprit",
            language = "FR"
        )
        assertNotNull(reminderFr)
        org.junit.Assert.assertTrue(reminderFr.contains("Pauline Mbarga"))
        org.junit.Assert.assertTrue(reminderFr.contains("samedi"))
        org.junit.Assert.assertTrue(reminderFr.contains("200 FCFA"))
    }

    @Test
    fun `sms dues reminder is concise and formatted for sms`() {
        val sms = com.example.ui.util.ChoirStrings.buildSmsDuesReminder(
            memberName = "David",
            choirName = "Choir Funds",
            language = "FR"
        )
        assertNotNull(sms)
        org.junit.Assert.assertTrue(sms.contains("David"))
        org.junit.Assert.assertTrue(sms.contains("200 FCFA"))
        org.junit.Assert.assertTrue(sms.length < 160) // within single SMS length
    }

    @Test
    fun `saturday reminder scheduler calculates future saturday`() {
        val nextSaturday = com.example.util.SaturdayReminderScheduler.calculateNextSaturdayTimestamp()
        org.junit.Assert.assertTrue(nextSaturday >= System.currentTimeMillis() - 1000)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = nextSaturday }
        assertEquals(java.util.Calendar.SATURDAY, cal.get(java.util.Calendar.DAY_OF_WEEK))
        assertEquals(9, cal.get(java.util.Calendar.HOUR_OF_DAY))
    }
}
