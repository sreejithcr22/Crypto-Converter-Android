package com.codit.cryptoconverter.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.codit.cryptoconverter.db.MarketDB;
import com.codit.cryptoconverter.model.CoinPrices;

import java.util.List;

/**
 * Created by Sreejith on 23-Nov-17.
 */

public class MarketViewModel extends AndroidViewModel {


    private final LiveData<List<CoinPrices>> allCoinPricesLive;

    public MarketViewModel(@NonNull Application application) {
        super(application);


        allCoinPricesLive = MarketDB.getInstance().getAllCoinPricesLive(this.getApplication());


    }

    public LiveData<List<CoinPrices>> getAllCoinPricesLive() {
        return allCoinPricesLive;
    }
}
