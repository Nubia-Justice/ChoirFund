package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contributions",
    indices = [
        Index(value = ["memberId"]),
        Index(value = ["campaignId"]),
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class Contribution(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long? = null, // null if outside donor
    val donorName: String, // member full name or outside donor name
    val isOutsideDonor: Boolean = false,
    val campaignId: Long? = null,
    val campaignName: String = "",
    val type: String = "DUES", // DUES, VOLUNTARY_GIFT, DONATION
    val amount: Long, // in FCFA (no decimals)
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "CASH", // CASH, MTN_MOMO, ORANGE_MONEY
    val notes: String = ""
)
