package com.example.gamedeals.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_deals")
data class FavoriteDeal(
    @PrimaryKey val title: String,
    val salePrice: String,
    val normalPrice: String,
    val storeID: String,
    val thumb: String,
    val dealID: String
)
