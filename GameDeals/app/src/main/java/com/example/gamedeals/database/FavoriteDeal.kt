package com.example.gamedeals.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_deals", primaryKeys = ["title", "userEmail"])
data class FavoriteDeal(
    val title: String,
    val salePrice: String,
    val normalPrice: String,
    val storeID: String,
    val thumb: String,
    val userEmail: String
)
