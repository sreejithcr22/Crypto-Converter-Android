package com.codit.cryptoconverter.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.adapter.SpinnerAdapter;
import com.codit.cryptoconverter.listener.OnCurrencySelectedListener;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.util.Coin;
import com.codit.cryptoconverter.util.Currency;
import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConverterFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private EditText edit1,edit2;
    private Spinner spinner1,spinner2;
    private static final String TAG = "spinner";

    public ConverterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_converter, container, false);
        edit1 = view.findViewById(R.id.edit1);
        edit2 = view.findViewById(R.id.edit2);
        spinner1 = view.findViewById(R.id.spinner1);
        spinner2 = view.findViewById(R.id.spinner2);



        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*List<SpinnerItem> items = new ArrayList<>();
        for (Map.Entry <String,String> entry : Currency.getCurrencyData().entrySet()) {

            items.add(new SpinnerItem(entry.getKey(),entry.getValue(),SpinnerItem.CURRENCY_TYPE_FIAT));
        }
        for (Map.Entry <String,String> entry : Coin.getCoinsData().entrySet()) {

            items.add(new SpinnerItem(entry.getKey(),entry.getValue(),SpinnerItem.CURRENCY_TYPE_CRYPTO));
        }

        SpinnerAdapter adapter1 = new SpinnerAdapter(getActivity(),R.layout.spinner_item_row,R.id.spinner_item_row_text,items);
        SpinnerAdapter adapter2 =new SpinnerAdapter(getActivity(),R.layout.spinner_item_row,R.id.spinner_item_row_text,items);


        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);

        spinner1.setOnItemSelectedListener(this);
        spinner2.setOnItemSelectedListener(this);*/


        SpinnerDialog dialog = new SpinnerDialog();
        dialog.show(getActivity().getFragmentManager(),"");



        }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId())
        {
            case R.id.spinner1:
                Log.d(TAG, "onItemSelected: spinner1 - "+adapterView.getAdapter().getItem(i).toString());
                break;
            case R.id.spinner2:
                Log.d(TAG, "onItemSelected: spinner2 - "+adapterView.getAdapter().getItem(i).toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void updateCurrencySelection(SpinnerItem item) {
        Log.d(TAG, "updateCurrencySelection: "+item.getCurrencyName());
    }
}

