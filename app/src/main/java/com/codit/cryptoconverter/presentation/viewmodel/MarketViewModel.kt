package com.codit.cryptoconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codit.cryptoconverter.di.AppContainer
import com.codit.cryptoconverter.domain.model.CoinPrice
import com.codit.cryptoconverter.domain.model.RefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MarketUiState(
    val allPrices: List<CoinPrice> = emptyList(),
    val visiblePrices: List<CoinPrice> = emptyList(),
    val query: String = "",
    val defaultCurrency: String = "USD",
    val isRefreshing: Boolean = false,
    val refreshProgress: Int = 0,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)

class MarketViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                container.observeMarketPrices(),
                container.observeDefaultCurrency(),
                _uiState
            ) { prices, defaultCurrency, state ->
                Triple(prices, defaultCurrency, state.query)
            }.catch { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }.collect { (prices, defaultCurrency, query) ->
                val filtered = if (query.isBlank()) prices
                else prices.filter {
                    it.coinCode.contains(query, ignoreCase = true) ||
                        it.coinName.contains(query, ignoreCase = true)
                }
                val sorted = filtered.sortedByDescending { it.prices[defaultCurrency] ?: Double.MIN_VALUE }
                _uiState.update {
                    it.copy(
                        allPrices = prices,
                        visiblePrices = sorted,
                        defaultCurrency = defaultCurrency,
                        isEmpty = prices.isEmpty()
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        // Re-filter synchronously using last allPrices
        val s = _uiState.value
        val filtered = if (query.isBlank()) s.allPrices
        else s.allPrices.filter {
            it.coinCode.contains(query, ignoreCase = true) ||
                it.coinName.contains(query, ignoreCase = true)
        }
        val sorted = filtered.sortedByDescending { it.prices[s.defaultCurrency] ?: Double.MIN_VALUE }
        _uiState.update { it.copy(visiblePrices = sorted) }
    }

    fun refresh() {
        viewModelScope.launch {
            container.refreshMarketData().collect { state ->
                when (state) {
                    RefreshState.Idle -> _uiState.update { it.copy(isRefreshing = false) }
                    is RefreshState.Refreshing -> _uiState.update {
                        it.copy(isRefreshing = true, refreshProgress = state.progress)
                    }
                    RefreshState.Success -> _uiState.update {
                        it.copy(isRefreshing = false, refreshProgress = 100)
                    }
                    is RefreshState.Error -> _uiState.update {
                        it.copy(isRefreshing = false, errorMessage = state.message)
                    }
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MarketViewModel(container) as T
        }
    }
}
