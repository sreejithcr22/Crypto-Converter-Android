package com.codit.cryptoconverter.data.local

import com.codit.cryptoconverter.domain.model.CoinPrice
import com.codit.cryptoconverter.domain.model.FavoritePair
import com.codit.cryptoconverter.model.CoinPrices
import com.codit.cryptoconverter.model.FavouritePair
import com.codit.cryptoconverter.util.CryptoCurrency
import com.google.gson.Gson

private val gson = Gson()

fun CoinPrices.toDomain(): CoinPrice {
    val map: Map<String, Double> = try {
        val parsed: Map<String, Double>? = gson.fromJson(
            jsonPricesString,
            object : com.google.gson.reflect.TypeToken<HashMap<String, Double>>() {}.type
        )
        parsed ?: emptyMap()
    } catch (_: Exception) {
        emptyMap()
    }
    return CoinPrice(
        coinCode = coinCode,
        coinName = CryptoCurrency.getCoinName(coinCode) ?: coinCode,
        prices = map
    )
}

fun CoinPrice.toEntity(): CoinPrices = CoinPrices(coinCode, HashMap(prices))

fun FavouritePair.toDomain(): FavoritePair =
    FavoritePair(fromCurrency = convertFromCurrency, toCurrency = convertToCurrency)

fun FavoritePair.toEntity(): FavouritePair =
    FavouritePair(fromCurrency, toCurrency)
