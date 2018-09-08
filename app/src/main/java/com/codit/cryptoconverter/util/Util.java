package com.codit.cryptoconverter.util;

public class Util {


    public static String getCurrencyName(String currencyCode) {
        if (FiatCurrency.getCurrencyData().containsKey(currencyCode))
            return FiatCurrency.getCurrencyData().get(currencyCode);
        else if (CryptoCurrency.getCryptoCurrencyData().containsKey(currencyCode))
            return CryptoCurrency.getCryptoCurrencyData().get(currencyCode);
        else return currencyCode;
    }

    public static String getCurrencyType(String currencyCode) {
        if (FiatCurrency.getCurrencyData().containsKey(currencyCode))
            return Constants.CURRENCY_TYPE_FIAT;
        else if (CryptoCurrency.getCryptoCurrencyData().containsKey(currencyCode))
            return Constants.CURRENCY_TYPE_CRYPTO;
        else return null;
    }
}
