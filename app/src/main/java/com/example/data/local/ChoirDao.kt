package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Campaign
import com.example.data.model.ChoirSettings
import com.example.data.model.Contribution
import com.example.data.model.Expense
import com.example.data.model.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoirDao {
    // Members
    @Query("SELECT * FROM members ORDER BY fullName ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Long): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Query("SELECT COUNT(*) FROM members WHERE isActive = 1")
    fun countActiveMembers(): Flow<Int>

    // Contributions
    @Query("SELECT * FROM contributions ORDER BY date DESC, id DESC")
    fun getAllContributions(): Flow<List<Contribution>>

    @Query("SELECT * FROM contributions WHERE memberId = :memberId ORDER BY date DESC")
    fun getContributionsForMember(memberId: Long): Flow<List<Contribution>>

    @Query("SELECT * FROM contributions WHERE campaignId = :campaignId ORDER BY date DESC")
    fun getContributionsForCampaign(campaignId: Long): Flow<List<Contribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: Contribution): Long

    @Update
    suspend fun updateContribution(contribution: Contribution)

    @Delete
    suspend fun deleteContribution(contribution: Contribution)

    @Query("SELECT SUM(amount) FROM contributions")
    fun getTotalContributionsSum(): Flow<Long?>

    // Campaigns
    @Query("SELECT * FROM campaigns ORDER BY deadline ASC, id DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaignById(id: Long): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpensesSum(): Flow<Long?>

    // Settings
    @Query("SELECT * FROM choir_settings WHERE id = 1")
    fun getSettings(): Flow<ChoirSettings?>

    @Query("SELECT * FROM choir_settings WHERE id = 1")
    suspend fun getSettingsOnce(): ChoirSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ChoirSettings)

    // Wipe and reset for backup import
    @Query("DELETE FROM contributions")
    suspend fun clearAllContributions()

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM campaigns")
    suspend fun clearAllCampaigns()

    @Query("DELETE FROM members")
    suspend fun clearAllMembers()
}
