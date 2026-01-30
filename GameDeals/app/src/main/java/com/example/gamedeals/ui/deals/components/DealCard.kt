package com.example.gamedeals.ui.deals.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.gamedeals.R
import com.example.gamedeals.database.FavoriteDeal
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DealCard(
    title: String,
    salePrice: String,
    normalPrice: String,
    storeID: String,
    storeName: String,
    thumb: String,
    savings: String?,
    dealID: String,
    metacriticScore: String?,
    favoritesViewModel: FavoritesViewModel,
    alertsViewModel: AlertsViewModel,
    onDealClick: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var showAlertDialog by remember { mutableStateOf(false) }
    val discountPercent = savings?.toFloatOrNull()?.toInt() ?: 0

    // Animation State
    var isFavorite by remember { mutableStateOf(false) } // This should ideally stay sync with DB, currently local visual
    var isLiked by remember { mutableStateOf(false) }
    
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = keyframes {
            durationMillis = 300
            0.8f at 100
            1.2f at 200
        }
    )
    
    val heartColor by animateColorAsState(
        targetValue = if (isLiked) Color.Red else MaterialTheme.colorScheme.tertiary,
        label = "HeartColor"
    )

    if (showAlertDialog) {
        PriceAlertDialog(
            gameTitle = title,
            currentPrice = salePrice,
            onDismiss = { showAlertDialog = false },
            onConfirm = { targetPrice ->
                alertsViewModel.addAlert(
                    gameTitle = title,
                    targetPrice = targetPrice,
                    currentPrice = salePrice.toDoubleOrNull() ?: 0.0,
                    dealID = dealID,
                    thumb = thumb
                )
                showAlertDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = { onDealClick(dealID) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(thumb),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.9f)
                                ),
                                startY = 100f
                            )
                        )
                )

                if (discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48))))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (metacriticScore != null) {
                    val score = metacriticScore.toIntOrNull() ?: 0
                    val scoreColor = when {
                        score >= 75 -> Color(0xFF4CAF50)
                        score >= 50 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Meta",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(
                                text = metacriticScore,
                                color = scoreColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 24.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = storeName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$$salePrice",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$$normalPrice",
                                fontSize = 14.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { showAlertDialog = true }) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = "Alerta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                favoritesViewModel.addFavorite(
                                    FavoriteDeal(
                                        title = title,
                                        salePrice = salePrice,
                                        normalPrice = normalPrice,
                                        storeID = storeID,
                                        thumb = thumb,
                                        dealID = dealID,
                                        userEmail = ""
                                    )
                                )
                                scope.launch {
                                    isLiked = true
                                    delay(400)
                                    isLiked = false
                                }
                            },
                            modifier = Modifier.scale(heartScale)
                        ) {
                            Icon(
                                if(isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = heartColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                         uriHandler.openUri("https://www.cheapshark.com/redirect?dealID=$dealID")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.view_deal), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
