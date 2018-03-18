package com.codit.cryptoconverter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.listener.OnSpinnerItemClickListener;
import com.codit.cryptoconverter.model.SpinnerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sreejith on 03-Mar-18.
 */

public class SpinnerAdapter extends RecyclerView.Adapter<SpinnerAdapter.SpinnerViewHolder> implements Filterable {
    private static final String TAG = "search";
    private Context mContext;
    private List<SpinnerItem> spinnerItems = new ArrayList<>();
    private List<SpinnerItem> spinnerItemsCopy = new ArrayList<>();
    private static OnSpinnerItemClickListener onSpinnerItemClickListener;

    public SpinnerAdapter(Context mContext, List<SpinnerItem> spinnerItems, OnSpinnerItemClickListener listener) {
        this.mContext = mContext;
        this.spinnerItems = spinnerItems;
        this.spinnerItemsCopy = spinnerItems;
        this.onSpinnerItemClickListener = listener;
    }

    @NonNull
    @Override
    public SpinnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item_row, parent, false);
        return new SpinnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpinnerViewHolder holder, int position) {
        holder.setData(spinnerItems.get(position));
    }

    @Override
    public int getItemCount() {

        return spinnerItems.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                String searchString = charSequence.toString().toLowerCase();
                if (searchString.isEmpty()) {
                    filterResults.values = spinnerItemsCopy;
                } else {
                    List<SpinnerItem> filteredList = new ArrayList<>();
                    for (SpinnerItem spinnerItem : spinnerItemsCopy) {
                        if (spinnerItem.getCurrencyName().toLowerCase().contains(searchString) ||
                                spinnerItem.getCurrencyCode().toLowerCase().contains(searchString)) {
                            filteredList.add(spinnerItem);
                        }
                    }
                    filterResults.values = filteredList;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null) {
                    spinnerItems = (List<SpinnerItem>) filterResults.values;
                }
                notifyDataSetChanged();
            }
        };
    }

    static class SpinnerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView listRow;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            listRow = itemView.findViewById(R.id.spinner_item_row_text);
        }

        private void setData(SpinnerItem spinnerItem) {
            String value = spinnerItem.getCurrencyName() + " (" + spinnerItem.getCurrencyCode() + ")";
            listRow.setText(value);
            this.itemView.setTag(spinnerItem);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SpinnerItem item = (SpinnerItem) v.getTag();
            if(item!=null) {
                Log.d(TAG, "onClick: "+item.getCurrencyCode());
                onSpinnerItemClickListener.onSpinnerItemClick(item);

            }
        }
    }
}
