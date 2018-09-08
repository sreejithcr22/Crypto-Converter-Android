package com.codit.cryptoconverter.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.adapter.FavListAdapter;
import com.codit.cryptoconverter.db.FavPairsDB;
import com.codit.cryptoconverter.listener.FavPairDBOperationsListener;
import com.codit.cryptoconverter.listener.FavPairSelectedListener;
import com.codit.cryptoconverter.model.FavouritePair;

import java.util.ArrayList;
import java.util.List;

public class FavPairsListDialog extends DialogFragment {
    private RecyclerView recyclerView;
    private FavListAdapter favListAdapter;
    List<FavouritePair> favouritePairList = new ArrayList<>();
    private FavPairSelectedListener converterFragmentCallback;

    public static FavPairsListDialog newInstance(FavPairSelectedListener favPairSelectedListener) {
        FavPairsListDialog dialog = new FavPairsListDialog();
        dialog.setConverterFragmentCallback(favPairSelectedListener);
        return dialog;
    }

    private FavPairSelectedListener dialogCallback = new FavPairSelectedListener() {
        @Override
        public void onFavPairSelected(FavouritePair pair) {
            if (getDialog() != null) getDialog().dismiss();
            converterFragmentCallback.onFavPairSelected(pair);
        }
    };

    private void setConverterFragmentCallback(FavPairSelectedListener listener) {
        this.converterFragmentCallback = listener;
    }

    private FavPairDBOperationsListener favPairDBOperationsListener = new FavPairDBOperationsListener() {
        @Override
        public void onDbOpenFailed() {

        }

        @Override
        public void onGenericError(String errorMessage) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFavPairAdded() {

        }

        @Override
        public void onFavPairAddFailed(String errorMessage) {

        }

        @Override
        public void onFavPairsFetched(List<FavouritePair> fetchedList) {
            favouritePairList.clear();
            favouritePairList.addAll(fetchedList);
            for (FavouritePair favouritePair : favouritePairList) {
                Log.d("list", favouritePair.getConvertFromCurrency() + "-" + favouritePair.getConvertToCurrency());
            }
            favListAdapter.notifyDataSetChanged();

        }

        @Override
        public void onFavPairDeleted() {

        }

        @Override
        public void onFavPairDeleteFailed(String message) {

        }

        @Override
        public void onIsPairExistResult(boolean isExist) {

        }
    };


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fav_pais_list_dialog, null);
        recyclerView = dialogView.findViewById(R.id.fav_list_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        favListAdapter = new FavListAdapter(favouritePairList, getActivity(), dialogCallback);
        recyclerView.setAdapter(favListAdapter);


        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        return builder.create();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        populateList();
    }

    private void populateList() {
        FavPairsDB.getInstance(getContext()).getAllFavPairs(favPairDBOperationsListener);
    }
}
