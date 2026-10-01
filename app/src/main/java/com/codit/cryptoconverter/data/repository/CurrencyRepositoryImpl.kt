package com.codit.cryptoconverter.data.repository

import com.codit.cryptoconverter.domain.model.Currency
import com.codit.cryptoconverter.domain.model.CurrencyType
import com.codit.cryptoconverter.domain.repository.CurrencyRepository
import com.codit.cryptoconverter.util.CryptoCurrency
import com.codit.cryptoconverter.util.FiatCurrency
import com.codit.cryptoconverter.util.Util

class CurrencyRepositoryImpl : CurrencyRepository {
    override fun getAllCurrencies(): List<Currency> {
        val out = mutableListOf<Currency>()
        CryptoCurrency.getCryptoCurrencyData().forEach { (code, name) ->
            out += Currency(code, name, CurrencyType.CRYPTO)
        }
        FiatCurrency.getCurrencyData().forEach { (code, name) ->
            out += Currency(code, name, CurrencyType.FIAT)
        }
        return out.sortedBy { it.code }
    }

    override fun getCryptoCurrencies(): List<Currency> =
        CryptoCurrency.getCryptoCurrencyData()
            .map { (code, name) -> Currency(code, name, CurrencyType.CRYPTO) }
            .sortedBy { it.code }

    override fun getFiatCurrencies(): List<Currency> =
        FiatCurrency.getCurrencyData()
            .map { (code, name) -> Currency(code, name, CurrencyType.FIAT) }
            .sortedBy { it.code }

    override fun getCurrencyName(code: String): String = Util.getCurrencyName(code)

    override fun isFiat(code: String): Boolean =
        FiatCurrency.getCurrencyData().containsKey(code)

    override fun isCrypto(code: String): Boolean =
        CryptoCurrency.getCryptoCurrencyData().containsKey(code)
}
