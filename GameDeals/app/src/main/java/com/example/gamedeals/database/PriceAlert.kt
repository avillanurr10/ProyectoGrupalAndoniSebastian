package com.example.gamedeals.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameTitle: String,
    val targetPrice: Double,
    val currentPriceAtSetting: Double,
    val dealID: String,
    val thumb: String,
    val dateSet: Long = System.currentTimeMillis()
)
