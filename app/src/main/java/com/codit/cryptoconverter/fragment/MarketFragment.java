package com.codit.cryptoconverter.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.ad.AdHelper;
import com.codit.cryptoconverter.adapter.MarketRecyclerAdapter;
import com.codit.cryptoconverter.listener.RecyclerviewSearchListener;
import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.viewmodel.MarketViewModel;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment  implements RecyclerviewSearchListener{

    private RecyclerView marketRecyclerView;
    private MarketRecyclerAdapter marketRecyclerAdapter;
    private List<CoinPrices> coinPricesList;
    public MarketFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coinPricesList=new ArrayList<>();
        AdHelper.getInstance(getActivity()).showAd(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView=inflater.inflate(R.layout.fragment_market, container, false);
        marketRecyclerView= rootView.findViewById(R.id.marketRecyclerView);
        return rootView;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        marketRecyclerView.setLayoutManager(layoutManager);
        marketRecyclerAdapter=new MarketRecyclerAdapter(coinPricesList,getContext());
        marketRecyclerView.setAdapter(marketRecyclerAdapter);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(
                marketRecyclerView.getContext(),
                layoutManager.getOrientation()
        );
        marketRecyclerView.addItemDecoration(mDividerItemDecoration);



        MarketViewModel marketViewModel= ViewModelProviders.of(this).get(MarketViewModel.class);
        marketViewModel.getAllCoinPricesLive().observe(getViewLifecycleOwner(), new Observer<List<CoinPrices>>() {
            @Override
            public void onChanged(@Nullable List<CoinPrices> updatedPrices) {
                assert updatedPrices != null;
                marketRecyclerAdapter.updateData(updatedPrices);
            }
        });
    }


    public void onSearch(String searchString) {
        marketRecyclerAdapter.getFilter().filter(searchString);

    }

    public void refreshList()
    {
        marketRecyclerAdapter.notifyDataSetChanged();
    }


}
