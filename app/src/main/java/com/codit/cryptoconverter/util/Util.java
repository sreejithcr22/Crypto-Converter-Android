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
        else return Constants.CURRENCY_TYPE_FIAT;
    }

    public static String extractCurrencyCode(String string) {

        try {
            String arr[] = string.split("\\(");
            return arr[1].substring(0, arr[1].length() - 1);
        } catch (Exception e) {
            return null;
        }
    }
}
