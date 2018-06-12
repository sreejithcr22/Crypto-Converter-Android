package com.codit.cryptoconverter.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.orm.AppDatabase;
import com.codit.cryptoconverter.orm.MarketDao;
import com.codit.cryptoconverter.util.Calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class ConvertAndDisplay extends AsyncTask<ConvertAndDisplayParams,Void,String> {
    private  final String TAG = getClass().getSimpleName();
    private Context context;
    private TextView outputField;

    public ConvertAndDisplay(Context context, TextView outputField) {
        this.context = context;
        this.outputField = outputField;
    }
    @Override
    protected String doInBackground(ConvertAndDisplayParams... params) {

        String convertFromCurrency = params[0].getConvertFrom();
        String convertToCurrency = params[0].getConvertTo();
        BigDecimal valToConvert = params[0].getConvertValue();
        Log.d(TAG, "convert: from-"+convertFromCurrency+", To-"+convertToCurrency);
        if(convertFromCurrency!=null && convertFromCurrency!=null) {


            //get currency values from db
            MarketDao marketDao = AppDatabase.getDatabase(context).marketDao();
            if(marketDao!=null) {
                CoinPrices prices = marketDao.getCoinPricesFor(convertFromCurrency);
                if(prices!=null && prices.getPrices()!=null && prices.getPrices().containsKey(convertToCurrency)) { //convert from crypto to fiat
                    Log.d(TAG, "convertFromCurrency: "+prices.getPrices());

                        Double toPrice = prices.getPrices().get(convertToCurrency);
                        Log.d(TAG, "convert: unit toPrice = " + String.valueOf(toPrice));
                        return Calculator.convert(valToConvert, toPrice);

                } else { //convert from fiat to crypto
                    prices = marketDao.getCoinPricesFor(convertToCurrency);

                    if(prices!=null && prices.getPrices()!=null && prices.getPrices().containsKey(convertFromCurrency)) {
                        Log.d(TAG, "convertToCurrency: "+prices.getPrices());
                        Double unitPrice = prices.getPrices().get(convertFromCurrency);
                        return ((BigDecimal.ONE.divide(new BigDecimal(String.valueOf(unitPrice)), 8, RoundingMode.HALF_EVEN)).multiply(valToConvert)).toPlainString();
                    }

                }
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: "+s);
        if(outputField!=null && s!=null) {
            Log.d(TAG, "onPostExecute: converted result = "+s);
            outputField.setText(s);
        }
        else {
            outputField.setText(context.getResources().getString(R.string.err_data_na));
        }
    }
}
