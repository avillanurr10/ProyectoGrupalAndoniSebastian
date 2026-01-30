package com.example.gamedeals.ui.deals

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import android.content.Intent
import com.example.gamedeals.database.FavoriteDeal
import com.example.gamedeals.ui.deals.components.HypeGauge
import com.example.gamedeals.ui.deals.components.PriceAlertDialog
import com.example.gamedeals.ui.deals.models.DealDetail
import com.example.gamedeals.ui.deals.models.RetrofitClient
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    dealID: String,
    storeMap: Map<String, String>,
    favoritesViewModel: FavoritesViewModel,
    alertsViewModel: AlertsViewModel,
    onBack: () -> Unit
) {
    var dealDetail by remember { mutableStateOf<DealDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(dealID) {
        isLoading = true
        try {
            dealDetail = RetrofitClient.api.getDealLookup(dealID)
            hasError = false
        } catch (e: Exception) {
            hasError = true
        } finally {
            isLoading = false
        }
    }

    if (showAlertDialog && dealDetail != null) {
        val info = dealDetail!!.gameInfo
        PriceAlertDialog(
            gameTitle = info.title,
            currentPrice = info.salePrice,
            onDismiss = { showAlertDialog = false },
            onConfirm = { targetPrice ->
                alertsViewModel.addAlert(
                    gameTitle = info.title,
                    targetPrice = targetPrice,
                    currentPrice = info.salePrice.toDoubleOrNull() ?: 0.0,
                    dealID = dealID,
                    thumb = info.thumb
                )
                showAlertDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Juego") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "¡Mira este juegazo! ${dealDetail?.gameInfo?.title} por solo $${dealDetail?.gameInfo?.salePrice} en CheapShark: https://www.cheapshark.com/redirect?dealID=$dealID")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (hasError || dealDetail == null) {
                // ... (Error state remains same)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Text("Error al cargar detalles", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { onBack() }, modifier = Modifier.padding(16.dp)) {
                        Text("Regresar")
                    }
                }
            } else {
                val info = dealDetail!!.gameInfo
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Header con imagen grande y HypeGauge
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(info.thumb),
                                contentDescription = info.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            
                            Row(
                                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = info.title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                // --- HYPE GAUGE ---
                                val savingsVal = ((1 - (info.salePrice.toFloatOrNull() ?: 1f) / (info.normalPrice.toFloatOrNull() ?: 1f)) * 100)
                                val metaVal = info.metacriticScore?.toIntOrNull() ?: 50
                                val hypeScore = (savingsVal * 0.6f + metaVal * 0.4f).coerceIn(0f, 100f)
                                
                                HypeGauge(
                                    score = hypeScore,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    // Información de precio y Trend visual
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            tonalElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "$${info.salePrice}",
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "Antes: $${info.normalPrice}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textDecoration = TextDecoration.LineThrough,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Row {
                                        IconButton(onClick = {
                                            favoritesViewModel.addFavorite(
                                                FavoriteDeal(
                                                    title = info.title,
                                                    salePrice = info.salePrice,
                                                    normalPrice = info.normalPrice,
                                                    storeID = info.storeID,
                                                    thumb = info.thumb,
                                                    dealID = dealID
                                                )
                                            )
                                        }) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = Color(0xFFE91E63))
                                        }
                                        IconButton(onClick = { showAlertDialog = true }) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alerta", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // --- PRICE TREND INDICATOR ---
                                val savingsPercent = ((1 - (info.salePrice.toFloatOrNull() ?: 1f) / (info.normalPrice.toFloatOrNull() ?: 1f)) * 100).toInt()
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Ahorro", style = MaterialTheme.typography.labelMedium)
                                        Text("${savingsPercent}%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = (savingsPercent / 100f).coerceIn(0f, 1f),
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Button(
                                    onClick = { uriHandler.openUri("https://www.cheapshark.com/redirect?dealID=$dealID") },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("¡COMPRAR AHORA!", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }

                    // Ratings con nuevo estilo
                    item {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Puntuaciones y Críticas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                RatingCard(
                                    label = "Metacritic",
                                    score = info.metacriticScore ?: "N/A",
                                    icon = Icons.Default.Stars,
                                    modifier = Modifier.weight(1f)
                                )
                                RatingCard(
                                    label = "Steam User",
                                    score = info.steamRatingPercent?.let { "$it%" } ?: "N/A",
                                    icon = Icons.Default.ThumbUp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Comparativa con otras tiendas
                    if (dealDetail!!.cheaperStores.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text("Comparativa de precios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        
                        items(dealDetail!!.cheaperStores) { store ->
                            CheaperStoreItem(store, storeMap)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingCard(label: String, score: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(score, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun CheaperStoreItem(store: com.example.gamedeals.ui.deals.models.CheaperStore, storeMap: Map<String, String>) {
    val storeName = storeMap[store.storeID] ?: "Tienda ${store.storeID}"
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(storeName, fontWeight = FontWeight.Medium)
            Text(
                text = "$${store.salePrice}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
