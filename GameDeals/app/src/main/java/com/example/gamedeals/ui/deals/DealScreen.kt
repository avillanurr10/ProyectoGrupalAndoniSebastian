package com.example.gamedeals.ui.deals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamedeals.ui.deals.components.DealCard
import com.example.gamedeals.ui.deals.components.FilterBottomSheet
import com.example.gamedeals.ui.deals.components.SearchBar
import com.example.gamedeals.ui.deals.models.*
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.DealsViewModel
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Definiciones de datos integradas de ramaSebas
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    favoritesViewModel: FavoritesViewModel,
    dealsViewModel: DealsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    alertsViewModel: AlertsViewModel,
    onDealClick: (String) -> Unit
) {
    // Estados del ViewModel (Main)
    val filteredDeals by dealsViewModel.filteredDeals.collectAsState()
    val storeMap by dealsViewModel.storeMap.collectAsState()
    val isLoading by dealsViewModel.isLoading.collectAsState()
    val hasError by dealsViewModel.hasError.collectAsState()
    val activeFiltersCount by dealsViewModel.activeFiltersCount.collectAsState()
    
    // Estados de UI
    val searchQuery by dealsViewModel.searchQuery.collectAsState()
    val sortOption by dealsViewModel.sortOption.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // Estado para animación de la suerte
    val scope = rememberCoroutineScope()
    var isShuffling by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { dealsViewModel.searchQuery.value = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            FilterControls(
                activeFiltersCount = activeFiltersCount,
                sortOption = sortOption,
                onFilterClick = { showFilterSheet = true },
                onSortChange = { dealsViewModel.sortOption.value = it },
                resultCount = filteredDeals.size
            )

            if (activeFiltersCount > 0) {
                val maxPrice by dealsViewModel.maxPrice.collectAsState()
                val minDiscount by dealsViewModel.minDiscount.collectAsState()
                val selectedStores by dealsViewModel.selectedStores.collectAsState()
                val minMetacritic by dealsViewModel.minMetacritic.collectAsState()

                ActiveFilterChips(
                    maxPrice = maxPrice,
                    minDiscount = minDiscount,
                    minMetacritic = minMetacritic,
                    selectedStoresCount = selectedStores.size,
                    onClearMaxPrice = { dealsViewModel.maxPrice.value = 60f },
                    onClearMinDiscount = { dealsViewModel.minDiscount.value = 0f },
                    onClearMinMetacritic = { dealsViewModel.minMetacritic.value = 0f },
                    onClearStores = { dealsViewModel.selectedStores.value = emptySet() }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(targetState = isLoading) { loading ->
                    if (loading && filteredDeals.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        when {
                            hasError -> ErrorState(onRetry = { dealsViewModel.refreshData() })
                            filteredDeals.isEmpty() -> EmptyState()
                            else -> DealsList(
                                deals = filteredDeals,
                                storeMap = storeMap,
                                favoritesViewModel = favoritesViewModel,
                                alertsViewModel = alertsViewModel,
                                onDealClick = onDealClick
                            )
                        }
                    }
                }
            }
        }

        // --- BOTÓN DE LA SUERTE ---
        FloatingActionButton(
            onClick = {
                if (!isShuffling) {
                    scope.launch {
                        isShuffling = true
                        rotation.animateTo(
                            targetValue = rotation.value + 360f,
                            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
                        )
                        val luckyDeal = dealsViewModel.getRandomDeal()
                        if (luckyDeal != null) {
                            onDealClick(luckyDeal.dealID)
                        }
                        isShuffling = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .graphicsLayer(rotationZ = rotation.value),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = if (isShuffling) Icons.Default.Casino else Icons.Default.AutoAwesome,
                contentDescription = "Botón de la suerte",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showFilterSheet) {
        val maxPrice by dealsViewModel.maxPrice.collectAsState()
        val minDiscount by dealsViewModel.minDiscount.collectAsState()
        val minMetacritic by dealsViewModel.minMetacritic.collectAsState()
        val selectedStores by dealsViewModel.selectedStores.collectAsState()

        FilterBottomSheet(
            maxPrice = maxPrice,
            onMaxPriceChange = { dealsViewModel.maxPrice.value = it },
            minDiscount = minDiscount,
            onMinDiscountChange = { dealsViewModel.minDiscount.value = it },
            minMetacritic = minMetacritic,
            onMinMetacriticChange = { dealsViewModel.minMetacritic.value = it },
            availableStores = storeMap,
            selectedStores = selectedStores,
            onStoresChange = { dealsViewModel.selectedStores.value = it },
            onDismiss = { showFilterSheet = false },
            onClearAll = { dealsViewModel.clearFilters() }
        )
    }
}

@Composable
private fun FilterControls(
    activeFiltersCount: Int,
    sortOption: SortOption,
    onFilterClick: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    resultCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = activeFiltersCount > 0,
            onClick = onFilterClick,
            label = { Text(if (activeFiltersCount > 0) "Filtros ($activeFiltersCount)" else "Filtros") },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
        )

        var expandedSort by remember { mutableStateOf(false) }
        FilterChip(
            selected = false,
            onClick = { expandedSort = true },
            label = { Text(sortOption.label) },
            leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null) }
        )
        DropdownMenu(
            expanded = expandedSort,
            onDismissRequest = { expandedSort = false }
        ) {
            SortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSortChange(option)
                        expandedSort = false
                    },
                    leadingIcon = {
                        if (sortOption == option) Icon(Icons.Default.Check, contentDescription = null)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "$resultCount ofertas",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ActiveFilterChips(
    maxPrice: Float,
    minDiscount: Float,
    minMetacritic: Float,
    selectedStoresCount: Int,
    onClearMaxPrice: () -> Unit,
    onClearMinDiscount: () -> Unit,
    onClearMinMetacritic: () -> Unit,
    onClearStores: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (maxPrice < 60f) {
            item {
                AssistChip(
                    onClick = onClearMaxPrice,
                    label = { Text("Max: $${maxPrice.toInt()}") },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                )
            }
        }
        if (minDiscount > 0f) {
            item {
                AssistChip(
                    onClick = onClearMinDiscount,
                    label = { Text("Desc: ${minDiscount.toInt()}%+") },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                )
            }
        }
        if (minMetacritic > 0f) {
            item {
                AssistChip(
                    onClick = onClearMinMetacritic,
                    label = { Text("Meta: ${minMetacritic.toInt()}+") },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                )
            }
        }
        if (selectedStoresCount > 0) {
            item {
                AssistChip(
                    onClick = onClearStores,
                    label = { Text("$selectedStoresCount tiendas") },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
                )
            }
        }
    }
}

@Composable
private fun DealsList(
    deals: List<DealModel>,
    storeMap: Map<String, String>,
    favoritesViewModel: FavoritesViewModel,
    alertsViewModel: AlertsViewModel,
    onDealClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(deals, key = { _, deal -> deal.dealID }) { index, deal ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, delayMillis = (index % 10) * 50)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300, delayMillis = (index % 10) * 50)
                        )
            ) {
                DealCard(
                    title = deal.title,
                    salePrice = deal.salePrice,
                    normalPrice = deal.normalPrice,
                    storeID = deal.storeID,
                    storeName = storeMap[deal.storeID] ?: "Tienda ${deal.storeID}",
                    thumb = deal.thumb,
                    savings = deal.savings,
                    dealID = deal.dealID,
                    metacriticScore = deal.metacriticScore,
                    favoritesViewModel = favoritesViewModel,
                    alertsViewModel = alertsViewModel,
                    onDealClick = onDealClick
                )
            }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Error de conexión",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "No se pudieron cargar las ofertas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No se encontraron ofertas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Intenta ajustar los filtros de búsqueda",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}