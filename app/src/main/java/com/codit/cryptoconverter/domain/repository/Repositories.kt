package com.codit.cryptoconverter.domain.repository

import com.codit.cryptoconverter.domain.model.CoinPrice
import com.codit.cryptoconverter.domain.model.Currency
import com.codit.cryptoconverter.domain.model.FavoritePair
import com.codit.cryptoconverter.domain.model.RefreshState
import kotlinx.coroutines.flow.Flow

interface MarketRepository {
    fun observeCoinPrices(): Flow<List<CoinPrice>>
    suspend fun getCoinPrice(coinCode: String): CoinPrice?
    fun refreshMarketData(): Flow<RefreshState>
    suspend fun isCacheEmpty(): Boolean
}

interface CurrencyRepository {
    fun getAllCurrencies(): List<Currency>
    fun getCryptoCurrencies(): List<Currency>
    fun getFiatCurrencies(): List<Currency>
    fun getCurrencyName(code: String): String
    fun isFiat(code: String): Boolean
    fun isCrypto(code: String): Boolean
}

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<FavoritePair>>
    suspend fun addFavorite(from: String, to: String)
    suspend fun removeFavorite(from: String, to: String)
    fun observeIsFavorite(from: String, to: String): Flow<Boolean>
}

interface SettingsRepository {
    val defaultCurrency: Flow<String>
    suspend fun setDefaultCurrency(code: String)
}
