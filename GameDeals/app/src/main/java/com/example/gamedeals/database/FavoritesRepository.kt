package com.example.gamedeals.database

import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val favoriteDealDao: FavoriteDealDao) {

    val allFavorites: Flow<List<FavoriteDeal>> = favoriteDealDao.getAllFavorites()

    suspend fun addFavorite(favoriteDeal: FavoriteDeal) {
        favoriteDealDao.addFavorite(favoriteDeal)
    }

    suspend fun removeFavorite(favoriteDeal: FavoriteDeal) {
        favoriteDealDao.removeFavorite(favoriteDeal)
    }
}