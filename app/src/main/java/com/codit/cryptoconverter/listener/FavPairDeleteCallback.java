package com.codit.cryptoconverter.listener;

import android.content.Context;
import android.widget.Toast;

import com.codit.cryptoconverter.adapter.FavListAdapter;
import com.codit.cryptoconverter.model.FavouritePair;

import java.util.List;

public class FavPairDeleteCallback implements FavPairDBOperationsListener {
    private int adapterPos;
    private Context context;
    private FavListAdapter adapter;

    public FavPairDeleteCallback(int adapterPos, Context context, FavListAdapter adapter) {
        this.adapterPos = adapterPos;
        this.adapter = adapter;
        this.context = context;
    }

    public int getAdapterPos() {
        return adapterPos;
    }

    @Override
    public void onDbOpenFailed() {

    }

    @Override
    public void onGenericError(String errorMessage) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavPairAdded() {

    }

    @Override
    public void onFavPairAddFailed(String errorMessage) {

    }

    @Override
    public void onFavPairsFetched(List<FavouritePair> favouritePairList) {

    }

    @Override
    public void onFavPairDeleted() {
        Toast.makeText(context, "Favourite pair deleted!", Toast.LENGTH_SHORT).show();
        adapter.deleteFavPair(getAdapterPos());
    }

    @Override
    public void onFavPairDeleteFailed(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onIsPairExistResult(boolean isExist) {

    }
}
