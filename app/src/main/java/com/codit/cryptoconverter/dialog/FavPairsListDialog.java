package com.codit.cryptoconverter.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.adapter.FavListAdapter;
import com.codit.cryptoconverter.db.FavPairsDB;
import com.codit.cryptoconverter.listener.FavDialogOperationsListener;
import com.codit.cryptoconverter.listener.FavPairDBOperationsListener;
import com.codit.cryptoconverter.model.FavouritePair;

import java.util.ArrayList;
import java.util.List;

public class FavPairsListDialog extends DialogFragment {
    private RecyclerView recyclerView;
    private FavListAdapter favListAdapter;
    private TextView listEmptyMessageText;
    List<FavouritePair> favouritePairList = new ArrayList<>();
    private FavDialogOperationsListener converterFragmentCallback;

    public static FavPairsListDialog newInstance(FavDialogOperationsListener favDialogOperationsListener) {
        FavPairsListDialog dialog = new FavPairsListDialog();
        dialog.setConverterFragmentCallback(favDialogOperationsListener);
        return dialog;
    }

    private FavDialogOperationsListener dialogCallback = new FavDialogOperationsListener() {
        @Override
        public void onFavPairSelected(FavouritePair pair) {
            converterFragmentCallback.onFavPairSelected(pair);
            if (getDialog() != null) getDialog().dismiss();

        }

        @Override
        public void onFavDialogDismissed() {

        }
    };

    private void setConverterFragmentCallback(FavDialogOperationsListener listener) {
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
            if (fetchedList.isEmpty()) {
                listEmptyMessageText.setVisibility(View.VISIBLE);
            }
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
        listEmptyMessageText = dialogView.findViewById(R.id.fav_list_empty_message);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        favListAdapter = new FavListAdapter(favouritePairList, getActivity(), dialogCallback, listEmptyMessageText);
        recyclerView.setAdapter(favListAdapter);


        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.dialog_cancel_btn_text, null);
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        converterFragmentCallback.onFavDialogDismissed();

    }
}
