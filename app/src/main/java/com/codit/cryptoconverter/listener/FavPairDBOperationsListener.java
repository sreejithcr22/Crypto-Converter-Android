package com.codit.cryptoconverter.listener;

import com.codit.cryptoconverter.model.FavouritePair;

import java.util.List;

public interface FavPairDBOperationsListener {
    void onDbOpenFailed();

    void onGenericError(String errorMessage);

    void onFavPairAdded();

    void onFavPairAddFailed(String errorMessage);

    void onFavPairsFetched(List<FavouritePair> favouritePairList);

    void onFavPairDeleted();

    void onFavPairDeleteFailed(String message);

    void onIsPairExistResult(boolean isExist);
}
