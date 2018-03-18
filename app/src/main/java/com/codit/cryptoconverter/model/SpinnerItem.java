package com.codit.cryptoconverter.model;

/**
 * Created by Sreejith on 03-Mar-18.
 */

public class SpinnerItem {

    public static final String CURRENCY_TYPE_FIAT = "fiat";
    public static final String CURRENCY_TYPE_CRYPTO = "crypto";

    private String currencyCode;
    private String currencyName;
    private String currencyType;

    public SpinnerItem(String currencyCode, String currencyName, String currencyType) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.currencyType = currencyType;
    }


    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencyType() {
        return currencyType;
    }


}
