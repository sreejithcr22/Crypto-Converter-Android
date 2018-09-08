package com.codit.cryptoconverter;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptoconverter.helper.SharedPreferenceManager;
import com.codit.cryptoconverter.service.FetchMarketDataService;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class App extends Application {
    private static final String TAG="app";
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        sharedPreferenceManager =new SharedPreferenceManager(getApplicationContext());
        initSession();
    }

    private void initSession() {

        sharedPreferenceManager.setSessionCount(sharedPreferenceManager.getSessionCount() + 1);
        Intent intent = new Intent(this, FetchMarketDataService.class);
        startService(intent);

       /* if (!SharedPreferenceManager.SESSION_COUNT_UPDATED) {

            SharedPreferenceManager.SESSION_COUNT_UPDATED = true;

        }*/
        Log.d(TAG, "session count=" + sharedPreferenceManager.getSessionCount());
    }

}
