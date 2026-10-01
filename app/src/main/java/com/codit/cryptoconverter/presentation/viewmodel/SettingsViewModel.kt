package com.codit.cryptoconverter.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.codit.cryptoconverter.di.AppContainer
import com.codit.cryptoconverter.domain.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val defaultCurrency: String = "USD",
    val fiatCurrencies: List<Currency> = emptyList()
)

class SettingsViewModel(
    private val container: AppContainer
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(fiatCurrencies = emptyList())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(fiatCurrencies = container.getCurrencies.fiat()) }
        viewModelScope.launch {
            container.observeDefaultCurrency().collect { code ->
                _uiState.update { it.copy(defaultCurrency = code) }
            }
        }
    }

    fun setDefaultCurrency(code: String) {
        viewModelScope.launch {
            container.setDefaultCurrency(code)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(container) as T
        }
    }
}
