package com.example.gamedeals.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDealDao {

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteDeal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(deal: FavoriteDeal)

    @Delete
    suspend fun deleteFavorite(deal: FavoriteDeal)
}
