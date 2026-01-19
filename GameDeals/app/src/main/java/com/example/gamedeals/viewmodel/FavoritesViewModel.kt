package com.example.gamedeals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedeals.database.FavoriteDeal
import com.example.gamedeals.database.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {

    val favorites = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addFavorite(deal: FavoriteDeal) {
        viewModelScope.launch {
            repository.addFavorite(deal)
        }
    }

    fun removeFavorite(deal: FavoriteDeal) {
        viewModelScope.launch {
            repository.removeFavorite(deal)
        }
    }
}
