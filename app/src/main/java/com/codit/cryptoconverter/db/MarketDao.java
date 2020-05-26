package com.codit.cryptoconverter.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.codit.cryptoconverter.model.CoinPrices;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

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
