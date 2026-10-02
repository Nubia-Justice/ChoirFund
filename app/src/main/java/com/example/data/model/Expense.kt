package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // SHEET_MUSIC, SOUND_EQUIPMENT, REFRESHMENTS, UNIFORMS, TRANSPORT, HONORARIUM, WELFARE, OTHER
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val amount: Long, // in FCFA
    val paymentMethod: String = "CASH" // CASH, MTN_MOMO, ORANGE_MONEY
)
