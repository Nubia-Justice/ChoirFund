package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "choir_settings")
data class ChoirSettings(
    @PrimaryKey
    val id: Int = 1,
    val choirName: String = "Your Choir Name",
    val parishName: String = "Parish / Church name",
    val treasurerName: String = "Treasurer",
    val treasurerPhone: String = "",
    val treasurerPasswordHash: String = "admin",
    val hasCompletedFirstTimeSetup: Boolean = false,
    val themeMode: String = "LIGHT", // LIGHT, DARK
    val language: String = "EN" // EN, FR
)
