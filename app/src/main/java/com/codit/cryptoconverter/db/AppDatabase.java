package com.codit.cryptoconverter.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.model.FavouritePair;

/**
 * Created by Sreejith on 22-Nov-17.
 */
@Database(entities = {CoinPrices.class, FavouritePair.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase instance;

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_db")
                            .build();
        }
        return instance;
    }

    public abstract MarketDao marketDao();

    public abstract FavPairDao favPairDao();
}
