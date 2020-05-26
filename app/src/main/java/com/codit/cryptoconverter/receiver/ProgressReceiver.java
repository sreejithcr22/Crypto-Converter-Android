package com.codit.cryptoconverter.receiver;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ProgressReceiver extends BroadcastReceiver {

    private ProgressDialog dialog;
    public static final String PROGRESS_ACTION = "progress_action";
    public static final String CURRENT_PROGRESS = "current_progress";

    public ProgressReceiver(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int progress = intent.getIntExtra(CURRENT_PROGRESS, 100);
        Log.d("ProgressReceiver", "progress = " + progress);
        if (progress >= 100) dialog.dismiss();
        else  {
            dialog.show();
            dialog.setProgress(progress);
        }

    }
}
