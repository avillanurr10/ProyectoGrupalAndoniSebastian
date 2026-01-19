package com.example.gamedeals.database

import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val dao: FavoriteDealDao) {

    val allFavorites: Flow<List<FavoriteDeal>> = dao.getAllFavorites()

    suspend fun addFavorite(deal: FavoriteDeal) {
        dao.insertFavorite(deal)
    }

    suspend fun removeFavorite(deal: FavoriteDeal) {
        dao.deleteFavorite(deal)
    }
}
