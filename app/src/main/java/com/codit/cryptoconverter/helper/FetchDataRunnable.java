package com.codit.cryptoconverter.helper;

import android.content.Context;
import android.util.Log;

import com.codit.cryptoconverter.listener.FetchDataCallback;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.UrlBuilder;

public class FetchDataRunnable implements Runnable {
    private static final String TAG = "FetchData";
    private int fsysStartIndex, fsysEndIndex, tosysStartIndex, tosysEndIndex;
    private String toSys;
    private int qPos;
    private FetchDataCallback fetchDataCallback;
    private Context context;

    public FetchDataRunnable(int fsysStartIndex, int fsysEndIndex, int tosysStartIndex, int tosysEndIndex, String toSys, int qPos, FetchDataCallback fetchDataCallback, Context context) {
        this.fsysStartIndex = fsysStartIndex;
        this.fsysEndIndex = fsysEndIndex;
        this.tosysStartIndex = tosysStartIndex;
        this.tosysEndIndex = tosysEndIndex;
        this.toSys = toSys;
        this.qPos = qPos;
        this.fetchDataCallback = fetchDataCallback;
        this.context = context;


    }

    @Override
    public void run() {
        Log.d(TAG, "FetchDataRunnable: fsysStartIndex=" + String.valueOf(fsysStartIndex) + ", fsysEndIndex=" + String.valueOf(fsysEndIndex) + ", tosysStartIndex=" + String.valueOf(tosysStartIndex) + ", tosysEndIndex=" + String.valueOf(tosysEndIndex));

        String fsysUrl = UrlBuilder.buildCryptoCurrencyList(fsysStartIndex, fsysEndIndex);
        String toSysUrl;
        if (toSys.equals(Constants.CURRENCY_TYPE_FIAT)) {
            toSysUrl = UrlBuilder.buildFiatCurrencyList(tosysStartIndex, tosysEndIndex);
        } else {
            toSysUrl = UrlBuilder.buildCryptoCurrencyList(tosysStartIndex, tosysEndIndex);
        }

        fetchDataCallback.executeApiCall(fsysUrl, toSysUrl);
        fetchDataCallback.onCurrentApiCallFinished(qPos);


    }
}

