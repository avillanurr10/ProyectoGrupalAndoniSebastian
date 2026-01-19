package com.example.gamedeals.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteDeal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val salePrice: String,
    val normalPrice: String,
    val storeID: String,
    val thumb: String
)
