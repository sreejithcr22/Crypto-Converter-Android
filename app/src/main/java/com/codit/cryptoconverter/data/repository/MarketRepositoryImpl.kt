package com.codit.cryptoconverter.data.repository

import com.codit.cryptoconverter.data.local.toDomain
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
        NetworkModule.provideMarketApi(),
    private val gecko: com.codit.cryptoconverter.data.remote.CoinGeckoApiService =
        NetworkModule.provideCoinGeckoApi()
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
            // Primary: CoinGecko keyless API (no key, few calls).
            emit(RefreshState.Refreshing(5))
            val geckoMatrix = fetchCoinGeckoMatrix()
            if (geckoMatrix.isNotEmpty()) {
                emit(RefreshState.Refreshing(85))
                withContext(Dispatchers.IO) { mergeIntoDb(geckoMatrix) }
                emit(RefreshState.Refreshing(100))
                emit(RefreshState.Success)
                return@flow
            }
            // Fallback: legacy CryptoCompare matrix (needs a healthy key).
            emit(RefreshState.Refreshing(85))
            val ccMatrix = fetchCryptoCompareMatrix()
            if (ccMatrix.isNotEmpty()) {
                withContext(Dispatchers.IO) { mergeIntoDb(ccMatrix) }
                emit(RefreshState.Refreshing(100))
                emit(RefreshState.Success)
            } else {
                emit(RefreshState.Error("No prices returned (rate-limited?). Try again later."))
            }
        } catch (e: Exception) {
            emit(RefreshState.Error(e.message ?: "Refresh failed"))
        }
    }

    /**
     * CoinGecko fetch: symbol-based simple/price (response keyed by lowercase
     * symbol, so no id mapping needed) + exchange_rates for fiats that
     * simple/price doesn't quote. Crypto x crypto derived via USD cross.
     * ~4 calls with 5s pacing (keyless pool is ~10-30 calls/min).
     */
    private suspend fun fetchCoinGeckoMatrix(): LinkedHashMap<String, HashMap<String, Double>> {
        val cryptoCodes = CryptoCurrency.getCryptoCurrencyData().keys.toList()
        val fiatCodes = FiatCurrency.getCurrencyData().keys.toList()
        val result = LinkedHashMap<String, HashMap<String, Double>>()
        if (cryptoCodes.isEmpty()) return result
        val cryptoSet = HashSet(cryptoCodes)

        val vsToOriginal = LinkedHashMap<String, String>()
        for (fiat in fiatCodes) {
            val vs = vsCodeFor(fiat)
            if (vs != null) vsToOriginal[vs] = fiat
        }
        val vsCurrencies = ArrayList(vsToOriginal.keys)
        vsCurrencies.add("btc")
        val vsParam = vsCurrencies.joinToString(",")

        val chunks = ArrayList<List<String>>()
        var i = 0
        val lower = cryptoCodes.map { it.lowercase() }
        while (i < lower.size) {
            chunks.add(lower.subList(i, minOf(i + 40, lower.size)))
            i += 40
        }

        for (chunk in chunks) {
            val resp: Map<String, Map<String, Double>> = withContext(Dispatchers.IO) {
                gecko.getPrices(chunk.joinToString(","), vsParam)
            }
            for (entry in resp.entries) {
                val sym = entry.key.uppercase()
                if (!cryptoSet.contains(sym)) continue
                val map = result[sym] ?: HashMap<String, Double>().also { result[sym] = it }
                val prices = entry.value
                for (priceEntry in prices.entries) {
                    val key = priceEntry.key.lowercase()
                    val value = priceEntry.value
                    if (value <= 0) continue
                    if (key == "btc") {
                        map["BTC"] = value
                    } else {
                        val original = vsToOriginal[key] ?: continue
                        map[original] = value
                    }
                }
            }
            delay(5000)
        }

        // FX derivation for fiats simple/price doesn't quote.
        try {
            val rates = withContext(Dispatchers.IO) { gecko.getExchangeRates().rates }
            val usdRate = rates["usd"]?.value ?: 0.0
            if (usdRate > 0) {
                for (map in result.values) {
                    val usd = map["USD"] ?: continue
                    for (fiat in fiatCodes) {
                        if (map.containsKey(fiat)) continue
                        val r = rates[fiat.lowercase()]?.value ?: 0.0
                        if (r > 0) map[fiat] = usd * (r / usdRate)
                    }
                }
            }
        } catch (_: Exception) {
            // Best-effort; direct quotes already stored.
        }

        // Crypto x crypto cross via USD.
        val usdByCoin = HashMap<String, Double>()
        for (e in result.entries) usdByCoin[e.key] = e.value["USD"] ?: 0.0
        for (e in result.entries) {
            val priceA = usdByCoin[e.key] ?: 0.0
            if (priceA <= 0) continue
            for (coinB in result.keys) {
                if (coinB == e.key || e.value.containsKey(coinB)) continue
                val priceB = usdByCoin[coinB] ?: 0.0
                if (priceB <= 0) continue
                e.value[coinB] = priceA / priceB
            }
        }
        return result
    }

    /** Legacy CryptoCompare full-matrix fetch. Returns merged matrix. */
    private suspend fun fetchCryptoCompareMatrix(): LinkedHashMap<String, HashMap<String, Double>> {
        val cryptoCodes = CryptoCurrency.getCryptoCurrencyData().keys.toList()
        val fiatCodes = FiatCurrency.getCurrencyData().keys.toList()
        val result = LinkedHashMap<String, HashMap<String, Double>>()
        if (cryptoCodes.isEmpty()) return result

        val batches = ArrayList<Pair<String, String>>()
        var fsysStart = 0
        while (fsysStart < cryptoCodes.size) {
            val fsysEnd = minOf(fsysStart + 50, cryptoCodes.size)
            val fsys = cryptoCodes.subList(fsysStart, fsysEnd).joinToString(",")
            var toStart = 0
            while (toStart < fiatCodes.size) {
                val toEnd = minOf(toStart + 20, fiatCodes.size)
                batches.add(Pair(fsys, fiatCodes.subList(toStart, toEnd).joinToString(",")))
                toStart += 20
            }
            var cStart = 0
            while (cStart < cryptoCodes.size) {
                val cEnd = minOf(cStart + 20, cryptoCodes.size)
                batches.add(Pair(fsys, cryptoCodes.subList(cStart, cEnd).joinToString(",")))
                cStart += 20
            }
            fsysStart += 50
        }

        val apiKey = NetworkModule.apiKeyOrNull()
        for (batch in batches) {
            val body = withContext(Dispatchers.IO) {
                api.getAllCoinPrices(batch.first, batch.second, apiKey)
            }
            val parsed = parseBodyOrNull(body)
            if (parsed != null) {
                for (e in parsed.entries) {
                    val map = result[e.key] ?: HashMap<String, Double>().also { result[e.key] = it }
                    map.putAll(e.value)
                }
            }
            delay(400)
        }
        return result
    }

    private fun vsCodeFor(fiatCode: String): String? {
        return when (fiatCode) {
            "RUR" -> "rub"
            "IRR", "UGX", "BGN", "JOD" -> null
            else -> fiatCode.lowercase()
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
        for (entry in fetched.entries) {
            var idx = -1
            for (j in current.indices) {
                if (current[j].coinCode == entry.key) {
                    idx = j
                    break
                }
            }
            if (idx == -1) {
                current.add(CoinPrices(entry.key, entry.value))
            } else {
                val existing = current[idx]
                val merged = try {
                    val existingMap = Gson().fromJson(
                        existing.jsonPricesString, HashMap::class.java
                    )
                    @Suppress("UNCHECKED_CAST")
                    val typed = (existingMap as? HashMap<String, Double>) ?: hashMapOf()
                    typed.putAll(entry.value)
                    typed
                } catch (_: Exception) {
                    HashMap(entry.value)
                }
                existing.setPrices(merged)
                current[idx] = existing
            }
        }
        dao.addCoinPrices(current)
    }
}
