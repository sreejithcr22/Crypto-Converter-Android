package com.codit.cryptoconverter.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by Sreejith on 22-Nov-17.
 */
@Entity
public class CoinPrices {

    public CoinPrices(){

    }

    @NonNull
    @PrimaryKey
    String coinCode;

    @Ignore
    public HashMap<String,Double> getPrices() {

        return new Gson().fromJson(jsonPricesString,HashMap.class);


    }

    @Ignore
    public void setPrices(HashMap<String, Double> pricesMap) {
        this.jsonPricesString = new Gson().toJson(pricesMap);
    }

    @Ignore
    HashMap<String,Double> prices;

    public CoinPrices(String coinCode, HashMap<String,Double> prices) {
        this.coinCode = coinCode;
        this.jsonPricesString=new Gson().toJson(prices);
        this.prices=prices;

    }


    public String getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }




    public String getJsonPricesString() {
        return jsonPricesString;
    }

    public void setJsonPricesString(String jsonPricesString) {
        this.jsonPricesString = jsonPricesString;
    }

    String jsonPricesString;
}
