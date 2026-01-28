package com.example.gamedeals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedeals.ui.deals.models.Deal
import com.example.gamedeals.ui.deals.models.RetrofitClient
import com.example.gamedeals.ui.deals.models.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DealsViewModel : ViewModel() {

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    private val _storeMap = MutableStateFlow<Map<String, String>>(emptyMap())
    
    val storeMap: StateFlow<Map<String, String>> = _storeMap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    // --- FILTERS STATE ---
    val searchQuery = MutableStateFlow("")
    val maxPrice = MutableStateFlow(60f)
    val minDiscount = MutableStateFlow(0f)
    val minMetacritic = MutableStateFlow(0f)
    val selectedStores = MutableStateFlow<Set<String>>(emptySet())
    val sortOption = MutableStateFlow(SortOption.HIGHEST_DISCOUNT)

    // --- FILTERED DEALS ---
    val filteredDeals: StateFlow<List<Deal>> = combine(
        _deals, searchQuery, maxPrice, minDiscount, minMetacritic, selectedStores, sortOption
    ) { args ->
        val deals = args[0] as List<Deal>
        val query = args[1] as String
        val maxP = args[2] as Float
        val minD = args[3] as Float
        val minM = args[4] as Float
        val stores = args[5] as Set<String>
        val sort = args[6] as SortOption

        val filteredList = deals.asSequence()
            .filter { it.title.contains(query, ignoreCase = true) }
            .filter { it.salePrice.toFloatOrNull()?.let { p -> p <= maxP } ?: true }
            .filter { (it.savings?.toFloatOrNull() ?: 0f) >= minD }
            .filter { (it.metacriticScore?.toFloatOrNull() ?: 0f) >= minM }
            .filter { stores.isEmpty() || it.storeID in stores }
            .toList()
        
        val result: List<Deal> = when (sort) {
            SortOption.HIGHEST_DISCOUNT -> filteredList.sortedByDescending { it.savings?.toFloatOrNull() ?: 0f }
            SortOption.LOWEST_PRICE -> filteredList.sortedBy { it.salePrice.toFloatOrNull() ?: Float.MAX_VALUE }
            SortOption.ALPHABETICAL -> filteredList.sortedBy { it.title }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeFiltersCount: StateFlow<Int> = combine(
        searchQuery, maxPrice, minDiscount, minMetacritic, selectedStores
    ) { query, maxP, minD, minM, stores ->
        var count = 0
        if (query.isNotEmpty()) count++
        if (maxP < 60f) count++
        if (minD > 0f) count++
        if (minM > 0f) count++
        if (stores.isNotEmpty()) count++
        count
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        loadData()
    }

    fun loadData() {
        if (_deals.value.isNotEmpty() && _storeMap.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false
            try {
                withContext(Dispatchers.IO) {
                    val dealsResponse = RetrofitClient.api.getDeals()
                    val storesResponse = RetrofitClient.api.getStores()
                    
                    _deals.value = dealsResponse
                    _storeMap.value = storesResponse.associate { it.storeID to it.storeName }
                }
            } catch (e: Exception) {
                _hasError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        _deals.value = emptyList()
        _storeMap.value = emptyMap()
        loadData()
    }

    fun clearFilters() {
        searchQuery.value = ""
        maxPrice.value = 60f
        minDiscount.value = 0f
        minMetacritic.value = 0f
        selectedStores.value = emptySet()
    }

    fun getRandomDeal(): Deal? {
        val list = filteredDeals.value
        if (list.isEmpty()) return null
        return list.random()
    }
}
