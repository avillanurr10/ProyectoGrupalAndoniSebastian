package com.example.gamedeals.ui.deals

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (hasError || dealDetail == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error al cargar detalles", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { onBack() }, modifier = Modifier.padding(16.dp)) {
                    Text("Regresar")
                }
            }
        } else {
            val info = dealDetail!!.gameInfo
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // PARALLAX HEADER STYLE
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(info.thumb),
                            contentDescription = info.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 200f
                                    )
                                )
                        )
                        
                        // Back Button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(16.dp)
                                .statusBarsPadding()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Text(
                                text = info.title,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Hype Gauge
                                val savingsVal = ((1 - (info.salePrice.toFloatOrNull() ?: 1f) / (info.normalPrice.toFloatOrNull() ?: 1f)) * 100)
                                val metaVal = info.metacriticScore?.toIntOrNull() ?: 50
                                val hypeScore = (savingsVal * 0.6f + metaVal * 0.4f).coerceIn(0f, 100f)
                                
                                HypeGauge(score = hypeScore, modifier = Modifier.size(60.dp))
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Price
                                Column {
                                    Text(
                                        text = "$${info.salePrice}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Antes: $${info.normalPrice}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ACTION BUTTONS
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { uriHandler.openUri("https://www.cheapshark.com/redirect?dealID=$dealID") },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("COMPRAR", fontWeight = FontWeight.Bold)
                        }

                        // Share
                        val context = LocalContext.current
                        FilledTonalButton(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "¡ ${info.title} a $${info.salePrice}! https://www.cheapshark.com/redirect?dealID=$dealID")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                             modifier = Modifier.height(56.dp),
                             shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Share, null)
                        }

                        // Favorite
                        FilledTonalButton(
                             onClick = {
                                favoritesViewModel.addFavorite(
                                    FavoriteDeal(
                                        title = info.title,
                                        salePrice = info.salePrice,
                                        normalPrice = info.normalPrice,
                                        storeID = info.storeID,
                                        thumb = info.thumb,
                                        dealID = dealID,
                                        userEmail = ""
                                    )
                                )
                            },
                             modifier = Modifier.height(56.dp),
                             shape = RoundedCornerShape(16.dp)
                        ) {
                             Icon(Icons.Default.FavoriteBorder, null)
                        }
                         // Alert
                        FilledTonalButton(
                             onClick = { showAlertDialog = true },
                             modifier = Modifier.height(56.dp),
                             shape = RoundedCornerShape(16.dp)
                        ) {
                             Icon(Icons.Default.NotificationsNone, null)
                        }
                    }
                }
                
                // RATINGS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), 
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RatingCard(
                            label = "Metacritic",
                            score = info.metacriticScore ?: "N/A",
                            icon = Icons.Default.Stars,
                            modifier = Modifier.weight(1f)
                        )
                        RatingCard(
                            label = "Steam",
                            score = info.steamRatingPercent?.let { "$it%" } ?: "N/A",
                            icon = Icons.Default.ThumbUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (dealDetail!!.cheaperStores.isNotEmpty()) {
                    item {
                        Text(
                            "Otras Tiendas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
                        )
                    }
                    items(dealDetail!!.cheaperStores) { store ->
                        CheaperStoreItem(store, storeMap)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun RatingCard(label: String, score: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(score, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CheaperStoreItem(store: com.example.gamedeals.ui.deals.models.CheaperStore, storeMap: Map<String, String>) {
    val storeName = storeMap[store.storeID] ?: "Tienda ${store.storeID}"
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
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
