package com.example.gamedeals.database

import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val favoriteDealDao: FavoriteDealDao) {

    fun getFavorites(email: String): Flow<List<FavoriteDeal>> = favoriteDealDao.getAllFavorites(email)

    suspend fun addFavorite(favoriteDeal: FavoriteDeal) {
        favoriteDealDao.addFavorite(favoriteDeal)
    }

    suspend fun removeFavorite(favoriteDeal: FavoriteDeal) {
        favoriteDealDao.removeFavorite(favoriteDeal)
    }
}