package com.codit.cryptoconverter.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(primaryKeys = {"convertFromCurrency", "convertToCurrency"})
public class FavouritePair {

    public FavouritePair() {

    }

    @Ignore
    public FavouritePair(String convertFromCurrency, String convertToCurrency) {
        this.convertFromCurrency = convertFromCurrency;
        this.convertToCurrency = convertToCurrency;
    }


    public String getConvertFromCurrency() {
        return convertFromCurrency;
    }

    public void setConvertFromCurrency(String convertFromCurrency) {
        this.convertFromCurrency = convertFromCurrency;
    }

    public String getConvertToCurrency() {
        return convertToCurrency;
    }

    public void setConvertToCurrency(String convertToCurrency) {
        this.convertToCurrency = convertToCurrency;
    }

    @NonNull
    String convertFromCurrency, convertToCurrency;
}
