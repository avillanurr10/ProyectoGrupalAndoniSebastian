package com.example.gamedeals.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY dateSet DESC")
    fun getAllAlerts(): Flow<List<PriceAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert)

    @Delete
    suspend fun deleteAlert(alert: PriceAlert)

    @Query("SELECT EXISTS(SELECT 1 FROM price_alerts WHERE dealID = :dealID)")
    suspend fun hasAlert(dealID: String): Boolean
}
