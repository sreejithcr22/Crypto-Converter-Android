package com.codit.cryptoconverter.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.orm.AppDatabase;

import java.util.List;

/**
 * Created by Sreejith on 23-Nov-17.
 */

public class MarketViewModel extends AndroidViewModel {


    public LiveData<List<CoinPrices>> getAllCoinPrices() {
        return allCoinPrices;
    }

    private final LiveData<List<CoinPrices>> allCoinPrices;


    private AppDatabase appDatabase;

    public MarketViewModel(@NonNull Application application) {
        super(application);

        appDatabase = AppDatabase.getDatabase(this.getApplication());

        allCoinPrices = appDatabase.marketDao().getAllCoinPrices();


    }
}
