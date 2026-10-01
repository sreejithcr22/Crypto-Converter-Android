package com.codit.cryptoconverter.data.remote

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApiService {
    @GET("data/pricemulti")
    suspend fun getAllCoinPrices(
        @Query("fsyms") fromSymbols: String,
        @Query("tsyms") toSymbols: String,
        @Query("api_key") apiKey: String? = null
    ): JsonElement
}
