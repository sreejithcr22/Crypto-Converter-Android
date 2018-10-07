package com.codit.cryptoconverter;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.codit.cryptoconverter.activity.MainActivity;
import com.codit.cryptoconverter.service.FetchMarketDataService;
import com.codit.cryptoconverter.util.Connectivity;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        //Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        initSession();
    }

    private void initSession() {

        try {
            Connectivity connectivity = new Connectivity(getApplicationContext());
            if (!connectivity.isConnected()) {
                Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, FetchMarketDataService.class);
            startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "initSession: " + e.getMessage());
        }

    }

    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            Intent restartIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(restartIntent);
        }
    };

}
