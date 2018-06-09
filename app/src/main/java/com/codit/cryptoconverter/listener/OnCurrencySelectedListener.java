package com.codit.cryptoconverter.listener;

import android.os.Parcelable;

import com.codit.cryptoconverter.model.SpinnerItem;

/**
 * Created by Sreejith on 18-Mar-18.
 */

public interface OnCurrencySelectedListener {
    void onCurrencySelected(SpinnerItem item, int textViewId);
}
