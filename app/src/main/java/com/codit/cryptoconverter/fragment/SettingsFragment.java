package com.codit.cryptoconverter.fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.helper.SharedPreferenceManager;
import com.codit.cryptoconverter.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Sreejith on 28-Nov-17.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    SharedPreferenceManager helper;
    Preference cryptoWatch, rateUs, shareApp, contactUs, credits;

    @Override
    public boolean onPreferenceClick(Preference preference) {

        switch (preference.getKey()) {
            case SharedPreferenceManager.CRYPTO_WATCH:
                launchPlayStore(Constants.CRYPTO_WATCH_WALLET_PACKAGE);
                return true;
            case SharedPreferenceManager.RATE_APP:
                launchPlayStore(getContext().getPackageName());
                return true;
            case SharedPreferenceManager.SHARE_APP:
                shareApp();
                return true;
            case SharedPreferenceManager.CONTACT_US:
                sendFeedbackMail();
                return true;
            case SharedPreferenceManager.CREDITS:
                showCreditsDialog();
                return true;
        }
        return false;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d("preference", "onCreatePreferences: ");
        addPreferencesFromResource(R.xml.settings);
        Log.d("preference", "oncreate: ");
        helper = new SharedPreferenceManager(getActivity().getApplicationContext());
        Log.d("preference", "onCreatePreferences: " + getPreferenceScreen().getPreference(0).getKey());

        cryptoWatch = getPreferenceManager().findPreference(SharedPreferenceManager.CRYPTO_WATCH);
        rateUs = getPreferenceManager().findPreference(SharedPreferenceManager.RATE_APP);
        shareApp = getPreferenceManager().findPreference(SharedPreferenceManager.SHARE_APP);
        contactUs = getPreferenceManager().findPreference(SharedPreferenceManager.CONTACT_US);
        credits = getPreferenceManager().findPreference(SharedPreferenceManager.CREDITS);


        cryptoWatch.setOnPreferenceClickListener(this);
        rateUs.setOnPreferenceClickListener(this);
        shareApp.setOnPreferenceClickListener(this);
        contactUs.setOnPreferenceClickListener(this);
        credits.setOnPreferenceClickListener(this);


    }

    private void launchPlayStore(String appPackage) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + getContext().getPackageName());
        startActivity(intent);

    }

    private void sendFeedbackMail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "codit.apps@gmail.com", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback: CryptoConverter");
        startActivity(Intent.createChooser(intent, "Send feedback"));

    }

    private void showDonationDialog() {
        final LinkedHashMap<String, String> addressesMap = new LinkedHashMap<>();
        addressesMap.put("Bitcoin", getString(R.string.bitcoin_address));
        addressesMap.put("Litecoin", getString(R.string.lite_address));
        addressesMap.put("Ripple", getString(R.string.ripple_address));
        addressesMap.put("Ethereum", getString(R.string.eth_address));
        final String[] currencies = new String[addressesMap.size()], addresses = new String[addressesMap.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : addressesMap.entrySet()) {
            currencies[i] = entry.getKey();
            addresses[i] = entry.getValue();
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Copy address")
                .setItems(currencies, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        copyToClipboard(addresses[i]);
                        Toast.makeText(getActivity(), "Address copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Dismiss", null)
                .create().show();
    }

    private void copyToClipboard(String address) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            //clipboard.setText(address);
            ClipData clipData = ClipData.newPlainText("address", address);
            clipboard.setPrimaryClip(clipData);
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), "Address could not be copied !", Toast.LENGTH_SHORT).show();
        }

    }

    private void showCreditsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Credits")
                .setMessage(getResources().getString(R.string.credits))
                .setPositiveButton("Ok",null)
                .create()
                .show();
    }

}
