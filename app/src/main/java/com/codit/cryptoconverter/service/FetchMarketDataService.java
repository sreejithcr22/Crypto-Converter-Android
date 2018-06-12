package com.codit.cryptoconverter.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Looper;
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
import java.util.logging.Handler;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FetchMarketDataService extends IntentService {
    private static final String TAG = "FetchMarketDataService";


    public FetchMarketDataService() {
        super("FetchMarketDataService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("wallet", "fetch: ");
            updateDB(fetchDataFromServer(false));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateDB(fetchDataFromServer(true));
        }
    }

     LinkedHashMap<String, HashMap<String, Double>>  fetchDataFromServer(boolean isCurrencyLimited)
    {
        Retrofit retrofit= ApiClient.getInstance().getMarketClient();
        MarketApi marketApi=retrofit.create(MarketApi.class);
        Call<LinkedHashMap<String,HashMap<String,Double> >> call;

        if(isCurrencyLimited) {
            call=marketApi.getAllCoinPrices(UrlBuilder.buildCoinList(false),
                    UrlBuilder.buildCoinList(true));
        }
        else {
            call = marketApi.getAllCoinPrices(UrlBuilder.buildCoinList(false),
                    UrlBuilder.buildCurrencyList(getApplicationContext().getResources().getStringArray(R.array.currencies))); }
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

    void updateDB(LinkedHashMap<String, HashMap<String, Double>> fetchedPricesList)
    {
        if(fetchedPricesList==null) return;

        MarketDao dao= AppDatabase.getDatabase(getApplicationContext()).marketDao();
        List<CoinPrices> dbPricesList = dao.getAllCoinPrices();
        if(dbPricesList!= null && dbPricesList.size()>0) {
            Log.d(TAG, "updateDB:getAllCoinPrices returned values ");
            for (CoinPrices dbPrices: dbPricesList) {
                Log.d(TAG, "updateDB: before update--"+dbPrices.getCoinCode()+"--"+dbPrices.getJsonPricesString());
                HashMap<String, Double> dbPricesMap = dbPrices.getPrices();
                if (dbPricesMap!=null && fetchedPricesList.containsKey(dbPrices.getCoinCode()) && fetchedPricesList.get(dbPrices.getCoinCode())!=null) {
                    HashMap<String, Double> fetchedPricesMap = fetchedPricesList.get(dbPrices.getCoinCode());
                    dbPricesMap.putAll(fetchedPricesMap);
                    dbPrices.setPrices(dbPricesMap);
                    Log.d(TAG, "updateDB: after update--"+dbPrices.getCoinCode()+"--"+dbPrices.getJsonPricesString());
                }
                else {
                    Log.d(TAG, "updateDB: some null");
                }

            }

            dao.addCoinPrices(dbPricesList);

        } else {
            Log.d(TAG, "updateDB:getAllCoinPrices returned null ");
            List<CoinPrices> coinPricesList=new ArrayList<>();
            for(Map.Entry<String, HashMap<String, Double>> entry : fetchedPricesList.entrySet()) {
                coinPricesList.add(new CoinPrices(entry.getKey(),entry.getValue()));
                dao.addCoinPrices(coinPricesList);
        }



        }


    }




}
