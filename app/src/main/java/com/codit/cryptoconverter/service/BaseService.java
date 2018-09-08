package com.codit.cryptoconverter.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.codit.cryptoconverter.db.MarketDao;
import com.codit.cryptoconverter.util.Connectivity;


public abstract class BaseService extends IntentService {

    public static final String REPORT_STATUS = "report_status";
    public static final String REPORT_DATA = "report_data";
    public static final String ERROR_MESSAGE_BAD_REQUEST = "Invalid address";
    public static final String ERROR_MESSAGE_NO_INTERNET = "No internet connection";
    public static final String ERROR_REQUEST_TIME_OUT = "Connection timed out, please try again";
    public static final String ERROR_UNKNOWN = "Sorry, something went wrong";
    public static final String SUCCESS_WALLET_ADDED = "Wallet added successfully";
    public static final String ERRROR_DUPLICATE_WALLET = "Wallet name already exists";
    public static final String IS_ERROR = "is-error";
    public static final String REPORT_TYPE = "report_type";
    public static final String REPOT_TYPE_SUCCESS = "report_success";
    public static final String REPOT_TYPE_FAILURE = "report_failure";
    public static final String REPORT_TYPE_WARNING = "report_warning";
    public static final String EXTRA_SHOULD_IGNORE_WALLET_REFRESH = "ignore_wallet_refresh";
    public static final String EXTRA_SHOULD_START_NOTIFICATION = "start_notification";
    public static final String EXTRA_SHOULD_UPDATE_WALLET_WORTH = "update_wallet_worth";
    private final static String BASE_URL_BLOCKCYPHER = "https://api.blockcypher.com/v1/";
    private final static String BASE_URL_BCASH = "https://blockdozer.com/insight-api/";
    private final static String BASE_URL_RIPPLE = "https://data.ripple.com/v2/accounts/";
    public MarketDao marketDao;

    public BaseService() {
        super("BaseService");
    }




    void reportStatus(String message, String reportType) {
        Connectivity connectivity = new Connectivity(this.getApplicationContext());
        if (reportType.equals(REPOT_TYPE_FAILURE) && !connectivity.isConnected()) {
            message = ERROR_MESSAGE_NO_INTERNET;
        }
        Intent intent = new Intent(REPORT_STATUS);
        intent.putExtra(REPORT_DATA, message);
        intent.putExtra(REPORT_TYPE, reportType);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
