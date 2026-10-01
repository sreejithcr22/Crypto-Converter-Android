package com.codit.cryptoconverter.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * CoinGecko Keyless Public API (no key, ~10-30 calls/min shared pool).
 * Used as the primary price source; CryptoCompare remains as fallback.
 * Docs: https://docs.coingecko.com/docs/keyless-public-api
 */
interface CoinGeckoApiService {

    @GET("simple/price")
    suspend fun getPrices(
        @Query("symbols") symbols: String,
        @Query("vs_currencies") vsCurrencies: String,
        @Query("precision") precision: String = "full"
    ): Map<String, Map<String, Double>>

    @GET("exchange_rates")
    suspend fun getExchangeRates(): ExchangeRatesResponse
}

data class ExchangeRatesResponse(
    val rates: Map<String, ExchangeRateEntry> = emptyMap()
)

data class ExchangeRateEntry(
    val name: String = "",
    val unit: String = "",
    val value: Double = 0.0,
    val type: String = ""
)
