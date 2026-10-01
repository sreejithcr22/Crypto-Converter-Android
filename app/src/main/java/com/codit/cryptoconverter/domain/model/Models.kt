package com.codit.cryptoconverter.domain.model

enum class CurrencyType { CRYPTO, FIAT }

data class Currency(
    val code: String,
    val name: String,
    val type: CurrencyType
)

data class CoinPrice(
    val coinCode: String,
    val coinName: String,
    val prices: Map<String, Double> = emptyMap()
) {
    fun priceIn(currencyCode: String): Double? = prices[currencyCode]
}

data class FavoritePair(
    val fromCurrency: String,
    val toCurrency: String
)

sealed interface RefreshState {
    data object Idle : RefreshState
    data class Refreshing(val progress: Int) : RefreshState
    data object Success : RefreshState
    data class Error(val message: String) : RefreshState
}
