package com.codit.cryptoconverter.listener;

import com.codit.cryptoconverter.model.FavouritePair;

public interface FavDialogOperationsListener {
    void onFavPairSelected(FavouritePair pair);

    void onFavDialogDismissed();
}
