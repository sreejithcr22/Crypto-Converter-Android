package com.codit.cryptoconverter.data.repository

import com.codit.cryptoconverter.data.local.toDomain
import com.codit.cryptoconverter.data.local.toEntity
import com.codit.cryptoconverter.data.remote.NetworkModule
import com.codit.cryptoconverter.db.AppDatabase
import com.codit.cryptoconverter.domain.model.CoinPrice
import com.codit.cryptoconverter.domain.model.RefreshState
import com.codit.cryptoconverter.domain.repository.MarketRepository
import com.codit.cryptoconverter.model.CoinPrices
import com.codit.cryptoconverter.util.CryptoCurrency
import com.codit.cryptoconverter.util.FiatCurrency
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MarketRepositoryImpl(
    private val db: AppDatabase,
    private val api: com.codit.cryptoconverter.data.remote.MarketApiService =
        NetworkModule.provideMarketApi()
) : MarketRepository {

    private val gson = Gson()

    override fun observeCoinPrices(): Flow<List<CoinPrice>> =
        db.marketDao().getAllCoinPricesFlow().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getCoinPrice(coinCode: String): CoinPrice? =
        withContext(Dispatchers.IO) {
            db.marketDao().getCoinPricesFor(coinCode)?.toDomain()
        }

    override suspend fun isCacheEmpty(): Boolean = withContext(Dispatchers.IO) {
        db.marketDao().getAllCoinPrices().isEmpty()
    }

    override fun refreshMarketData(): Flow<RefreshState> = flow {
        emit(RefreshState.Refreshing(0))
        try {
            val cryptoCodes = CryptoCurrency.getCryptoCurrencyData().keys.toList()
            val fiatCodes = FiatCurrency.getCurrencyData().keys.toList()
            if (cryptoCodes.isEmpty()) {
                emit(RefreshState.Error("No currencies configured"))
                return@flow
            }
            val fsysLimit = 50
            val tosysLimit = 20

            data class Batch(val fsys: String, val tsyms: String)

            val batches = mutableListOf<Batch>()
            var fsysStart = 0
            while (fsysStart < cryptoCodes.size) {
                val fsysEnd = minOf(fsysStart + fsysLimit, cryptoCodes.size)
                val fsys = cryptoCodes.subList(fsysStart, fsysEnd).joinToString(",")
                var toStart = 0
                while (toStart < fiatCodes.size) {
                    val toEnd = minOf(toStart + tosysLimit, fiatCodes.size)
                    batches += Batch(fsys, fiatCodes.subList(toStart, toEnd).joinToString(","))
                    toStart += tosysLimit
                }
                var cStart = 0
                while (cStart < cryptoCodes.size) {
                    val cEnd = minOf(cStart + tosysLimit, cryptoCodes.size)
                    batches += Batch(fsys, cryptoCodes.subList(cStart, cEnd).joinToString(","))
                    cStart += tosysLimit
                }
                fsysStart += fsysLimit
            }

            val apiKey = NetworkModule.apiKeyOrNull()
            var done = 0
            for (batch in batches) {
                val body = withContext(Dispatchers.IO) {
                    api.getAllCoinPrices(batch.fsys, batch.tsyms, apiKey)
                }
                val parsed = parseBodyOrNull(body)
                if (parsed != null) {
                    withContext(Dispatchers.IO) { mergeIntoDb(parsed) }
                }
                done++
                emit(RefreshState.Refreshing((done * 100 / batches.size).coerceIn(0, 100)))
                if (batch != batches.last()) delay(400)
            }
            emit(RefreshState.Success)
        } catch (e: Exception) {
            emit(RefreshState.Error(e.message ?: "Refresh failed"))
        }
    }

    private fun parseBodyOrNull(body: com.google.gson.JsonElement?): LinkedHashMap<String, HashMap<String, Double>>? {
        if (body == null || body.isJsonNull) return null
        try {
            if (body.isJsonObject && body.asJsonObject.has("Response")) {
                val status = body.asJsonObject.get("Response").asString
                if (status.equals("Error", ignoreCase = true)) return null
            }
            val type = object : TypeToken<LinkedHashMap<String, HashMap<String, Double>>>() {}.type
            return gson.fromJson(body, type)
        } catch (_: Exception) {
            return null
        }
    }

    private fun mergeIntoDb(fetched: LinkedHashMap<String, HashMap<String, Double>>) {
        if (fetched.isEmpty()) return
        val dao = db.marketDao()
        val current = dao.getAllCoinPrices().toMutableList()
        val indexOf: (String) -> Int = { code ->
            current.indexOfFirst { it.coinCode == code }
        }
        for ((coin, prices) in fetched) {
            val idx = indexOf(coin)
            if (idx == -1) {
                current.add(CoinPrices(coin, prices))
            } else {
                val existing = current[idx]
                val merged = try {
                    val existingMap: HashMap<String, Double> = Gson().fromJson(
                        existing.jsonPricesString, HashMap::class.java
                    ) as? HashMap<String, Double> ?: hashMapOf()
                    existingMap.putAll(prices)
                    existingMap
                } catch (_: Exception) {
                    HashMap(prices)
                }
                existing.setPrices(merged)
                current[idx] = existing
            }
        }
        dao.addCoinPrices(current)
    }
}
