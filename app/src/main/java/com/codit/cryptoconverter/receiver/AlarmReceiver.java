package com.codit.cryptoconverter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptoconverter.service.FetchMarketDataService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("wallet", "AlarmReceiver onReceive: ");
        Intent serviceIntent=new Intent(context.getApplicationContext(),FetchMarketDataService.class);
        context.getApplicationContext().startService(serviceIntent);
    }
}
