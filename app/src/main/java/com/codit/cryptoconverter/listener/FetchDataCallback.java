package com.codit.cryptoconverter.listener;

public interface FetchDataCallback {
    void onCurrentApiCallFinished(int queuePos);

    void executeApiCall(String fsysUrl, String tosysUrl);
}
