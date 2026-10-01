package com.codit.cryptoconverter.http;


import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Sreejith on 22-Nov-17.
 * Uses JsonElement so CryptoCompare error envelopes
 * ({"Response":"Error","Message":...}) don't crash Gson parsing.
 */

public interface MarketApi {

    @GET("data/pricemulti")
    Call<JsonElement> getAllCoinPrices(@Query("fsyms") String coinsList,
                                       @Query("tsyms") String currencyList,
                                       @Query("api_key") String apiKey);


}
