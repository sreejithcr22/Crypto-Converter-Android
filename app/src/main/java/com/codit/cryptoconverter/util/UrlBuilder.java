package com.codit.cryptoconverter.util;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class UrlBuilder {

    private static final String TAG = "UrlBuilder";

    public static String buildCryptoCurrencyList(int startIndex, int endIndex)
    {

        StringBuffer coinList=new StringBuffer();
        Object array[] = CryptoCurrency.getCryptoCurrencyData().keySet().toArray();
        String[] currencyCodes = Arrays.copyOf(array, array.length, String[].class);
        /*for (Map.Entry<String,String> entry: CryptoCurrency.getCryptoCurrencyData().entrySet()) {
            if (isLimited && count == Constants.API_CURRENCY_ARG_LIMIT) { break; }
            coinList.append(entry.getKey()+",");
            count++;
        }*/

        endIndex = currencyCodes.length < endIndex ? currencyCodes.length : endIndex;
        Log.d(TAG, "buildCryptoCurrencyList: endIndex=" + String.valueOf(endIndex));
        for (int i = startIndex; i < endIndex; i++) {
            coinList.append(currencyCodes[i] + ",");
        }
        coinList.setLength(coinList.length()-1);
        Log.d(TAG, coinList.toString());
        return coinList.toString();
    }

    public static String buildFiatCurrencyList(String currencyArray[], int startIndex, int endIndex)
    {

        StringBuffer currencyList=new StringBuffer();
        endIndex = currencyArray.length < endIndex ? currencyArray.length : endIndex;
        Log.d(TAG, "buildFiatCurrencyList: endIndex=" + String.valueOf(endIndex));
        for (int i = startIndex; i <= endIndex; i++) {

            if (currencyArray.length != i) {
                currencyList.append(currencyArray[i] + ",");
            }

        }
        currencyList.setLength(currencyList.length()-1);
        Log.d(TAG, currencyList.toString());
        return currencyList.toString();
    }
}
