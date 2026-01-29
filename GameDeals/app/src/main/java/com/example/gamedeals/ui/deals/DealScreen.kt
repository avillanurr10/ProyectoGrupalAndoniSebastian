package com.example.gamedeals.ui.deals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.gamedeals.database.FavoriteDeal
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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
    val thumb: String,
    val savings: String? = null,
    val dealID: String
)

data class Store(
    val storeID: String,
    val storeName: String,
    val isActive: Int
)

interface CheapSharkApi {
    @GET("deals")
    suspend fun getDeals(
        @Query("upperPrice") upperPrice: String? = null,
        @Query("pageSize") pageSize: Int = 30
    ): List<Deal>

    @GET("stores")
    suspend fun getStores(): List<Store>
}

object RetrofitClient {
    val api: CheapSharkApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.cheapshark.com/api/1.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CheapSharkApi::class.java)
    }
}

// --- PANTALLA PRINCIPAL ---
@Composable
fun DealsScreen(viewModel: FavoritesViewModel) {
    var deals by remember { mutableStateOf<List<Deal>>(emptyList()) }
    var storeMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Función para cargar datos de ofertas y tiendas en paralelo
    fun loadData() {
        scope.launch {
            isLoading = true
            hasError = false
            try {
                withContext(Dispatchers.IO) {
                    val dealsDeferred = async { RetrofitClient.api.getDeals() }
                    val storesDeferred = async { RetrofitClient.api.getStores() }

                    deals = dealsDeferred.await()
                    storeMap = storesDeferred.await().associate { it.storeID to it.storeName }
                }
            } catch (e: Exception) {
                hasError = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            hasError -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error de conexión", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deals) { deal ->
                        val storeName = storeMap[deal.storeID] ?: "Tienda ${deal.storeID}"
                        DealCardImproved(deal, viewModel, storeName)
                    }
                }
            }
        }
    }
}

// --- COMPONENTE DE TARJETA MEJORADO ---
@Composable
fun DealCardImproved(deal: Deal, viewModel: FavoritesViewModel, storeName: String) {
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(deal.thumb),
                    contentDescription = deal.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${deal.salePrice}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${deal.normalPrice}",
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "Tienda: $storeName",
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para añadir a favoritos
                OutlinedButton(
                    onClick = {
                        viewModel.addFavorite(
                            FavoriteDeal(
                                title = deal.title,
                                salePrice = deal.salePrice,
                                normalPrice = deal.normalPrice,
                                storeID = deal.storeID,
                                thumb = deal.thumb,
                                userEmail = "" // ViewModel se encarga de asignar el usuario correcto
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Favorito")
                }

                // Botón para ir a la web de la oferta
                Button(
                    onClick = {
                        uriHandler.openUri("https://www.cheapshark.com/redirect?dealID=${deal.dealID}")
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver Oferta 🌐")
                }
            }
        }
    }
}