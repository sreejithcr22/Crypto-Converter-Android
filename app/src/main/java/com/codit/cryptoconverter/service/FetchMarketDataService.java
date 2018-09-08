package com.codit.cryptoconverter.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.db.MarketDB;
import com.codit.cryptoconverter.helper.FetchDataRunnable;
import com.codit.cryptoconverter.http.ApiClient;
import com.codit.cryptoconverter.http.MarketApi;
import com.codit.cryptoconverter.listener.FetchDataCallback;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.CryptoCurrency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;


public class FetchMarketDataService extends IntentService implements FetchDataCallback {
    private static final String TAG = "FetchMarketDataService";
    HandlerThread handlerThread;
    Handler handler;
    List<FetchDataRunnable> apiCallQueue = new ArrayList<>();

    public FetchMarketDataService() {
        super("FetchMarketDataService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("wallet", "fetch: ");
            handlerThread = new HandlerThread("thread");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());

            int fsysIterator = (int) Math.ceil(Double.valueOf(String.valueOf(CryptoCurrency.getCryptoCurrencyData().size())) / Double.valueOf(Constants.API_CALL_FSYS_ARG_LIMIT));
            int tosysIterator = (int) Math.ceil(Double.valueOf(String.valueOf(getApplicationContext().getResources().getStringArray(R.array.fiat_currencies).length)) / Double.valueOf(Constants.API_CALL_TOSYS_ARG_LIMIT));
            int tosysCryptoIterator = (int) Math.ceil(Double.valueOf(String.valueOf(CryptoCurrency.getCryptoCurrencyData().size())) / Double.valueOf(Constants.API_CALL_TOSYS_ARG_LIMIT));
            Log.d(TAG, "fsysIterator=" + String.valueOf(fsysIterator) + ", tosysIterator=" + String.valueOf(tosysIterator) + ", tosysCryptoIterator=" + tosysCryptoIterator);

            for (int i = 1; i <= fsysIterator; i++) {
                int fsysStartIndex = (i * Constants.API_CALL_FSYS_ARG_LIMIT) - Constants.API_CALL_FSYS_ARG_LIMIT;
                int fsysEndIndex = (i * Constants.API_CALL_FSYS_ARG_LIMIT) - 1;

                for (int j = 1; j <= tosysIterator; j++) {

                    int tosysStartIndex = (j * Constants.API_CALL_TOSYS_ARG_LIMIT) - Constants.API_CALL_TOSYS_ARG_LIMIT;
                    int tosysEndIndex = (j * Constants.API_CALL_TOSYS_ARG_LIMIT - 1);
                    //handler.postDelayed(new FetchDataRunnable(fsysStartIndex,fsysEndIndex,tosysStartIndex,tosysEndIndex,Constants.CURRENCY_TYPE_FIAT), Constants.API_CALL_DELAY);
                    apiCallQueue.add((new FetchDataRunnable(fsysStartIndex, fsysEndIndex, tosysStartIndex, tosysEndIndex, Constants.CURRENCY_TYPE_FIAT, apiCallQueue.size(), this, getApplicationContext())));
                }

                for (int k = 1; k <= tosysCryptoIterator; k++) {
                    int tosysStartIndex = (k * Constants.API_CALL_TOSYS_ARG_LIMIT) - Constants.API_CALL_TOSYS_ARG_LIMIT;
                    int tosysEndIndex = (k * Constants.API_CALL_TOSYS_ARG_LIMIT - 1);
                    //handler.postDelayed(new FetchDataRunnable(fsysStartIndex,fsysEndIndex,tosysStartIndex,tosysEndIndex,Constants.CURRENCY_TYPE_CRYPTO), Constants.API_CALL_DELAY);
                    apiCallQueue.add((new FetchDataRunnable(fsysStartIndex, fsysEndIndex, tosysStartIndex, tosysEndIndex, Constants.CURRENCY_TYPE_CRYPTO, apiCallQueue.size(), this, getApplicationContext())));

                }
            }

            //start api calls
            if (apiCallQueue.size() != 0) {
                Log.d(TAG, "FetchMarketDataService: api call start");
                handler.post(apiCallQueue.get(0));
            }


        }
    }

    LinkedHashMap<String, HashMap<String, Double>> fetchDataFromServer(String fsysUrl, String tosysUrl) {
        Retrofit retrofit = ApiClient.getInstance().getMarketClient();
        MarketApi marketApi = retrofit.create(MarketApi.class);
        Call<LinkedHashMap<String, HashMap<String, Double>>> call;


        call = marketApi.getAllCoinPrices(fsysUrl, tosysUrl);
        Log.d(TAG, "fetchDataFromServer: url=" + call.request().url().toString());
        try {

            Response<LinkedHashMap<String, HashMap<String, Double>>> response = call.execute();
            if (response.isSuccessful()) {

                return response.body();
            } else {

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*void updateDB(LinkedHashMap<String, HashMap<String, Double>> fetchedPricesList)
    {
        if(fetchedPricesList==null) return;

        MarketDao dao= AppDatabase.getDatabase(getApplicationContext()).marketDao();
        List<CoinPrices> dbPricesList = dao.getAllCoinPricesLive();
        if(dbPricesList!= null && dbPricesList.size()>0) {
            //Log.d(TAG, "updateDB:getAllCoinPricesLive returned values ");
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
            Log.d(TAG, "updateDB:first data fetch ");
            List<CoinPrices> coinPricesList=new ArrayList<>();
            for(Map.Entry<String, HashMap<String, Double>> entry : fetchedPricesList.entrySet()) {
                coinPricesList.add(new CoinPrices(entry.getKey(),entry.getValue()));
                dao.addCoinPrices(coinPricesList);
        }



        }


    }*/


    @Override
    public void onCurrentApiCallFinished(int qPos) {
        if (apiCallQueue.size() == qPos + 1) {
            handlerThread.quit();
            Log.d(TAG, "FetchMarketDataService: api call finish");
            return;
        }
        handler.postDelayed(apiCallQueue.get(qPos + 1), Constants.API_CALL_DELAY);
    }

    @Override
    public void executeApiCall(String fsysUrl, String tosysUrl) {
        MarketDB.getInstance().updateDB(getApplicationContext(), fetchDataFromServer(fsysUrl, tosysUrl));
    }


}
