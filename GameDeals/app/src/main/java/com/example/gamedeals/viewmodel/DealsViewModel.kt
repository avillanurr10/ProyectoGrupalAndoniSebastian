package com.example.gamedeals.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedeals.ui.deals.models.Deal
import com.example.gamedeals.ui.deals.models.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DealsViewModel : ViewModel() {

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals: StateFlow<List<Deal>> = _deals.asStateFlow()

    private val _storeMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val storeMap: StateFlow<Map<String, String>> = _storeMap.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

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
}
