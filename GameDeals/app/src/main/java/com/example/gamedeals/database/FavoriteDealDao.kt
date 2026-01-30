package com.example.gamedeals.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDealDao {

    @Query("SELECT * FROM favorite_deals WHERE userEmail = :email")
    fun getAllFavorites(email: String): Flow<List<FavoriteDeal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(deal: FavoriteDeal)

    @Delete
    suspend fun removeFavorite(deal: FavoriteDeal)
}