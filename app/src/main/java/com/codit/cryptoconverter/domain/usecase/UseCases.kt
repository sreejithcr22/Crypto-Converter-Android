package com.codit.cryptoconverter.domain.usecase

import com.codit.cryptoconverter.domain.model.CoinPrice
import com.codit.cryptoconverter.domain.model.Currency
import com.codit.cryptoconverter.domain.model.FavoritePair
import com.codit.cryptoconverter.domain.model.RefreshState
import com.codit.cryptoconverter.domain.repository.CurrencyRepository
import com.codit.cryptoconverter.domain.repository.FavoritesRepository
import com.codit.cryptoconverter.domain.repository.MarketRepository
import com.codit.cryptoconverter.domain.repository.SettingsRepository
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.Flow

// ---- Market ----
class ObserveMarketPricesUseCase(
    private val marketRepository: MarketRepository
) {
    operator fun invoke(): Flow<List<CoinPrice>> = marketRepository.observeCoinPrices()
}

class RefreshMarketDataUseCase(
    private val marketRepository: MarketRepository
) {
    operator fun invoke(): Flow<RefreshState> = marketRepository.refreshMarketData()
}

// ---- Conversion (ports legacy ConvertAndDisplay + Calculator) ----
sealed interface ConversionResult {
    data class Success(val outputText: String) : ConversionResult
    data object NotAvailable : ConversionResult
}

class ConvertCurrencyUseCase {
    operator fun invoke(
        input: BigDecimal?,
        from: String,
        to: String,
        pricesByCoin: Map<String, Map<String, Double>>
    ): ConversionResult {
        if (input == null) return ConversionResult.NotAvailable
        // Direct: from-coin table contains `to` (crypto -> fiat/crypto)
        val direct = pricesByCoin[from]?.get(to)
        if (direct != null) {
            val out = input.multiply(BigDecimal(direct.toString())).toPlainString()
            return ConversionResult.Success(out)
        }
        // Inverse: to-coin table contains `from` (fiat -> crypto)
        val inverse = pricesByCoin[to]?.get(from)
        if (inverse != null && inverse != 0.0) {
            val unit = BigDecimal.ONE.divide(
                BigDecimal(inverse.toString()), 8, RoundingMode.HALF_EVEN
            )
            return ConversionResult.Success(unit.multiply(input).toPlainString())
        }
        return ConversionResult.NotAvailable
    }
}

// ---- Currencies ----
class GetCurrenciesUseCase(
    private val currencyRepository: CurrencyRepository
) {
    fun all(): List<Currency> = currencyRepository.getAllCurrencies()
    fun crypto(): List<Currency> = currencyRepository.getCryptoCurrencies()
    fun fiat(): List<Currency> = currencyRepository.getFiatCurrencies()
}

// ---- Favorites ----
class ObserveFavoritesUseCase(
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(): Flow<List<FavoritePair>> = favoritesRepository.observeFavorites()
}

class ToggleFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository
) {
    suspend fun toggle(from: String, to: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) favoritesRepository.removeFavorite(from, to)
        else favoritesRepository.addFavorite(from, to)
    }
}

class ObserveIsFavoriteUseCase(
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(from: String, to: String): Flow<Boolean> =
        favoritesRepository.observeIsFavorite(from, to)
}

// ---- Settings ----
class ObserveDefaultCurrencyUseCase(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<String> = settingsRepository.defaultCurrency
}

class SetDefaultCurrencyUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(code: String) = settingsRepository.setDefaultCurrency(code)
}
