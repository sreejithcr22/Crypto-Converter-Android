package com.codit.cryptoconverter.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.adapter.SpinnerAdapter;
import com.codit.cryptoconverter.listener.OnCurrencySelectedListener;
import com.codit.cryptoconverter.listener.OnSpinnerItemClickListener;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.CryptoCurrency;
import com.codit.cryptoconverter.util.FiatCurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by Sreejith on 03-Mar-18.
 */

public class SpinnerDialog extends DialogFragment {

    private static final String TAG = "search";
    private OnCurrencySelectedListener onCurrencySelectedListener;
    private int currentSelectedLabelId = -1;
    private OnSpinnerItemClickListener onSpinnerItemClickListener = new OnSpinnerItemClickListener() {
        @Override
        public void onSpinnerItemClick(SpinnerItem item) {
            Log.d(TAG, "onSpinnerItemClick: " + item.getCurrencyName());
            if (getDialog() != null) {
                if (onCurrencySelectedListener != null) {
                    onCurrencySelectedListener.onCurrencySelected(item, currentSelectedLabelId);
                }
                getDialog().dismiss();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCurrencySelectedListener = (OnCurrencySelectedListener) getActivity();
        Bundle args = getArguments();
        if (args != null) {
            currentSelectedLabelId = args.getInt(Constants.EXTRA_CURRENCY_TEXTVIEW_ID, -1);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        List<SpinnerItem> items = new ArrayList<>();

        for (Map.Entry<String, String> entry : CryptoCurrency.getCryptoCurrencyData().entrySet()) {

            items.add(new SpinnerItem(entry.getKey(), entry.getValue(), SpinnerItem.CURRENCY_TYPE_CRYPTO));
        }

        for (Map.Entry<String, String> entry : FiatCurrency.getCurrencyData().entrySet()) {

            items.add(new SpinnerItem(entry.getKey(), entry.getValue(), SpinnerItem.CURRENCY_TYPE_FIAT));
        }

        Collections.sort(items, new Comparator<SpinnerItem>() {
            @Override
            public int compare(SpinnerItem o1, SpinnerItem o2) {
                return o1.getCurrencyName().compareTo(o2.getCurrencyName());
            }
        });


        final SpinnerAdapter adapter = new SpinnerAdapter(items, onSpinnerItemClickListener);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_dialog_layout, null);
        SearchView searchView = view.findViewById(R.id.dialog_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return true;
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.spinner_dialog_listview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(),
                layoutManager.getOrientation()
        );
        recyclerView.addItemDecoration(mDividerItemDecoration);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_currency_dialog_title))
                .setView(view)
                .setPositiveButton(R.string.dialog_cancel_btn_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                });


        return builder.create();


    }


}
