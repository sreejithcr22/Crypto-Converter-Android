package com.codit.cryptoconverter.util;

import java.util.HashMap;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public class Currency {

private static HashMap<String,String> currencyData = new HashMap<>();

static {
    currencyData.put("USD","US Dollar");
    currencyData.put("INR","Indian Rupee");
    currencyData.put("EUR","EURO");
    currencyData.put("GBP","Pound");

}

public static HashMap<String,String> getCurrencyData(){
    return currencyData;
}

}
