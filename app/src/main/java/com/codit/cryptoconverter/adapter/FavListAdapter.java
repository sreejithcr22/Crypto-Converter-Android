package com.codit.cryptoconverter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.db.FavPairsDB;
import com.codit.cryptoconverter.listener.FavPairDeleteCallback;
import com.codit.cryptoconverter.listener.FavPairSelectedListener;
import com.codit.cryptoconverter.model.FavouritePair;
import com.codit.cryptoconverter.util.Util;

import java.util.List;

public class FavListAdapter extends RecyclerView.Adapter<FavListAdapter.FavPairViewHolder> {
    private List<FavouritePair> currencyPairs;
    private Context context;
    private FavPairSelectedListener favPairSelectedListener;
    private static final String TAG = FavListAdapter.class.getSimpleName();

    public FavListAdapter(List<FavouritePair> currencyPairs, Context context, FavPairSelectedListener favPairSelectedListener) {
        this.currencyPairs = currencyPairs;
        this.context = context;
        this.favPairSelectedListener = favPairSelectedListener;
    }

    public void deleteFavPair(int pos) {
        currencyPairs.remove(currencyPairs.get(pos));
        notifyDataSetChanged();
    }

    private FavListAdapter getFavListAdapter() {
        return this;
    }


    @NonNull
    @Override
    public FavPairViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FavPairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_pair_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final FavPairViewHolder holder, int position) {
        FavouritePair pair = currencyPairs.get(position);
        String convertFromCurrencyName = Util.getCurrencyName(pair.getConvertFromCurrency());
        String convertToCurrencyName = Util.getCurrencyName(pair.getConvertToCurrency());
        Log.d(TAG, "setValues: " + String.valueOf(position) + " ->" + convertFromCurrencyName + "-" + convertToCurrencyName);
        holder.setValues(pair);
        holder.itemView.setTag(position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = (Integer) v.getTag();
                FavouritePair pair = currencyPairs.get(pos);
                FavPairsDB.getInstance(context.getApplicationContext()).deleteFavPair(pair, new FavPairDeleteCallback(pos, context, getFavListAdapter()));

                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favPairSelectedListener != null) {
                    FavouritePair pair = currencyPairs.get((Integer) v.getTag());
                    favPairSelectedListener.onFavPairSelected(pair);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencyPairs.size();
    }

    public class FavPairViewHolder extends RecyclerView.ViewHolder {
        TextView currency1Text, currency2Text;

        FavPairViewHolder(View itemView) {
            super(itemView);
            currency1Text = itemView.findViewById(R.id.currency1);
            currency2Text = itemView.findViewById(R.id.currency2);
        }


        void setValues(FavouritePair pair) {
            String convertFromCurrencyName = Util.getCurrencyName(pair.getConvertFromCurrency());
            String convertToCurrencyName = Util.getCurrencyName(pair.getConvertToCurrency());
            Log.d(TAG, "setValues: ->" + convertFromCurrencyName + "-" + convertToCurrencyName);
            if (convertFromCurrencyName != null && convertToCurrencyName != null) {
                currency1Text.setText(convertFromCurrencyName);
                currency2Text.setText(convertToCurrencyName);
            }
        }
    }
}
