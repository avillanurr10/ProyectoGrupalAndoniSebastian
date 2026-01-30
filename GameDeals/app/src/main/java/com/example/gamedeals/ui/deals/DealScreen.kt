package com.example.gamedeals.ui.deals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamedeals.R
import com.example.gamedeals.ui.deals.components.DealCard
import com.example.gamedeals.ui.deals.components.FilterBottomSheet
import com.example.gamedeals.ui.deals.components.PaginationControls
import com.example.gamedeals.ui.deals.components.SearchBar
import com.example.gamedeals.ui.deals.models.*
import com.example.gamedeals.viewmodel.AlertsViewModel
import com.example.gamedeals.viewmodel.DealsViewModel
import com.example.gamedeals.viewmodel.FavoritesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    favoritesViewModel: FavoritesViewModel,
    dealsViewModel: DealsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    alertsViewModel: AlertsViewModel,
    onDealClick: (String) -> Unit
) {
    val filteredDeals by dealsViewModel.filteredDeals.collectAsState()
    val storeMap by dealsViewModel.storeMap.collectAsState()
    val isLoading by dealsViewModel.isLoading.collectAsState()
    val hasError by dealsViewModel.hasError.collectAsState()
    val activeFiltersCount by dealsViewModel.activeFiltersCount.collectAsState()
    
    val currentPage by dealsViewModel.currentPage.collectAsState()
    val totalPages by dealsViewModel.totalPages.collectAsState()
    
    val searchQuery by dealsViewModel.searchQuery.collectAsState()
    val sortOption by dealsViewModel.sortOption.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isShuffling by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }

    // Scroll to top when page changes
    LaunchedEffect(currentPage) {
        listState.scrollToItem(0)
    }

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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (hasError && filteredDeals.isEmpty()) {
                    ErrorState(onRetry = { dealsViewModel.refreshData() })
                } else if (filteredDeals.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(filteredDeals, key = { _, deal -> "${deal.dealID}_${deal.storeID}" }) { _, deal ->
                            DealCard(
                                title = deal.title,
                                salePrice = deal.salePrice,
                                normalPrice = deal.normalPrice,
                                storeID = deal.storeID,
                                storeName = storeMap[deal.storeID] ?: stringResource(R.string.store),
                                thumb = deal.thumb,
                                savings = deal.savings,
                                dealID = deal.dealID,
                                metacriticScore = deal.metacriticScore,
                                favoritesViewModel = favoritesViewModel,
                                alertsViewModel = alertsViewModel,
                                onDealClick = onDealClick
                            )
                        }
                        
                        // Footer de paginación
                        item {
                            PaginationControls(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                onPageSelected = { dealsViewModel.jumpToPage(it) },
                                onPrev = { dealsViewModel.loadPreviousPage() },
                                onNext = { dealsViewModel.loadNextPage() }
                            )
                        }
                    }
                }
            }
        }

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
                .padding(bottom = 110.dp, end = 24.dp)
                .graphicsLayer(rotationZ = rotation.value),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (isShuffling) Icons.Default.Casino else Icons.Default.AutoAwesome,
                contentDescription = stringResource(R.string.lucky_deal),
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
            label = { 
                Text(
                    if (activeFiltersCount > 0) "${stringResource(R.string.filters_title)} ($activeFiltersCount)" 
                    else stringResource(R.string.filters_title)
                ) 
            },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
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
            text = stringResource(R.string.deals_count, resultCount),
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
                    label = { Text("$selectedStoresCount") }, // Simplified to avoid hardcoded string concat
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) }
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
            stringResource(R.string.error_connection),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.error_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.retry))
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
            stringResource(R.string.no_deals),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.no_deals_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}