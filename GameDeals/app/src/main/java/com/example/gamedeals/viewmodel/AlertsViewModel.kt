package com.example.gamedeals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedeals.database.AlertsRepository
import com.example.gamedeals.database.PriceAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlertsViewModel(private val repository: AlertsRepository) : ViewModel() {

    val alerts: Flow<List<PriceAlert>> = repository.allAlerts

    fun addAlert(
        gameTitle: String,
        targetPrice: Double,
        currentPrice: Double,
        dealID: String,
        thumb: String
    ) {
        viewModelScope.launch {
            val alert = PriceAlert(
                gameTitle = gameTitle,
                targetPrice = targetPrice,
                currentPriceAtSetting = currentPrice,
                dealID = dealID,
                thumb = thumb
            )
            repository.insert(alert)
        }
    }

    fun removeAlert(alert: PriceAlert) {
        viewModelScope.launch {
            repository.delete(alert)
        }
    }
}
