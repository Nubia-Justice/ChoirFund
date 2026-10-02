package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String, // main contact
    val email: String = "",
    val joiningDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val notes: String = "",
    val passwordHash: String = "1234" // Starting password set by treasurer
)
