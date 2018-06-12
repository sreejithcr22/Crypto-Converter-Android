package com.codit.cryptoconverter.util;

import android.util.Log;

import java.util.Map;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class UrlBuilder {

    public static String buildCoinList(boolean isLimited)
    {
        int count = 0;
        StringBuffer coinList=new StringBuffer();
        for (Map.Entry<String,String> entry:Coin.getCoinsData().entrySet()) {
            if (isLimited && count == Constants.API_CURRENCY_ARG_LIMIT) { break; }
            coinList.append(entry.getKey()+",");
            count++;
        }
        coinList.setLength(coinList.length()-1);
        Log.d("url- buildCoinList-", coinList.toString());
        return coinList.toString();
    }

    public static String buildCurrencyList(String currencyArray[])
    {

        StringBuffer currencyList=new StringBuffer();
        for (String currency:currencyArray) {
            currencyList.append(currency+",");
        }
        currencyList.setLength(currencyList.length()-1);
        Log.d("url-buildCurrencyList-", currencyList.toString());
        return currencyList.toString();
    }
}
