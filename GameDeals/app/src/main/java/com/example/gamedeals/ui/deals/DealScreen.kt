package com.example.gamedeals.ui.deals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.gamedeals.database.FavoriteDeal
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class Deal(
    val title: String,
    val salePrice: String,
    val normalPrice: String,
    val storeID: String,
    val thumb: String
)
typealias DealsResponse = List<Deal>

interface CheapSharkApi {
    @GET("deals")
    suspend fun getDeals(
        @Query("storeID") storeID: String? = null,
        @Query("upperPrice") upperPrice: String? = null,
        @Query("pageSize") pageSize: Int = 20
    ): DealsResponse
}

private val retrofit = Retrofit.Builder()
    .baseUrl("https://www.cheapshark.com/api/1.0/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val api = retrofit.create(CheapSharkApi::class.java)

@Composable
fun DealsScreen(viewModel: FavoritesViewModel) {
    var deals by remember { mutableStateOf<List<Deal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            deals = withContext(Dispatchers.IO) { api.getDeals() }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(deals) { deal ->
                DealCard(deal, viewModel)
            }
        }
    }
}

// Aquí va DealCard
@Composable
fun DealCard(deal: Deal, viewModel: FavoritesViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(deal.thumb),
                contentDescription = deal.title,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(deal.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Oferta: $${deal.salePrice}", color = MaterialTheme.colorScheme.primary)
                Text("Normal: $${deal.normalPrice}", fontSize = 12.sp)
                Text("Tienda: ${deal.storeID}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    viewModel.addFavorite(
                        FavoriteDeal(
                            title = deal.title,
                            salePrice = deal.salePrice,
                            normalPrice = deal.normalPrice,
                            storeID = deal.storeID,
                            thumb = deal.thumb
                        )
                    )
                }) {
                    Text("❤️ Favorito")
                }
            }
        }
    }
}
