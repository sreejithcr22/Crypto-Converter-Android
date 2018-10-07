package com.codit.cryptoconverter.activity;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.fragment.ConverterFragment;
import com.codit.cryptoconverter.fragment.MarketFragment;
import com.codit.cryptoconverter.helper.SharedPreferenceManager;
import com.codit.cryptoconverter.listener.FragmentTransactionListener;
import com.codit.cryptoconverter.listener.OnCurrencySelectedListener;
import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.util.Connectivity;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.CryptoCurrency;
import com.codit.cryptoconverter.util.FiatCurrency;
import com.codit.cryptoconverter.viewmodel.MarketViewModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnCurrencySelectedListener, FragmentTransactionListener {

    SharedPreferenceManager sharedPreferenceManager;
    private SearchView searchView;
    private Toolbar toolbar;
    private MenuItem currency;
    private MenuItem btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.title_converter));
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ConverterFragment(), Constants.FRAGMENT_CONVERTER).commit();

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

        showDbDownloadProgress();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.change_currency:
                showChangeCurrencyDialog();
                return true;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        Log.d(getLocalClassName(), "onCreateOptionsMenu: ");

        currency = menu.findItem(R.id.change_currency);
        currency.setTitle(sharedPreferenceManager.getDefaultCurrency());
        btnSearch = menu.findItem(R.id.search);
        searchView = (SearchView) btnSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                MarketFragment marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_MARKET);
                if (marketFragment != null && marketFragment.isVisible()) {
                    marketFragment.onSearch(newText);
                    return true;
                }

                return true;
            }
        });

        hideToolbarMenuItems();
        return true;
    }


    void showChangeCurrencyDialog() {
        HashMap<String, String> fiatCurrencies = FiatCurrency.getCurrencyData();
        final String currencyCodes[] = new String[fiatCurrencies.size()];
        String currencyDisplayItems[] = new String[fiatCurrencies.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : fiatCurrencies.entrySet()) {
            currencyCodes[i] = entry.getKey();
            currencyDisplayItems[i] = entry.getKey() + " - " + entry.getValue();
            i++;
        }

        Log.d(getLocalClassName(), "showChangeCurrencyDialog: " + Arrays.toString(currencyDisplayItems));
        Arrays.sort(currencyDisplayItems);
        Arrays.sort(currencyCodes);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change currency")
                .setSingleChoiceItems(currencyDisplayItems, Arrays.binarySearch(currencyCodes, sharedPreferenceManager.getDefaultCurrency()), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferenceManager.setDefaultCurrency(currencyCodes[i]);
                        currency.setTitle(sharedPreferenceManager.getDefaultCurrency());
                        dialogInterface.dismiss();

                        MarketFragment marketFragment = (MarketFragment) getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_MARKET);
                        if (marketFragment != null && marketFragment.isVisible()) {
                            marketFragment.refreshList();
                        }
                    }
                });

        builder.setNegativeButton("Cancel", null)
                .create().show();
    }

    @Override
    public void onCurrencySelected(SpinnerItem item, int textViewId) {
        ConverterFragment converterFragment = (ConverterFragment) getSupportFragmentManager().findFragmentByTag(Constants.FRAGMENT_CONVERTER);
        if (converterFragment != null) {
            converterFragment.updateCurrencySelection(item, textViewId);
        }
    }

    @Override
    public void onFragmentTransaction(String tag) {

        try {
            switch (tag) {
                case Constants.FRAGMENT_CONVERTER:
                    Log.d(getLocalClassName(), "onFragmentTransaction: converter visible");
                    toolbar.setTitle(getResources().getString(R.string.title_converter));
                    hideToolbarMenuItems();
                    return;
                case Constants.FRAGMENT_MARKET:
                    Log.d(getLocalClassName(), "onFragmentTransaction: market visible");
                    toolbar.setTitle(getResources().getString(R.string.title_market));
                    showToolbarMenuItems();
                    return;
                case Constants.FRAGMENT_SETTINGS:
                    Log.d(getLocalClassName(), "onFragmentTransaction: settings visible");
                    toolbar.setTitle(getResources().getString(R.string.title_settings));
                    hideToolbarMenuItems();
                    return;

            }
        } catch (Exception e) {
            Log.e(getLocalClassName(), "onFragmentTransaction: " + e.toString());
        }


    }

    private void showToolbarMenuItems() {
        Log.d(getLocalClassName(), "showToolbarMenuItems: ");
        if (currency != null && btnSearch != null) {
            currency.setVisible(true);
            btnSearch.setVisible(true);
        }
    }

    private void hideToolbarMenuItems() {
        Log.d(getLocalClassName(), "hideToolbarMenuItems: ");
        if (currency != null && btnSearch != null) {
            currency.setVisible(false);
            btnSearch.setVisible(false);
        }
    }

    private void showDbDownloadProgress() {
        if (sharedPreferenceManager.isInitialDataDownloaded() || !new Connectivity(getApplicationContext()).isConnected()) {
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_message));
        progressDialog.setCancelable(false);

        MarketViewModel marketViewModel = ViewModelProviders.of(this).get(MarketViewModel.class);
        marketViewModel.getAllCoinPricesLive().observe(this, new Observer<List<CoinPrices>>() {
            @Override
            public void onChanged(@Nullable List<CoinPrices> updatedPrices) {
                Log.d("showDbDownloadProgress", "onChanged: size=" + updatedPrices.size());
                if (updatedPrices.size() == CryptoCurrency.getCryptoCurrencyData().size()) {
                    Log.d("showDbDownloadProgress", "onChanged: prices size=" + String.valueOf(updatedPrices.get(updatedPrices.size() - 1).getPrices().size()));
                    if (updatedPrices.get(updatedPrices.size() - 1).getPrices().size() ==
                            CryptoCurrency.getCryptoCurrencyData().size() + FiatCurrency.getCurrencyData().size()) {
                        sharedPreferenceManager.setIsInitialDataDownloaded(true);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.data_download_success), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.data_download_failure), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 3 * 60 * 1000);
                }
            }
        });
    }


}
