package com.codit.cryptoconverter.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codit.cryptoconverter.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val defaultCurrencyKey = stringPreferencesKey("default_currency")

    override val defaultCurrency: Flow<String> =
        context.settingsDataStore.data.map { prefs ->
            prefs[defaultCurrencyKey] ?: "USD"
        }

    override suspend fun setDefaultCurrency(code: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[defaultCurrencyKey] = code
        }
        // Keep legacy SharedPreferences in sync so old fragments still see the change
        try {
            val legacy = android.preference.PreferenceManager
                .getDefaultSharedPreferences(context)
            legacy.edit().putString("crypto_watch_wallet", code).apply()
        } catch (_: Exception) {
        }
    }
}
