package com.codit.cryptoconverter.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.codit.cryptoconverter.listener.FavPairDBOperationsListener;
import com.codit.cryptoconverter.model.FavouritePair;

import java.util.List;

public class FavPairsDB {
    private static final String TAG = FavPairsDB.class.getSimpleName();
    private static final String OPERATION_ADD = "add_fav_pair";
    private static final String OPERATION_DELETE = "delete_fav_pair";
    private static final String OPERATION_LIST_ALL = "list_all_fav_pair";
    private static final String OPERATION_IS_PAIR_EXIST = "is_pair_exist";

    private static FavPairsDB instance = null;
    private Context context;

    private FavPairsDB(Context context) {
        this.context = context;
    }

    public static FavPairsDB getInstance(Context context) {
        if (instance == null) instance = new FavPairsDB(context);
        return instance;
    }

    public void addFavPair(FavouritePair pair, FavPairDBOperationsListener listener) {
        new DBOperationsThread(OPERATION_ADD, pair, listener).start();
    }

    public void getAllFavPairs(FavPairDBOperationsListener listener) {
        new DBOperationsThread(OPERATION_LIST_ALL, listener).start();
    }

    public void deleteFavPair(FavouritePair pair, FavPairDBOperationsListener listener) {
        new DBOperationsThread(OPERATION_DELETE, pair, listener).start();
    }

    public void isFavPairExist(FavouritePair pair, FavPairDBOperationsListener listener) {
        new DBOperationsThread(OPERATION_IS_PAIR_EXIST, pair, listener).start();
    }

    private class DBOperationsThread extends Thread {
        private FavPairDBOperationsListener listener;
        private FavouritePair favouritePair;
        private String operation;
        private Handler handler = new Handler(Looper.getMainLooper());
        private FavPairDao favPairDao;

        public DBOperationsThread(String operation, FavouritePair favouritePair, FavPairDBOperationsListener listener) {
            this.favouritePair = favouritePair;
            this.listener = listener;
            this.operation = operation;
            initDB();
        }

        public DBOperationsThread(String operation, FavPairDBOperationsListener listener) {
            this.listener = listener;
            this.operation = operation;
            initDB();
        }

        private void initDB() {
            try {
                favPairDao = AppDatabase.getDatabase(context).favPairDao();
            } catch (Exception e) {
                listener.onDbOpenFailed();
            }
        }

        private void addFavPair(FavouritePair favouritePair) {
            try {

                long id = favPairDao.addFavPair(favouritePair);
                Log.d(TAG, "addtoFavPair: id=" + String.valueOf(id));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFavPairAdded();
                    }
                });
            } catch (android.database.sqlite.SQLiteConstraintException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFavPairAddFailed("already exists");
                    }
                });
            }
        }

        private void listAllFavPairs() {

            final List<FavouritePair> pairs = favPairDao.getAllFavPairs();
            if (pairs != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFavPairsFetched(pairs);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGenericError("An error occurred!");
                    }
                });
            }

        }

        private void deleteFavPair(FavouritePair pair) {
            FavouritePair reversePair = new FavouritePair(pair.getConvertToCurrency(), pair.getConvertFromCurrency());
            if (favPairDao.deleteFavPair(pair) > 0 || favPairDao.deleteFavPair(reversePair) > 0) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFavPairDeleted();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFavPairDeleteFailed("Favourite pair could not be deleted!");
                    }
                });
            }
        }

        private void isPairExist(FavouritePair pair) {
            boolean isPairExist = false;
            try {
                Log.d(TAG, "isPairExist: check-" + pair.getConvertFromCurrency() + "-" + pair.getConvertToCurrency());
                List<FavouritePair> list = favPairDao.isPairExist(pair.getConvertFromCurrency(), pair.getConvertToCurrency());
                int count = list.size();
                Log.d(TAG, "isPairExist: " + count);
                if (count > 0) {
                    isPairExist = true;
                    Log.d(TAG, "isPairExist: pair=" + list.get(0).getConvertFromCurrency() + "-" + list.get(0).getConvertToCurrency());
                }
            } catch (Exception e) {
                isPairExist = false;
            }
            final boolean finalIsPairExist = isPairExist;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onIsPairExistResult(finalIsPairExist);
                }
            });
        }

        @Override
        public void run() {

            try {
                switch (operation) {
                    case OPERATION_ADD:
                        addFavPair(favouritePair);
                        break;
                    case OPERATION_DELETE:
                        deleteFavPair(favouritePair);
                        break;
                    case OPERATION_LIST_ALL:
                        listAllFavPairs();
                        break;
                    case OPERATION_IS_PAIR_EXIST:
                        isPairExist(favouritePair);
                        break;
                }
            } catch (final Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        listener.onGenericError("An error occurred!");
                    }
                });
            }
        }
    }
}
