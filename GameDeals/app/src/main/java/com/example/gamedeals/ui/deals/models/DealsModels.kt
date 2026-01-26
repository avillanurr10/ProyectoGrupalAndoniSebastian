package com.example.gamedeals.ui.deals.models

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- DATA MODELS ---
data class Deal(
    val title: String,
    val salePrice: String,
    val normalPrice: String,
    val storeID: String,
    val thumb: String,
    val savings: String? = null,
    val dealID: String
)

data class Store(
    val storeID: String,
    val storeName: String,
    val isActive: Int
)

// --- API INTERFACE ---
interface CheapSharkApi {
    @GET("deals")
    suspend fun getDeals(
        @Query("upperPrice") upperPrice: String? = null,
        @Query("pageSize") pageSize: Int = 60
    ): List<Deal>

    @GET("stores")
    suspend fun getStores(): List<Store>
}

// --- RETROFIT CLIENT ---
object RetrofitClient {
    val api: CheapSharkApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.cheapshark.com/api/1.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CheapSharkApi::class.java)
    }
}

// --- ENUMS ---
enum class SortOption(val label: String) {
    HIGHEST_DISCOUNT("Mayor Descuento"),
    LOWEST_PRICE("Menor Precio"),
    ALPHABETICAL("A-Z")
}
