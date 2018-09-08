package com.codit.cryptoconverter.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.codit.cryptoconverter.model.FavouritePair;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.FAIL;

@Dao
public interface FavPairDao {

    @Query("select * from FavouritePair")
    List<FavouritePair> getAllFavPairs();

    @Insert(onConflict = FAIL)
    long addFavPair(FavouritePair pair);

    @Delete
    int deleteFavPair(FavouritePair pair);

    @Query("Select * from FavouritePair where convertFromCurrency=:convertFromCurrency and convertToCurrency=:convertToCurrency")
    List<FavouritePair> isPairExist(String convertFromCurrency, String convertToCurrency);
}
