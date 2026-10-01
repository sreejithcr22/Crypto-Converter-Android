package com.codit.cryptoconverter.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.codit.cryptoconverter.BuildConfig;
import com.codit.cryptoconverter.db.MarketDB;
import com.codit.cryptoconverter.helper.FetchDataRunnable;
import com.codit.cryptoconverter.http.ApiClient;
import com.codit.cryptoconverter.http.MarketApi;
import com.codit.cryptoconverter.listener.FetchDataCallback;
import com.codit.cryptoconverter.receiver.ProgressReceiver;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.CryptoCurrency;
import com.codit.cryptoconverter.util.FiatCurrency;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
            int tosysIterator = (int) Math.ceil(Double.valueOf(String.valueOf(FiatCurrency.getCurrencyData().size())) / Double.valueOf(Constants.API_CALL_TOSYS_ARG_LIMIT));
            int tosysCryptoIterator = (int) Math.ceil(Double.valueOf(String.valueOf(CryptoCurrency.getCryptoCurrencyData().size())) / Double.valueOf(Constants.API_CALL_TOSYS_ARG_LIMIT));
            Log.d(TAG, "fsysIterator=" + fsysIterator + ", tosysIterator=" + tosysIterator + ", tosysCryptoIterator=" + tosysCryptoIterator);

            for (int i = 1; i <= fsysIterator; i++) {
                int fsysStartIndex = (i * Constants.API_CALL_FSYS_ARG_LIMIT) - Constants.API_CALL_FSYS_ARG_LIMIT;
                int fsysEndIndex = (i * Constants.API_CALL_FSYS_ARG_LIMIT) - 1;

                for (int j = 1; j <= tosysIterator; j++) {

                    int tosysStartIndex = (j * Constants.API_CALL_TOSYS_ARG_LIMIT) - Constants.API_CALL_TOSYS_ARG_LIMIT;
                    int tosysEndIndex = (j * Constants.API_CALL_TOSYS_ARG_LIMIT - 1);
                    apiCallQueue.add((new FetchDataRunnable(fsysStartIndex, fsysEndIndex, tosysStartIndex, tosysEndIndex, Constants.CURRENCY_TYPE_FIAT, apiCallQueue.size(), this, getApplicationContext())));
                }

                for (int k = 1; k <= tosysCryptoIterator; k++) {
                    int tosysStartIndex = (k * Constants.API_CALL_TOSYS_ARG_LIMIT) - Constants.API_CALL_TOSYS_ARG_LIMIT;
                    int tosysEndIndex = (k * Constants.API_CALL_TOSYS_ARG_LIMIT - 1);
                    apiCallQueue.add((new FetchDataRunnable(fsysStartIndex, fsysEndIndex, tosysStartIndex, tosysEndIndex, Constants.CURRENCY_TYPE_CRYPTO, apiCallQueue.size(), this, getApplicationContext())));

                }
            }

            //start api calls
            if (apiCallQueue.size() != 0) {
                Log.d(TAG, "FetchMarketDataService: api call start");
                sendProgress(0);
                handler.post(apiCallQueue.get(0));
            }


        }
    }

    String getApiKeyOrNull() {
        String apiKey = BuildConfig.CRYPTOCOMPARE_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) return null; // Retrofit omits null query params
        return apiKey.trim();
    }

    LinkedHashMap<String, HashMap<String, Double>> fetchDataFromServer(String fsysUrl, String tosysUrl) {
        Retrofit retrofit = ApiClient.getInstance().getMarketClient();
        MarketApi marketApi = retrofit.create(MarketApi.class);
        Call<JsonElement> call;


        String apiKey = getApiKeyOrNull();
        if (apiKey == null) {
            Log.w(TAG, "fetchDataFromServer: missing CRYPTOCOMPARE_API_KEY (apikeys.properties)");
        }
        call = marketApi.getAllCoinPrices(fsysUrl, tosysUrl, apiKey);
        Log.d(TAG, "fetchDataFromServer: url=" + call.request().url().toString());
        try {

            Response<JsonElement> response = call.execute();
            if (!response.isSuccessful()) {
                String errBody = null;
                try { errBody = response.errorBody() != null ? response.errorBody().string() : null; } catch (Exception ignored) {}
                Log.w(TAG, "fetchDataFromServer: failed code=" + response.code() + " body=" + errBody);
                return null;
            }
            JsonElement body = response.body();
            if (body == null || body.isJsonNull()) {
                Log.w(TAG, "fetchDataFromServer: empty body");
                return null;
            }
            // CryptoCompare returns HTTP 200 with {"Response":"Error","Message":...} on failures
            // (e.g. rate limit). Detect and skip instead of crashing Gson parsing.
            if (body.isJsonObject() && body.getAsJsonObject().has("Response")) {
                String status = body.getAsJsonObject().get("Response").getAsString();
                if ("Error".equalsIgnoreCase(status)) {
                    String msg = body.getAsJsonObject().has("Message")
                            ? body.getAsJsonObject().get("Message").getAsString() : "unknown error";
                    Log.w(TAG, "fetchDataFromServer: API error: " + msg);
                    return null;
                }
            }
            Type mapType = new TypeToken<LinkedHashMap<String, HashMap<String, Double>>>() {}.getType();
            LinkedHashMap<String, HashMap<String, Double>> prices = new Gson().fromJson(body, mapType);
            if (prices == null || prices.isEmpty()) {
                Log.w(TAG, "fetchDataFromServer: no prices in body=" + body.toString().substring(0, Math.min(300, body.toString().length())));
            }
            return prices;
        } catch (Exception e) {
            Log.w(TAG, "fetchDataFromServer: exception=" + e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onCurrentApiCallFinished(int qPos) {
        Log.d(TAG, "q size = "+apiCallQueue.size()+", pos = "+qPos);
        sendProgress(qPos+1);
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

    private void sendProgress(int qPos) {
        int progress = 0;
        if (apiCallQueue == null || apiCallQueue.size() == 0) {
            progress = 100;
        } else {

            progress = Math.round((((float) qPos / apiCallQueue.size()) * 100));
        }
        Intent intent = new Intent(ProgressReceiver.PROGRESS_ACTION);
        intent.putExtra(ProgressReceiver.CURRENT_PROGRESS, progress);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
