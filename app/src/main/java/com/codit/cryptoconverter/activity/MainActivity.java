package com.codit.cryptoconverter.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.ad.AdHelper;
import com.codit.cryptoconverter.db.MarketDB;
import com.codit.cryptoconverter.fragment.ConverterFragment;
import com.codit.cryptoconverter.fragment.MarketFragment;
import com.codit.cryptoconverter.fragment.SettingsFragment;
import com.codit.cryptoconverter.helper.SharedPreferenceManager;
import com.codit.cryptoconverter.listener.ConverterFragmentCallback;
import com.codit.cryptoconverter.listener.FragmentTransactionListener;
import com.codit.cryptoconverter.listener.MarketDbCallback;
import com.codit.cryptoconverter.listener.OnCurrencySelectedListener;
import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.receiver.ProgressReceiver;
import com.codit.cryptoconverter.util.Connectivity;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.CryptoCurrency;
import com.codit.cryptoconverter.util.FiatCurrency;
import com.codit.cryptoconverter.viewmodel.MarketViewModel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity implements OnCurrencySelectedListener, FragmentTransactionListener {

    SharedPreferenceManager sharedPreferenceManager;
    private SearchView searchView;
    private Toolbar toolbar;
    private MenuItem currency;
    private MenuItem btnSearch;
    private MenuItem btnMore;
    private MenuItem paste;
    private ProgressDialog progressDialog;
    private ProgressReceiver progressReceiver;
    private ConverterFragmentCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
        sharedPreferenceManager.setSessionCount(sharedPreferenceManager.getSessionCount() + 1);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.title_converter));
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ConverterFragment(), Constants.FRAGMENT_CONVERTER).commit();
        setupProgressBar();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.change_currency:
                showChangeCurrencyDialog();
                return true;
            case R.id.more:
                onMoreMenuClicked();
                return true;
            case R.id.paste:
                onPasteMenuClicked();
                return true;
        }

        return false;
    }

    private void onPasteMenuClicked() {

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            String pasteData = item.getText().toString();
            try {
                final BigDecimal bigDecimal = new BigDecimal(pasteData);
                AlertDialog.Builder builder  = new AlertDialog.Builder(this);
                builder.setMessage("Paste \""+bigDecimal.toPlainString()+"\" to converter?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.pasteClipboard(bigDecimal);
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", null);
                builder.show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Clipboard does not contain a valid number!", Toast.LENGTH_SHORT).show();
            }
        }
       else {
           Toast.makeText(this, "Clipboard empty!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        Log.d(getLocalClassName(), "onCreateOptionsMenu: ");

        currency = menu.findItem(R.id.change_currency);
        currency.setTitle(sharedPreferenceManager.getDefaultCurrency());
        btnSearch = menu.findItem(R.id.search);
        btnMore = menu.findItem(R.id.more);
        paste = menu.findItem(R.id.paste);
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
    public void onFragmentTransaction(String tag, Fragment fragment) {

        try {
            switch (tag) {
                case Constants.FRAGMENT_CONVERTER:
                    callback = (ConverterFragment) fragment;
                    Log.d(getLocalClassName(), "onFragmentTransaction: converter visible");
                    toolbar.setTitle(getResources().getString(R.string.title_converter));
                    hideToolbarMenuItems();
                    btnMore.setVisible(true);
                    paste.setVisible(true);
                    return;
                case Constants.FRAGMENT_MARKET:
                    Log.d(getLocalClassName(), "onFragmentTransaction: market visible");
                    toolbar.setTitle(getResources().getString(R.string.title_market));
                    showToolbarMenuItems();
                    btnMore.setVisible(true);
                    paste.setVisible(false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(progressReceiver);
        }
    }

    public void setupProgressBar() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_message));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        final Context context = this;
        MarketDB.getInstance().getDbStatus(this, new MarketDbCallback() {
            @Override
            public void onDbStatus(boolean isEmpty) {
                Log.d("setupProgressBar", "isEmpty = "+isEmpty);
                if (isEmpty) {
                    progressReceiver = new ProgressReceiver(progressDialog);
                    LocalBroadcastManager.getInstance(context).registerReceiver(progressReceiver,
                            new IntentFilter(ProgressReceiver.PROGRESS_ACTION));
                } else {
                    AdHelper.getInstance(getApplicationContext()).showAd(context);
                }
            }
        });
    }

    private void onMoreMenuClicked() {
        toolbar.setTitle(getResources().getString(R.string.title_settings));
        hideToolbarMenuItems();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new SettingsFragment(), Constants.FRAGMENT_SETTINGS).addToBackStack(null).commit();
        btnMore.setVisible(false);
        paste.setVisible(false);

    }
}
