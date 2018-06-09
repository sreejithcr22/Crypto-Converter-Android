package com.codit.cryptoconverter.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.asynctask.ConvertAndDisplay;
import com.codit.cryptoconverter.asynctask.ConvertAndDisplayParams;
import com.codit.cryptoconverter.model.CoinPrices;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.orm.AppDatabase;
import com.codit.cryptoconverter.orm.MarketDao;
import com.codit.cryptoconverter.util.Calculator;
import com.codit.cryptoconverter.util.Constants;

import java.math.BigDecimal;

public class ConverterFragment extends Fragment {
    private static final String TAG = ConverterFragment.class.getSimpleName();
    private boolean operatorOn = false;
    private TextView edit1, edit2;
    private TextView currency1, currency2;
    private StringBuffer value1, value2 = new StringBuffer();
    private BigDecimal result = null;
    private Button no0, no1, no2, no3, no4, no5, no6, no7, no8, no9,
            opAdd, opSub, opDiv, opEquals, opMultiply, numDot, opClear;
    private View.OnClickListener onCurrencyTextviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SpinnerDialog dialog = new SpinnerDialog();
            Bundle args = new Bundle();
            args.putInt(Constants.EXTRA_CURRENCY_TEXTVIEW_ID, v.getId());
            dialog.setArguments(args);
            dialog.show(getActivity().getFragmentManager(), "");
        }
    };
    private View.OnClickListener onValueFieldClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (edit1 != null && edit2 != null) {
                //rest selection indicator and tags
                printResult();
                operatorOn = false;

                edit1.setBackgroundColor(Color.TRANSPARENT);
                edit2.setBackgroundColor(Color.TRANSPARENT);
                edit1.setTag(R.id.IS_CURRENT_SELECTED_FIELD, false);
                edit2.setTag(R.id.IS_CURRENT_SELECTED_FIELD, false);
            }

            //highlight selection and set selected tag
            v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            v.setTag(R.id.IS_CURRENT_SELECTED_FIELD, true);

            updateValues();
        }
    };
    private View.OnClickListener onCalcDigitButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView currentSelectedField = getCurrentSelectedField();
            TextView outputField = getOutputField();
            String currentOperator;
            String inputValue = ((Button) v).getText().toString();

            if(currentSelectedField==null || outputField==null) {
                Log.d(TAG, "onCalcDigitButtonClickListener: field null, returning");
                return;
            }

            //check if current selected filed is in error state
            if(currentSelectedField.getText().toString().equals(getResources().getString(R.string.err_divide_by_zero))) {
                Log.d(TAG, "onCalcDigitButtonClickListener: current selected field in error state");
                return;
            }

            //check if in error state and if true accept only decimal point input
                if(outputField.getText().toString().equals(getResources().getString(R.string.err_divide_by_zero))
                        && !(value2.toString().contains(String.valueOf(Calculator.INPUT_DECIMAL_POINT))) ) {
                    Log.d(TAG, "onCalcDigitButtonClickListener: error state and no decimal found, returning");
                    return;
                }

                currentOperator = currentSelectedField.getTag(R.id.TAG_CURRENT_OP) == null ? null : String.valueOf(currentSelectedField.getTag(R.id.TAG_CURRENT_OP));

                if (currentSelectedField != null && outputField != null) {

                   if ((inputValue.equals(String.valueOf(Calculator.INPUT_DECIMAL_POINT)))
                           && ((currentOperator==null && currentSelectedField.getText().toString().contains(String.valueOf(Calculator.INPUT_DECIMAL_POINT)))
                           || (currentOperator!=null && value2.toString().contains(String.valueOf(Calculator.INPUT_DECIMAL_POINT)))))
                   {
                       Log.d(TAG, "onClick: . found returning");
                       return;
                   }

                   //if input value is '.' prefix it with '0' and make it '0.'
                    // so that Bigdecimal initialization does not throw exception
                   if(inputValue.equals(String.valueOf(Calculator.INPUT_DECIMAL_POINT))&& (currentSelectedField.getText().toString().isEmpty() || (currentOperator!=null && value2.toString().isEmpty()) )) {
                       inputValue = "0.";
                       Log.d(TAG, "onClick: inputValue = 0.");
                   }

                    currentSelectedField.append(inputValue);
                    operatorOn = false;

                    //calculate result
                    if (currentOperator != null) {
                        value2 = value2.append(inputValue);
                        if(!value2.toString().equals("0.")) { //by pass calculation if value 2 is '0.'
                            try {
                                result = Calculator.calculate(value1.toString(), value2.toString(), currentOperator);
                                updateOutput(outputField, result);
                            } catch (ArithmeticException e) {
                                Log.d(TAG, "caught : "+e.getMessage());
                                if(e.getMessage().equals(Calculator.EXCEPTION_DIVIDE_BY_ZERO)) {
                                    updateOutput(outputField,getResources().getString(R.string.err_divide_by_zero));
                                }
                            }

                        }
                    } else {
                        value1 = new StringBuffer(currentSelectedField.getText().toString());
                        result = new BigDecimal(value1.toString());
                        updateOutput(outputField, result);
                    }
                    Log.d(TAG, "current op= " + currentOperator + " val1= " + String.valueOf(value1) + " , val2= " + String.valueOf(value2) + ", result= " + String.valueOf(result));


                }
            }
        };

    private View.OnClickListener onOperationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView currentSelectedField = getCurrentSelectedField();
            TextView outputField = getOutputField();

            //check if in error state and return
            if(isInErrorState(outputField) || isInErrorState(currentSelectedField)) {
                Log.d(TAG, "onOperationClickListener: error state, returning");
                return;
            }

            //check if current selected file

            String currentOperator = ((Button) v).getText().toString();
            if (currentSelectedField != null && !currentSelectedField.getText().toString().isEmpty()) {
                StringBuffer currentValue = new StringBuffer(currentSelectedField.getText().toString());
                currentSelectedField.setTag(R.id.TAG_CURRENT_OP, currentOperator);

                if (!operatorOn) {

                    if (currentValue.charAt(currentValue.length() - 1) == Calculator.INPUT_DECIMAL_POINT) {
                        currentValue.deleteCharAt(currentValue.length() - 1);
                        currentValue.append(currentOperator);
                        currentSelectedField.setText(currentValue);
                    } else {
                        currentSelectedField.append(currentOperator);
                    }

                } else {

                    currentValue.deleteCharAt(currentValue.length() - 1);
                    currentValue.append(currentOperator);
                    currentSelectedField.setText(currentValue.toString());
                }
                operatorOn = true;
                //reset val2 to empty
                value2 = new StringBuffer();
                if (result != null) {
                    value1 = new StringBuffer(result.toPlainString());
                } else {
                    Log.d(TAG, "onOperationClickListener: result null");
                }


            }
        }
    };

    public ConverterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_converter, container, false);
        edit1 = view.findViewById(R.id.edit1);
        edit2 = view.findViewById(R.id.edit2);
        edit1.setOnClickListener(onValueFieldClickListener);
        edit2.setOnClickListener(onValueFieldClickListener);

        edit1.setTag(R.id.IS_CURRENT_SELECTED_FIELD,false);
        edit2.setTag(R.id.IS_CURRENT_SELECTED_FIELD,false);

        edit1.callOnClick();

        currency1 = view.findViewById(R.id.currency_label1);
        currency2 = view.findViewById(R.id.currency_label2);
        currency1.setText(Constants.CURRENCY1_DEFAULT_VALUE);
        currency2.setText(Constants.CURRENCY2_DEFAULT_VALUE);
        currency1.setOnClickListener(onCurrencyTextviewClickListener);
        currency2.setOnClickListener(onCurrencyTextviewClickListener);

        no0 = (Button) view.findViewById(R.id.num0);
        no1 = (Button) view.findViewById(R.id.num1);
        no2 = (Button) view.findViewById(R.id.num2);
        no3 = (Button) view.findViewById(R.id.num3);
        no4 = (Button) view.findViewById(R.id.num4);
        no5 = (Button) view.findViewById(R.id.num5);
        no6 = (Button) view.findViewById(R.id.num6);
        no7 = (Button) view.findViewById(R.id.num7);
        no8 = (Button) view.findViewById(R.id.num8);
        no9 = (Button) view.findViewById(R.id.num9);
        numDot = (Button) view.findViewById(R.id.numDot);

        opAdd = view.findViewById(R.id.opAdd);
        opMultiply = view.findViewById(R.id.opMultiply);
        opSub = view.findViewById(R.id.opSub);
        opDiv = view.findViewById(R.id.opDiv);

        no0.setOnClickListener(onCalcDigitButtonClickListener);
        no1.setOnClickListener(onCalcDigitButtonClickListener);
        no2.setOnClickListener(onCalcDigitButtonClickListener);
        no3.setOnClickListener(onCalcDigitButtonClickListener);
        no4.setOnClickListener(onCalcDigitButtonClickListener);
        no5.setOnClickListener(onCalcDigitButtonClickListener);
        no6.setOnClickListener(onCalcDigitButtonClickListener);
        no7.setOnClickListener(onCalcDigitButtonClickListener);
        no8.setOnClickListener(onCalcDigitButtonClickListener);
        no9.setOnClickListener(onCalcDigitButtonClickListener);
        numDot.setOnClickListener(onCalcDigitButtonClickListener);

        opAdd.setOnClickListener(onOperationClickListener);
        opSub.setOnClickListener(onOperationClickListener);
        opMultiply.setOnClickListener(onOperationClickListener);
        opDiv.setOnClickListener(onOperationClickListener);


        return view;
    }


    private TextView getCurrentSelectedField() {
        if (edit1 != null && edit2 != null) {

            if ((boolean) edit1.getTag(R.id.IS_CURRENT_SELECTED_FIELD)) {
                return edit1;
            } else if ((boolean) edit2.getTag(R.id.IS_CURRENT_SELECTED_FIELD)) {
                return edit2;
            }
        }
        return null;
    }

    private TextView getOutputField() {

        if (edit1 != null && edit2 != null) {
            if ((boolean) edit1.getTag(R.id.IS_CURRENT_SELECTED_FIELD)) {
                return edit2;
            } else if ((boolean) edit2.getTag(R.id.IS_CURRENT_SELECTED_FIELD)) {
                return edit1;
            }
        }
        return null;
    }


    public void updateCurrencySelection(SpinnerItem item, int textViewId) {
        Log.d(TAG, "updateCurrencySelection: " + item.getCurrencyName());

        if (textViewId == -1) {
            Toast.makeText(getActivity(), getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (textViewId == currency1.getId()) {
            if (currency2.getText().toString().equals(item.getCurrencyCode())) {
                Toast.makeText(getActivity(), getString(R.string.same_currency_value), Toast.LENGTH_SHORT).show();
            } else {
                currency1.setText(item.getCurrencyCode());
            }
        } else {
            if (currency1.getText().toString().equals(item.getCurrencyCode())) {
                Toast.makeText(getActivity(), getString(R.string.same_currency_value), Toast.LENGTH_SHORT).show();
            } else {
                currency2.setText(item.getCurrencyCode());
            }
        }
    }

    private String getConvertFromCurrency() {

        TextView currentSelectedFiled = getCurrentSelectedField();
        if(currentSelectedFiled!=null) {
            if(currentSelectedFiled.getId() == R.id.edit1) {
                return currency1.getText().toString();
            }
            else if(currentSelectedFiled.getId() == R.id.edit2) {
                return currency2.getText().toString();
            }
        }
        return null;
    }

    private String getConvertToCurrency() {

        TextView outputField = getOutputField();
        if(outputField!=null) {
            if (outputField.getId() == R.id.edit1) {
                return currency1.getText().toString();
            }
            else if(outputField.getId() == R.id.edit2) {
                return currency2.getText().toString();
            }
        }
        return null;
    }

    private void updateOutput(TextView outputField, BigDecimal convertFromValue) {
        if (outputField != null && result != null) {

            //convert
            /*BigDecimal result = convert(getConvertFromCurrency(), getConvertToCurrency(), convertFromValue);*/
            //outputField.setText(convertFromValue.toPlainString());
            ConvertAndDisplay task = new ConvertAndDisplay(getContext().getApplicationContext(),outputField);
            task.execute(new ConvertAndDisplayParams(getConvertFromCurrency(),getConvertToCurrency(),convertFromValue));

            }
    }

    private void updateOutput(TextView outputField, String errorMessage) {
        if (outputField!=null) {
            Log.d(TAG, "updateOutput: "+errorMessage);
            outputField.setText(errorMessage);
        }
    }

    private BigDecimal convert(String convertFromCurrency, String convertToCurrency, BigDecimal value) {
        if(convertFromCurrency!=null && convertFromCurrency!=null) {
            Log.d(TAG, "convert: from-"+convertFromCurrency+", To-"+convertToCurrency);

            //get currency values from db
            MarketDao marketDao = AppDatabase.getDatabase(getContext().getApplicationContext()).marketDao();
            if(marketDao!=null) {
                CoinPrices prices = marketDao.getCoinPricesFor(convertFromCurrency);
                Double toPrice = prices.getPrices().get(convertToCurrency);
                Log.d(TAG, "convert: toPrice = "+String.valueOf(toPrice));
            }

        }

        Log.d(TAG, "convert: error--from-"+convertFromCurrency+", To-"+convertToCurrency);
        return null;
    }

    private void printResult() {
        TextView currentSelectedField = getCurrentSelectedField();
        if(currentSelectedField != null && result!=null) {
            currentSelectedField.setText(result.toPlainString());
        }
    }

    private void updateValues() {
        TextView currentSelectedField = getCurrentSelectedField();
        if (!isInErrorState(currentSelectedField) && !currentSelectedField.getText().toString().isEmpty()) {
            try {
                value1 = new StringBuffer(currentSelectedField.getText().toString());
                result = new BigDecimal(currentSelectedField.getText().toString());
                value2 = new StringBuffer();
                currentSelectedField.setTag(R.id.TAG_CURRENT_OP,null);

            } catch (Exception e) {
                Log.d(TAG, "updateValues: exception--"+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean isInErrorState(TextView field) {
        if(field!=null)
        return field.getText().toString().equals(getResources().getString(R.string.err_divide_by_zero));
        else return true;

    }

}

