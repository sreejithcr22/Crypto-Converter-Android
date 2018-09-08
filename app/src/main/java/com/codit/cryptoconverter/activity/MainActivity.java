package com.codit.cryptoconverter.activity;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.fragment.ConverterFragment;
import com.codit.cryptoconverter.fragment.MarketFragment;
import com.codit.cryptoconverter.fragment.SettingsFragment;
import com.codit.cryptoconverter.helper.SharedPreferenceManager;
import com.codit.cryptoconverter.listener.OnCurrencySelectedListener;
import com.codit.cryptoconverter.model.SpinnerItem;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SettingsFragment.onCurrencyPreferenceClickListener, OnCurrencySelectedListener {

    private static final String FRAGMENT_MARKET = "market_fragment";
    private static final String FRAGMENT_CONVERTER = "converter_fragment";
    private static final String FRAGMENT_SETTINGS = "settings_fragment";
    MarketFragment marketFragment;
    SharedPreferenceManager sharedPreferenceManager;
    private  SearchView searchView ;
    private Toolbar toolbar;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            searchView.setVisibility(View.GONE);
            switch (item.getItemId()) {

                case R.id.navigation_converter:
                    getSupportActionBar().setTitle("Converter");
                    if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONVERTER) == null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ConverterFragment(), FRAGMENT_CONVERTER).commit();
                    return true;

                case R.id.navigation_market:
                    searchView.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("Coins");
                    if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET) == null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MarketFragment(), FRAGMENT_MARKET).commit();

                    return true;
                case R.id.navigation_settings:
                    getSupportActionBar().setTitle("Settings");
                    if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_SETTINGS) == null)
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment(), FRAGMENT_SETTINGS).commit();

                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("mainactivity", "onCreate: ");

        sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Wallets");
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ConverterFragment(), FRAGMENT_CONVERTER).commit();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;


        String densityString = "cannot determine";
        if (density == DisplayMetrics.DENSITY_XXHIGH) {

            densityString = "xxhdpi";
        } else if (density == DisplayMetrics.DENSITY_XHIGH) {
            densityString = "xhdpi";

        } else if (density == DisplayMetrics.DENSITY_HIGH) {
            densityString = "hdpi";

        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {

            densityString = "mdpi";
        } else if (density == DisplayMetrics.DENSITY_LOW) {

            densityString = "ldpi";
        }


        Configuration config = getResources().getConfiguration();


        Log.d("dimens", "density= " + densityString);
        Log.d("dimens", "small width= " + String.valueOf(config.smallestScreenWidthDp));
        Log.d("dimens", "width x height " + String.valueOf(metrics.widthPixels) + " x " + String.valueOf(metrics.heightPixels));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET);
                if (marketFragment != null && marketFragment.isVisible()) {
                    marketFragment.onSearch(newText);
                    return true;
                }

                return true;
            }
        });

        return true;
    }


    void showChangeCurrencyDialog() {

        final String currencies[] = getApplicationContext().getResources().getStringArray(R.array.fiat_currencies);
        Arrays.sort(currencies);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change currency")
                .setSingleChoiceItems(currencies, Arrays.binarySearch(currencies, sharedPreferenceManager.getDefaultCurrency()), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferenceManager.setDefaultCurrency(currencies[i]);
                        dialogInterface.dismiss();
                        /*Intent intent = new Intent(MainActivity.this, UpdateWalletsWorthService.class);
                        startService(intent);*/
                        marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_MARKET);
                        if (marketFragment != null && marketFragment.isVisible()) {
                            marketFragment.refreshList();
                        }
                    }
                });

        builder.setNegativeButton("Cancel", null)
                .create().show();
    }

    @Override
    public void onCurrencyPreferenceClick() {
        showChangeCurrencyDialog();
    }

    @Override
    public void onCurrencySelected(SpinnerItem item, int textViewId) {
        ConverterFragment converterFragment = (ConverterFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_CONVERTER);
        if (converterFragment != null) {
            converterFragment.updateCurrencySelection(item, textViewId);
        }
    }

}
