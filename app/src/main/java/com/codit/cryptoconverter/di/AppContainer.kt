package com.codit.cryptoconverter.di

import android.content.Context
import com.codit.cryptoconverter.data.remote.NetworkModule
import com.codit.cryptoconverter.data.repository.CurrencyRepositoryImpl
import com.codit.cryptoconverter.data.repository.FavoritesRepositoryImpl
import com.codit.cryptoconverter.data.repository.MarketRepositoryImpl
import com.codit.cryptoconverter.data.repository.SettingsRepositoryImpl
import com.codit.cryptoconverter.db.AppDatabase
import com.codit.cryptoconverter.domain.repository.CurrencyRepository
import com.codit.cryptoconverter.domain.repository.FavoritesRepository
import com.codit.cryptoconverter.domain.repository.MarketRepository
import com.codit.cryptoconverter.domain.repository.SettingsRepository
import com.codit.cryptoconverter.domain.usecase.ConvertCurrencyUseCase
import com.codit.cryptoconverter.domain.usecase.GetCurrenciesUseCase
import com.codit.cryptoconverter.domain.usecase.ObserveDefaultCurrencyUseCase
import com.codit.cryptoconverter.domain.usecase.ObserveFavoritesUseCase
import com.codit.cryptoconverter.domain.usecase.ObserveIsFavoriteUseCase
import com.codit.cryptoconverter.domain.usecase.ObserveMarketPricesUseCase
import com.codit.cryptoconverter.domain.usecase.RefreshMarketDataUseCase
import com.codit.cryptoconverter.domain.usecase.SetDefaultCurrencyUseCase
import com.codit.cryptoconverter.domain.usecase.ToggleFavoriteUseCase

/**
 * Manual DI container (no Hilt) to keep the migration lightweight.
 * Presentation layer only depends on use-cases; data details stay hidden.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.getDatabase(appContext) }

    val marketRepository: MarketRepository by lazy {
        MarketRepositoryImpl(database, NetworkModule.provideMarketApi())
    }
    val currencyRepository: CurrencyRepository by lazy { CurrencyRepositoryImpl() }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepositoryImpl(database) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(appContext) }

    val observeMarketPrices = ObserveMarketPricesUseCase(marketRepository)
    val refreshMarketData = RefreshMarketDataUseCase(marketRepository)
    val convertCurrency = ConvertCurrencyUseCase()
    val getCurrencies = GetCurrenciesUseCase(currencyRepository)
    val observeFavorites = ObserveFavoritesUseCase(favoritesRepository)
    val toggleFavorite = ToggleFavoriteUseCase(favoritesRepository)
    val observeIsFavorite = ObserveIsFavoriteUseCase(favoritesRepository)
    val observeDefaultCurrency = ObserveDefaultCurrencyUseCase(settingsRepository)
    val setDefaultCurrency = SetDefaultCurrencyUseCase(settingsRepository)
}
