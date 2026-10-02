package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "New Speakers"
    val targetAmount: Long, // in FCFA
    val deadline: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val description: String = "",
    val isArchived: Boolean = false
)
