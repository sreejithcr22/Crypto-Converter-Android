package com.codit.cryptoconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codit.cryptoconverter.di.AppContainer
import com.codit.cryptoconverter.domain.model.Currency
import com.codit.cryptoconverter.domain.model.RefreshState
import com.codit.cryptoconverter.domain.usecase.ConversionResult
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConverterUiState(
    val fromCurrency: String = "BTC",
    val toCurrency: String = "USD",
    val inputExpression: String = "",
    val evaluatedInput: String = "",
    val outputText: String = "",
    val outputAvailable: Boolean = false,
    val isFavorite: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshProgress: Int = 0,
    val errorMessage: String? = null,
    val currencies: List<Currency> = emptyList(),
    val favoritesCount: Int = 0
)

class ConverterViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    // Raw price map for conversion: coin -> (currency -> price)
    private val pricesMap = MutableStateFlow<Map<String, Map<String, Double>>>(emptyMap())

    // Favorites list for dialog
    private val _favorites = MutableStateFlow<List<com.codit.cryptoconverter.domain.model.FavoritePair>>(emptyList())
    val favorites: StateFlow<List<com.codit.cryptoconverter.domain.model.FavoritePair>> = _favorites.asStateFlow()

    init {
        _uiState.update { it.copy(currencies = container.getCurrencies.all()) }
        observePrices()
        observeFavorites()
        // Initial auto-refresh if DB empty
        viewModelScope.launch {
            try {
                if (container.marketRepository.isCacheEmpty()) refresh()
            } catch (_: Exception) {
            }
        }
    }

    private fun observePrices() {
        viewModelScope.launch {
            container.observeMarketPrices()
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
                .collect { prices ->
                    pricesMap.value = prices.associate { it.coinCode to it.prices }
                    recomputeOutput()
                }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            container.observeFavorites().collect { favs ->
                _favorites.value = favs
                _uiState.update { it.copy(favoritesCount = favs.size) }
                updateIsFavorite()
            }
        }
        // Also observe isFavorite for current pair explicitly
        viewModelScope.launch {
            combine(
                _uiState,
                _favorites
            ) { state, favs ->
                favs.any { pair ->
                    (pair.fromCurrency == state.fromCurrency && pair.toCurrency == state.toCurrency) ||
                        (pair.fromCurrency == state.toCurrency && pair.toCurrency == state.fromCurrency)
                }
            }.collect { fav ->
                _uiState.update { it.copy(isFavorite = fav) }
            }
        }
    }

    private fun updateIsFavorite() {
        val s = _uiState.value
        val fav = _favorites.value.any { pair ->
            (pair.fromCurrency == s.fromCurrency && pair.toCurrency == s.toCurrency) ||
                (pair.fromCurrency == s.toCurrency && pair.toCurrency == s.fromCurrency)
        }
        _uiState.update { it.copy(isFavorite = fav) }
    }

    // ---------- Calculator / input (ports legacy Calculator + ConverterFragment) ----------
    fun onDigit(d: Char) {
        val cur = _uiState.value.inputExpression
        // Prevent multiple dots in current number segment
        if (d == '.') {
            val segment = cur.substringAfterLast('+').substringAfterLast('-')
                .substringAfterLast('*').substringAfterLast('÷')
            if (segment.contains('.')) return
            if (segment.isEmpty()) {
                updateInput(cur + "0.")
                return
            }
        }
        updateInput(cur + d)
    }

    fun onOperator(op: String) {
        val cur = _uiState.value.inputExpression
        if (cur.isEmpty()) return
        val last = cur.last().toString()
        if (last in listOf("+", "-", "*", "÷")) {
            updateInput(cur.dropLast(1) + op)
        } else {
            updateInput(cur + op)
        }
    }

    fun onClear() = updateInput("")

    fun onBackspace() {
        val cur = _uiState.value.inputExpression
        if (cur.isNotEmpty()) updateInput(cur.dropLast(1))
    }

    fun onEquals() {
        // Evaluate and collapse expression to result (like legacy printResult)
        val evaluated = evaluateExpression(_uiState.value.inputExpression)
        if (evaluated != null) {
            updateInput(evaluated.toPlainString())
        }
    }

    private fun updateInput(newExpression: String) {
        _uiState.update { it.copy(inputExpression = newExpression) }
        recomputeOutput()
    }

    fun onSwap() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                fromCurrency = s.toCurrency,
                toCurrency = s.fromCurrency
            )
        }
        updateIsFavorite()
        recomputeOutput()
    }

    fun onSelectCurrency(slot: Int, code: String) {
        val s = _uiState.value
        val newFrom = if (slot == 1) code else s.fromCurrency
        val newTo = if (slot == 2) code else s.toCurrency
        // Reject fiat-fiat like legacy (both_fiat_currency toast -> error message)
        val isFromFiat = container.currencyRepository.isFiat(newFrom)
        val isToFiat = container.currencyRepository.isFiat(newTo)
        if (isFromFiat && isToFiat) {
            _uiState.update { it.copy(errorMessage = "Fiat to fiat conversion is not supported") }
            return
        }
        if (newFrom == newTo) {
            _uiState.update { it.copy(errorMessage = "Currencies must be different") }
            return
        }
        _uiState.update {
            it.copy(fromCurrency = newFrom, toCurrency = newTo, errorMessage = null)
        }
        updateIsFavorite()
        recomputeOutput()
    }

    fun onApplyFavorite(from: String, to: String) {
        onSelectCurrency(1, from)
        onSelectCurrency(2, to)
        _uiState.update { it.copy(inputExpression = "", outputText = "", outputAvailable = false) }
    }

    fun onToggleFavorite() {
        val s = _uiState.value
        viewModelScope.launch {
            try {
                container.toggleFavorite.toggle(s.fromCurrency, s.toCurrency, s.isFavorite)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onRemoveFavorite(from: String, to: String) {
        viewModelScope.launch {
            try {
                container.favoritesRepository.removeFavorite(from, to)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun recomputeOutput() {
        val s = _uiState.value
        val evaluated = evaluateExpression(s.inputExpression)
        val evaluatedText = evaluated?.toPlainString() ?: ""
        val result = if (evaluated == null) {
            ConversionResult.NotAvailable
        } else {
            container.convertCurrency(evaluated, s.fromCurrency, s.toCurrency, pricesMap.value)
        }
        when (result) {
            is ConversionResult.Success -> _uiState.update {
                it.copy(
                    evaluatedInput = evaluatedText,
                    outputText = result.outputText,
                    outputAvailable = true
                )
            }
            ConversionResult.NotAvailable -> _uiState.update {
                it.copy(
                    evaluatedInput = evaluatedText,
                    outputText = if (s.inputExpression.isEmpty()) "" else "Not available",
                    outputAvailable = false
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            container.refreshMarketData().collect { state ->
                when (state) {
                    RefreshState.Idle -> _uiState.update {
                        it.copy(isRefreshing = false)
                    }
                    is RefreshState.Refreshing -> _uiState.update {
                        it.copy(isRefreshing = true, refreshProgress = state.progress)
                    }
                    RefreshState.Success -> {
                        _uiState.update { it.copy(isRefreshing = false, refreshProgress = 100) }
                        recomputeOutput()
                    }
                    is RefreshState.Error -> _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorMessage = state.message
                        )
                    }
                }
            }
        }
    }

    // ---- Expression evaluation (supports + - * ÷, left-to-right with standard precedence) ----
    internal fun evaluateExpression(expr: String): BigDecimal? {
        if (expr.isEmpty()) return null
        return try {
            // Tokenize numbers and ops
            val tokens = mutableListOf<String>()
            val buf = StringBuilder()
            for (c in expr) {
                if (c.isDigit() || c == '.') {
                    buf.append(c)
                } else if (c == '+' || c == '-' || c == '*' || c == '÷') {
                    if (buf.isNotEmpty()) {
                        tokens += buf.toString()
                        buf.clear()
                    }
                    tokens += c.toString()
                }
            }
            if (buf.isNotEmpty()) tokens += buf.toString()
            if (tokens.isEmpty()) return null
            if (tokens.size == 1) return tokens[0].toBigDecimalOrNull()

            // First pass: * and ÷
            val first = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val t = tokens[i]
                if ((t == "*" || t == "÷") && first.isNotEmpty() && i + 1 < tokens.size) {
                    val left = first.removeLast().toBigDecimal()
                    val right = tokens[i + 1].toBigDecimal()
                    val res = if (t == "*") left.multiply(right)
                    else {
                        if (right.compareTo(BigDecimal.ZERO) == 0) return null
                        left.divide(right, 8, RoundingMode.HALF_EVEN)
                    }
                    first += res.toPlainString()
                    i += 2
                } else {
                    first += t
                    i++
                }
            }
            // Second pass: + and -
            var acc = first[0].toBigDecimal()
            var j = 1
            while (j < first.size) {
                val op = first[j]
                val right = first[j + 1].toBigDecimal()
                acc = if (op == "+") acc.add(right) else acc.subtract(right)
                j += 2
            }
            acc
        } catch (_: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConverterViewModel(container) as T
        }
    }
}
