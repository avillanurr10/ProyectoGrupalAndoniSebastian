package com.example.gamedeals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedeals.database.FavoriteDeal
import com.example.gamedeals.database.FavoritesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository,
    private val userEmail: String
) : ViewModel() {

    val favorites = repository.getFavorites(userEmail)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addFavorite(deal: FavoriteDeal) {
        viewModelScope.launch {
            // Aseguramos que se guarde con el email actual
            repository.addFavorite(deal.copy(userEmail = userEmail))
        }
    }

    fun removeFavorite(deal: FavoriteDeal) {
        viewModelScope.launch {
            repository.removeFavorite(deal)
        }
    }
}
