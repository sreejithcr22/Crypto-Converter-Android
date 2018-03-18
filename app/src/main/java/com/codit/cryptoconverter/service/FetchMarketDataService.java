package com.codit.cryptoconverter.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.http.ApiClient;
import com.codit.cryptoconverter.http.MarketApi;
import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.orm.AppDatabase;
import com.codit.cryptoconverter.orm.MarketDao;
import com.codit.cryptoconverter.util.UrlBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FetchMarketDataService extends IntentService {


    public FetchMarketDataService() {
        super("FetchMarketDataService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("wallet", "fetch: ");
            updateDB(fetchDataFromServer());
        }
    }

    LinkedHashMap<String, HashMap<String, Double>>  fetchDataFromServer()
    {
        Retrofit retrofit= ApiClient.getInstance().getMarketClient();
        MarketApi marketApi=retrofit.create(MarketApi.class);

        Call<LinkedHashMap<String,HashMap<String,Double> >> call=marketApi.getAllCoinPrices(UrlBuilder.buildCoinList(),
                UrlBuilder.buildCurrencyList(getApplicationContext().getResources().getStringArray(R.array.currencies)));
        try {

            Response<LinkedHashMap<String, HashMap<String, Double>>> response=call.execute();
            if (response.isSuccessful())
            {
                return response.body();
            }
            else {

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void updateDB(LinkedHashMap<String, HashMap<String, Double>> prices)
    {
        if(prices==null) return;
        List<CoinPrices> coinPricesList=new ArrayList<>();
        for(Map.Entry<String, HashMap<String, Double>> entry : prices.entrySet()) {
            coinPricesList.add(new CoinPrices(entry.getKey(),entry.getValue()));

        }
        MarketDao dao= AppDatabase.getDatabase(getApplicationContext()).marketDao();
        dao.addCoinPrices(coinPricesList);



    }




}
