package com.codit.cryptoconverter.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.codit.cryptoconverter.model.CoinPrices;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Sreejith on 22-Nov-17.
 */
@Dao
public interface MarketDao {

    @Query("Select * from CoinPrices")
    LiveData<List<CoinPrices>> getAllCoinPricesLive();

    @Query("Select * from CoinPrices")
    List<CoinPrices> getAllCoinPrices();

    @Query("Select * from CoinPrices where coinCode=:coinCode")
    CoinPrices getCoinPricesFor(String coinCode);


    @Insert(onConflict = REPLACE)
    void addCoinPrices(List<CoinPrices> prices);


}
