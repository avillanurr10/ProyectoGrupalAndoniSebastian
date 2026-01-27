package com.example.gamedeals.ui.deals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamedeals.ui.deals.components.DealCard
import com.example.gamedeals.ui.deals.components.FilterDialog
import com.example.gamedeals.ui.deals.components.SearchBar
import com.example.gamedeals.ui.deals.models.*
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.DealsViewModel
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    favoritesViewModel: FavoritesViewModel,
    dealsViewModel: DealsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    alertsViewModel: AlertsViewModel
) {
    // Estados de datos obtenidos del ViewModel
    val deals by dealsViewModel.deals.collectAsState()
    val storeMap by dealsViewModel.storeMap.collectAsState()
    val isLoading by dealsViewModel.isLoading.collectAsState()
    val hasError by dealsViewModel.hasError.collectAsState()

    // Estados de filtros (Restaurados)
    var searchQuery by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf(60f) }
    var selectedStores by remember { mutableStateOf<Set<String>>(emptySet()) }
    var minDiscount by remember { mutableStateOf(0f) }
    var sortOption by remember { mutableStateOf(SortOption.HIGHEST_DISCOUNT) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Aplicar filtros
    val filteredDeals = remember(deals, searchQuery, maxPrice, selectedStores, minDiscount, sortOption) {
        deals
            .filter { it.title.contains(searchQuery, ignoreCase = true) }
            .filter { it.salePrice.toFloatOrNull()?.let { price -> price <= maxPrice } ?: true }
            .filter { selectedStores.isEmpty() || it.storeID in selectedStores }
            .filter { (it.savings?.toFloatOrNull() ?: 0f) >= minDiscount }
            .let { filtered ->
                when (sortOption) {
                    SortOption.HIGHEST_DISCOUNT -> filtered.sortedByDescending { it.savings?.toFloatOrNull() ?: 0f }
                    SortOption.LOWEST_PRICE -> filtered.sortedBy { it.salePrice.toFloatOrNull() ?: Float.MAX_VALUE }
                    SortOption.ALPHABETICAL -> filtered.sortedBy { it.title }
                }
            }
    }

    val activeFiltersCount = listOfNotNull(
        if (searchQuery.isNotEmpty()) 1 else null,
        if (maxPrice < 60f) 1 else null,
        if (selectedStores.isNotEmpty()) 1 else null,
        if (minDiscount > 0f) 1 else null
    ).size

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra de búsqueda
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        // Controles de filtro y ordenamiento
        FilterControls(
            activeFiltersCount = activeFiltersCount,
            sortOption = sortOption,
            onFilterClick = { showFilterDialog = true },
            onSortChange = { sortOption = it },
            resultCount = filteredDeals.size
        )

        // Chips de filtros activos
        if (activeFiltersCount > 0) {
            ActiveFilterChips(
                maxPrice = maxPrice,
                minDiscount = minDiscount,
                selectedStoresCount = selectedStores.size,
                onClearMaxPrice = { maxPrice = 60f },
                onClearMinDiscount = { minDiscount = 0f },
                onClearStores = { selectedStores = emptySet() }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Contenido principal
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = isLoading) { loading ->
                if (loading && deals.isEmpty()) {
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
                            alertsViewModel = alertsViewModel
                        )
                    }
                }
            }
        }
    }

    // Diálogo de filtros
    if (showFilterDialog) {
        FilterDialog(
            maxPrice = maxPrice,
            onMaxPriceChange = { maxPrice = it },
            minDiscount = minDiscount,
            onMinDiscountChange = { minDiscount = it },
            availableStores = storeMap,
            selectedStores = selectedStores,
            onStoresChange = { selectedStores = it },
            onDismiss = { showFilterDialog = false },
            onClearAll = {
                maxPrice = 60f
                minDiscount = 0f
                selectedStores = emptySet()
            }
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
    selectedStoresCount: Int,
    onClearMaxPrice: () -> Unit,
    onClearMinDiscount: () -> Unit,
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
    deals: List<Deal>,
    storeMap: Map<String, String>,
    favoritesViewModel: FavoritesViewModel,
    alertsViewModel: AlertsViewModel
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
                    favoritesViewModel = favoritesViewModel,
                    alertsViewModel = alertsViewModel
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