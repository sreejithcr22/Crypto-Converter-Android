package com.codit.cryptoconverter.util;

import java.util.HashMap;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public class FiatCurrency {

private static HashMap<String,String> currencyData = new HashMap<>();

static {
    currencyData.put("USD","US Dollar");
    currencyData.put("INR","Indian Rupee");
    currencyData.put("EUR","EURO");
    currencyData.put("GBP","Pound");
    currencyData.put("CNY", "Chinese Yuan");
    currencyData.put("AUD", "Australian Dollar");
    currencyData.put("CAD", "Canadian Dollar");
    currencyData.put("JPY", "Japanese Yen");
    currencyData.put("RUR", "Russian ruble");
    currencyData.put("PLN", "Poland złoty");
    currencyData.put("SGD", "Singapore Dollar");
    currencyData.put("HKD", "Hong Kong Dollar");
    currencyData.put("ZAR", "South African Rand");
    currencyData.put("KRW", "South Korean won");
    currencyData.put("NZD", "New Zealand Dollar");
    currencyData.put("SEK", "Swedish Krona");
    currencyData.put("UAH", "Ukrainian hryvnia");
    currencyData.put("ILS", "Israeli New Shekel");
    currencyData.put("THB", "Thai Baht");
    currencyData.put("VND", "Vietnamese dong");
    currencyData.put("DKK", "Danish Krone");
    currencyData.put("MXN", "Mexican Peso");
    currencyData.put("CZK", "Czech Koruna");
    currencyData.put("CLP", "Chilean Peso");
    currencyData.put("TRY", "Turkish lira");
    currencyData.put("NGN", "Nigerian Naira");
    currencyData.put("IDR", "Indonesian Rupiah");
    currencyData.put("MYR", "Malaysian Ringgit");
    currencyData.put("PHP", "Philippine Piso");
    currencyData.put("ARS", "Argentine Peso");
    currencyData.put("IRR", "Iranian Rial");
    currencyData.put("UGX", "Ugandan Shilling");
    currencyData.put("BHD", "Bahraini Dinar");
    currencyData.put("COP", "Colombian Peso");
    currencyData.put("RON", "Romanian Leu");
    currencyData.put("BGN", "Bulgarian Lev");
    currencyData.put("PKR", "Pakistani Rupee");
    currencyData.put("NOK", "Norwegian Krone");
    currencyData.put("JOD", "Jordanian Dinar");


}

public static HashMap<String,String> getCurrencyData(){
    return currencyData;
}

}
