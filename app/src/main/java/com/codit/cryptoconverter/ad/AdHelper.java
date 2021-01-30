package com.codit.cryptoconverter.ad;

import android.content.Context;

import com.codit.cryptoconverter.helper.SharedPreferenceManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;


public class AdHelper {

    private static AdHelper instance;
    private InterstitialAd mInterstitialAd;
    private static final String AD_ID = "a-app-pub-3940256099942544/1033173712";
    private static final String TEST_ID = "ca-app-pub-3940256099942544/1033173712";
    private AdListener adListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            mInterstitialAd.show();
        }
    };


    private AdHelper(Context context) {
        MobileAds.initialize(context);

    }

    public static AdHelper getInstance(Context context) {
        if (instance == null) instance = new AdHelper(context);
        return instance;
    }

    public void showAd(Context context) {
        SharedPreferenceManager manager = new SharedPreferenceManager(context);
        if (manager.getSessionCount() > 1) {
            mInterstitialAd = new InterstitialAd(context);
            mInterstitialAd.setAdUnitId(AD_ID);
            mInterstitialAd.setAdListener(adListener);
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }


}
