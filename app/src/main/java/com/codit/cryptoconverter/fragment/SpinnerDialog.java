package com.codit.cryptoconverter.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.adapter.SpinnerAdapter;
import com.codit.cryptoconverter.listener.OnCurrencySelectedListener;
import com.codit.cryptoconverter.listener.OnSpinnerItemClickListener;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.util.Coin;
import com.codit.cryptoconverter.util.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sreejith on 03-Mar-18.
 */

public class SpinnerDialog extends DialogFragment {

    private static final String TAG = "search";
    private OnCurrencySelectedListener onCurrencySelectedListener ;
    private OnSpinnerItemClickListener onSpinnerItemClickListener = new OnSpinnerItemClickListener() {
        @Override
        public void onSpinnerItemClick(SpinnerItem item) {
            Log.d(TAG, "onSpinnerItemClick: "+item.getCurrencyName());
            if(getDialog()!=null) {
                if(onCurrencySelectedListener != null) {
                    onCurrencySelectedListener.onCurrencySelected(item);
                }
                getDialog().dismiss();
            }
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       onCurrencySelectedListener = (OnCurrencySelectedListener) getActivity();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        List<SpinnerItem> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : Currency.getCurrencyData().entrySet()) {

            items.add(new SpinnerItem(entry.getKey(), entry.getValue(), SpinnerItem.CURRENCY_TYPE_FIAT));
        }
        for (Map.Entry<String, String> entry : Coin.getCoinsData().entrySet()) {

            items.add(new SpinnerItem(entry.getKey(), entry.getValue(), SpinnerItem.CURRENCY_TYPE_CRYPTO));
        }

        final SpinnerAdapter adapter = new SpinnerAdapter(getActivity(), items, onSpinnerItemClickListener);
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
                .setView(view);


        return builder.create();


    }


}
