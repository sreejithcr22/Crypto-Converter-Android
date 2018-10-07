package com.codit.cryptoconverter.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Sreejith on 28-Nov-17.
 */

public class SharedPreferenceManager {

    public static final String CRYPTO_WATCH = "crypto_watch_wallet";
    public static final String RATE_APP = "settings_rate_us";
    public static final String CONTACT_US = "settings_contact_us";
    public static final String SHARE_APP = "settings_share_app";
    public static final String CREDITS = "settings_credits";
    public static final String SESSION_COUNT = "session_count";
    public static final String FAV_DELETE_MESSAGE_SHOWN = "fav_delete_message_shown";
    public static final String IS_INITIAL_DATA_DOWNLOADED = "is_data_downloaded";

    SharedPreferences preferenceManager;

    public SharedPreferenceManager(Context context) {
        preferenceManager = android.preference.PreferenceManager.getDefaultSharedPreferences(context);

    }

    public String getDefaultCurrency() {
        return preferenceManager.getString(CRYPTO_WATCH, "USD");
    }

    public void setDefaultCurrency(String currency) {
        preferenceManager.edit().putString(CRYPTO_WATCH, currency).apply();
    }

    public void setFavDeleteMessageShown(boolean isShown) {
        preferenceManager.edit().putBoolean(FAV_DELETE_MESSAGE_SHOWN, isShown).apply();
    }

    public boolean isFavDeleteMessageShown() {
        return preferenceManager.getBoolean(FAV_DELETE_MESSAGE_SHOWN, false);
    }

    public void setIsInitialDataDownloaded(boolean isInitialDataDownloaded) {
        preferenceManager.edit().putBoolean(IS_INITIAL_DATA_DOWNLOADED, isInitialDataDownloaded).commit();
    }

    public boolean isInitialDataDownloaded() {
        return preferenceManager.getBoolean(IS_INITIAL_DATA_DOWNLOADED, false);
    }
}
