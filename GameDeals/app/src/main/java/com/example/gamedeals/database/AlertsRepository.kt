package com.example.gamedeals.database

import kotlinx.coroutines.flow.Flow

class AlertsRepository(private val priceAlertDao: PriceAlertDao) {
    val allAlerts: Flow<List<PriceAlert>> = priceAlertDao.getAllAlerts()

    suspend fun insert(alert: PriceAlert) {
        priceAlertDao.insertAlert(alert)
    }

    suspend fun delete(alert: PriceAlert) {
        priceAlertDao.deleteAlert(alert)
    }

    suspend fun hasAlert(dealID: String): Boolean {
        return priceAlertDao.hasAlert(dealID)
    }
}
