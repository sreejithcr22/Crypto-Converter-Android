package com.codit.cryptoconverter.db;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.util.Log;

import com.codit.cryptoconverter.model.CoinPrices;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MarketDB {

    private static final String TAG = MarketDB.class.getSimpleName();
    private static MarketDB marketDB = new MarketDB();

    private MarketDB() {
    }


    public static MarketDB getInstance() {
        if (marketDB == null) {
            marketDB = new MarketDB();
        }
        return marketDB;
    }

    public int getIndexOf(String coinCode, List<CoinPrices> dbPricesList) {

        for (int i = 0; i < dbPricesList.size(); i++) {
            CoinPrices prices = dbPricesList.get(i);
            if (prices.getCoinCode().equals(coinCode)) {
                return i;
            }
        }

        return -1;
    }

    public void updateDB(Context context, LinkedHashMap<String, HashMap<String, Double>> fetchedPricesList) {
        if (fetchedPricesList == null || fetchedPricesList.size() == 0) return;

        MarketDao dao = AppDatabase.getDatabase(context).marketDao();
        List<CoinPrices> dbPricesList = dao.getAllCoinPrices();

        for (Map.Entry<String, HashMap<String, Double>> entry : fetchedPricesList.entrySet()) {

            if (dbPricesList == null || dbPricesList.size() == 0 ||
                    MarketDB.getInstance().getIndexOf(entry.getKey(), dbPricesList) == -1) {
                Log.d(TAG, "updateDB: add new coin - " + entry.getKey());
                dbPricesList.add(new CoinPrices(entry.getKey(), entry.getValue()));
            } else {

                int index = MarketDB.getInstance().getIndexOf(entry.getKey(), dbPricesList);
                CoinPrices dbPrices = dbPricesList.get(index);
                HashMap<String, Double> dbPricesMap = dbPrices.getPrices();
                dbPricesMap.putAll(entry.getValue());
                dbPrices.setPrices(dbPricesMap);
                dbPricesList.set(index, dbPrices);
                Log.d(TAG, "updateDB: update coin - " + dbPrices.getCoinCode() + "--values--" + dbPrices.getJsonPricesString());
            }
        }

        dao.addCoinPrices(dbPricesList);
    }

    public LiveData<List<CoinPrices>> getAllCoinPricesLive(Context context) {
        return AppDatabase.getDatabase(context.getApplicationContext()).marketDao().getAllCoinPricesLive();
    }
}
