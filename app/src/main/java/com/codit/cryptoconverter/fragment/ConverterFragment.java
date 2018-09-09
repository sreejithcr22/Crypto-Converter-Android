package com.codit.cryptoconverter.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codit.cryptoconverter.R;
import com.codit.cryptoconverter.asynctask.ConvertAndDisplay;
import com.codit.cryptoconverter.asynctask.ConvertAndDisplayParams;
import com.codit.cryptoconverter.db.FavPairsDB;
import com.codit.cryptoconverter.dialog.FavPairsListDialog;
import com.codit.cryptoconverter.dialog.SpinnerDialog;
import com.codit.cryptoconverter.listener.FavDialogOperationsListener;
import com.codit.cryptoconverter.listener.FavPairDBOperationsListener;
import com.codit.cryptoconverter.model.CalculatorParams;
import com.codit.cryptoconverter.model.FavouritePair;
import com.codit.cryptoconverter.model.SpinnerItem;
import com.codit.cryptoconverter.util.Calculator;
import com.codit.cryptoconverter.util.Constants;
import com.codit.cryptoconverter.util.Util;

import java.math.BigDecimal;
import java.util.List;

public class ConverterFragment extends Fragment {
    private static final String TAG = ConverterFragment.class.getSimpleName();
    private boolean operatorOn = false;
    private LinearLayout converterLayoutHolder1, converterLayoutHolder2;
    private TextView edit1, edit2;
    private TextView currency1, currency2;
    private StringBuffer value1, value2 = new StringBuffer();
    private BigDecimal result = null;
    private Button no0, no1, no2, no3, no4, no5, no6, no7, no8, no9,
            opAdd, opSub, opDiv, opEquals, opMultiply, numDot, opClear;
    private ImageButton opBackSpace, btnAddFav, btnShowFavList, btnSwitchCurrencies;
    private FavDialogOperationsListener favDialogOperationsListener = new FavDialogOperationsListener() {
        @Override
        public void onFavPairSelected(FavouritePair pair) {
            currency1.setText(Util.getCurrencyName(pair.getConvertFromCurrency()) + " (" + pair.getConvertFromCurrency() + ")");
            currency2.setText(Util.getCurrencyName(pair.getConvertToCurrency()) + " (" + pair.getConvertToCurrency() + ")");
            clearFields();
        }

        @Override
        public void onFavDialogDismissed() {
            FavPairsDB.getInstance(getActivity()).isFavPairExist(new FavouritePair(getConvertFromCurrency(), getConvertToCurrency()), listener);

        }
    };
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

            if (getCurrentSelectedField() != null && getCurrentSelectedField().getId() == v.getId())
                return;

            if (edit1 != null && edit2 != null) {
                //rest selection indicator and tags
                printResult();


                //edit1.setBackground(getResources().getDrawable(R.drawable.currency_label_normal_bg));
                //edit2.setBackground(getResources().getDrawable(R.drawable.currency_label_normal_bg));
                converterLayoutHolder1.setBackground(getResources().getDrawable(R.drawable.converter_holder_normal_bg));
                converterLayoutHolder2.setBackground(getResources().getDrawable(R.drawable.converter_holder_normal_bg));
                edit1.setTag(R.id.IS_CURRENT_SELECTED_FIELD, false);
                edit2.setTag(R.id.IS_CURRENT_SELECTED_FIELD, false);
            }

            //highlight selection and set selected tag
            //v.setBackground(getResources().getDrawable(R.drawable.converter_layout_holder_selected_bg));
            if (v.getId() == R.id.edit1) {
                converterLayoutHolder1.setBackground(getResources().getDrawable(R.drawable.converter_layout_holder_selected_bg));
            } else {
                converterLayoutHolder2.setBackground(getResources().getDrawable(R.drawable.converter_layout_holder_selected_bg));

            }
            v.setTag(R.id.IS_CURRENT_SELECTED_FIELD, true);

            updateCalcParams();
        }
    };

    private View.OnClickListener onClearButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clearFields();
        }
    };

    private View.OnClickListener onCalcDigitButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView currentSelectedField = getCurrentSelectedField();
            TextView outputField = getOutputField();
            String currentOperator;
            String inputValue = ((Button) v).getText().toString();

            if (currentSelectedField == null || outputField == null) {
                Log.d(TAG, "onCalcDigitButtonClickListener: field null, returning");
                return;
            }

            //check if current selected filed is in error state
            if (isInErrorState(currentSelectedField)) {
                Log.d(TAG, "onCalcDigitButtonClickListener: current selected field in error state");
                return;
            }

            //check if in error state and if true accept only decimal point input
            if (isInErrorState(outputField)
                    && !(value2.toString().contains(String.valueOf(Calculator.INPUT_DECIMAL_POINT)))) {
                Log.d(TAG, "onCalcDigitButtonClickListener: error state and no decimal found, returning");
                return;
            }

            currentOperator = currentSelectedField.getTag(R.id.TAG_CURRENT_OP) == null ? null : String.valueOf(currentSelectedField.getTag(R.id.TAG_CURRENT_OP));

            if (currentSelectedField != null && outputField != null) {

                if ((inputValue.equals(String.valueOf(Calculator.INPUT_DECIMAL_POINT)))
                        && ((currentOperator == null && currentSelectedField.getText().toString().contains(String.valueOf(Calculator.INPUT_DECIMAL_POINT)))
                        || (currentOperator != null && value2.toString().contains(String.valueOf(Calculator.INPUT_DECIMAL_POINT))))) {
                    Log.d(TAG, "onClick: . found returning");
                    return;
                }

                //if input value is '.' prefix it with '0' and make it '0.'
                // so that Bigdecimal initialization does not throw exception
                if (inputValue.equals(String.valueOf(Calculator.INPUT_DECIMAL_POINT)) && (currentSelectedField.getText().toString().isEmpty() || (currentOperator != null && value2.toString().isEmpty()))) {
                    inputValue = "0.";
                    Log.d(TAG, "onClick: inputValue = 0.");
                }

                currentSelectedField.append(inputValue);
                operatorOn = false;

                //calculate result
                if (currentOperator != null) {
                    value2 = value2.append(inputValue);
                    if (!value2.toString().equals("0.")) { //by pass calculation if value 2 is '0.'
                        try {
                            result = Calculator.calculate(value1.toString(), value2.toString(), currentOperator);
                            updateOutput(outputField, result);
                        } catch (ArithmeticException e) {
                            Log.d(TAG, "caught : " + e.getMessage());
                            if (e.getMessage().equals(Calculator.EXCEPTION_DIVIDE_BY_ZERO)) {
                                updateOutput(outputField, getResources().getString(R.string.err_divide_by_zero));
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
            if (isInErrorState(outputField) || isInErrorState(currentSelectedField)) {
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

    private View.OnClickListener onShowFavBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showFavListDialog();
        }
    };

    private View.OnClickListener onBackspaceListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onBackSpaceClick();
        }
    };

    private View.OnClickListener onOpEqualsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            printResult();
            updateCalcParams();
        }
    };

    private View.OnClickListener onAddFavBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String convertFromCurrency = getConvertFromCurrency();
            String convertToCurrency = getConvertToCurrency();

            if (convertFromCurrency != null && convertToCurrency != null) {
                addFavPair(convertFromCurrency, convertToCurrency);

            }
        }
    };

    private View.OnClickListener onSwitchCurrenciesBtnCliclListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            v.animate().rotationX(v.getRotationX() + 180).setDuration(300).start();
            TextView currentSelectedCurrencyLabel, outputCurrencyLabel;
            String currentSelectedCurrency, outputCurrency;
            TextView currentSelectedField = getCurrentSelectedField();
            if (currentSelectedField != null) {
                if (currentSelectedField.getId() == R.id.edit1) {
                    currentSelectedCurrencyLabel = currency1;
                    outputCurrencyLabel = currency2;
                } else {
                    currentSelectedCurrencyLabel = currency2;
                    outputCurrencyLabel = currency1;
                }

                currentSelectedCurrency = currentSelectedCurrencyLabel.getText().toString();
                outputCurrency = outputCurrencyLabel.getText().toString();
                currentSelectedCurrencyLabel.setText(outputCurrency);
                outputCurrencyLabel.setText(currentSelectedCurrency);

                //update output
                FavPairsDB.getInstance(getActivity()).isFavPairExist(new FavouritePair(getConvertFromCurrency(), getConvertToCurrency()), listener);
                printResult();
                updateCalcParams();
                updateOutput(getOutputField(), result);

            }
        }
    };

    private void showFavListDialog() {
        FavPairsListDialog favPairsListDialog = FavPairsListDialog.newInstance(favDialogOperationsListener);
        favPairsListDialog.show(getFragmentManager(), null);
    }

    public ConverterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_converter, container, false);
        converterLayoutHolder1 = view.findViewById(R.id.converter_layout_holder_1);
        converterLayoutHolder2 = view.findViewById(R.id.converter_layout_holder_2);
        edit1 = view.findViewById(R.id.edit1);
        edit2 = view.findViewById(R.id.edit2);
        edit1.setOnClickListener(onValueFieldClickListener);
        edit2.setOnClickListener(onValueFieldClickListener);

        edit1.setTag(R.id.IS_CURRENT_SELECTED_FIELD, false);
        edit2.setTag(R.id.IS_CURRENT_SELECTED_FIELD, false);

        edit1.callOnClick();

        currency1 = view.findViewById(R.id.currency_label1);
        currency2 = view.findViewById(R.id.currency_label2);
        currency1.setText(Util.getCurrencyName(Constants.CURRENCY1_DEFAULT_VALUE) + " (" + Constants.CURRENCY1_DEFAULT_VALUE + ")");
        currency2.setText(Util.getCurrencyName(Constants.CURRENCY2_DEFAULT_VALUE) + " (" + Constants.CURRENCY2_DEFAULT_VALUE + ")");
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
        opBackSpace = view.findViewById(R.id.backspace);
        opClear = view.findViewById(R.id.clear);
        opEquals = view.findViewById(R.id.opEquals);
        btnAddFav = view.findViewById(R.id.btn_add_fav);
        btnShowFavList = view.findViewById(R.id.view_favs);
        btnSwitchCurrencies = view.findViewById(R.id.btn_switch);

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
        opBackSpace.setOnClickListener(onBackspaceListener);
        opClear.setOnClickListener(onClearButtonClickListener);
        opEquals.setOnClickListener(onOpEqualsClickListener);
        btnAddFav.setOnClickListener(onAddFavBtnClickListener);
        btnShowFavList.setOnClickListener(onShowFavBtnClickListener);
        btnSwitchCurrencies.setOnClickListener(onSwitchCurrenciesBtnCliclListener);

        //check if default currencies are added to favs
        FavPairsDB.getInstance(getActivity()).isFavPairExist(new FavouritePair(getConvertFromCurrency(), getConvertToCurrency()), listener);

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
        boolean isCurrencyUpdated = false;
        if (textViewId == -1) {
            Toast.makeText(getActivity(), getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
            return;
        }
        if (textViewId == currency1.getId()) {
            Log.d(TAG, "extractCurrencyCode: returned: " + extractCurrencyCode(currency2.getText().toString()));
            if (extractCurrencyCode(currency2.getText().toString()).equals(item.getCurrencyCode())) {
                Toast.makeText(getActivity(), getString(R.string.same_currency_value), Toast.LENGTH_SHORT).show();
            } else {
                currency1.setText(Util.getCurrencyName(item.getCurrencyCode()) + " (" + item.getCurrencyCode() + ")");
                isCurrencyUpdated = true;
            }
        } else {
            Log.d(TAG, "extractCurrencyCode: returned: " + extractCurrencyCode(currency1.getText().toString()));
            if (extractCurrencyCode(currency1.getText().toString()).equals(item.getCurrencyCode())) {
                Toast.makeText(getActivity(), getString(R.string.same_currency_value), Toast.LENGTH_SHORT).show();
            } else {
                currency2.setText(Util.getCurrencyName(item.getCurrencyCode()) + " (" + item.getCurrencyCode() + ")");
                isCurrencyUpdated = true;
            }
        }

        //currency changed, re-convert and display
        if (isCurrencyUpdated) {
            FavPairsDB.getInstance(getActivity()).isFavPairExist(new FavouritePair(getConvertFromCurrency(), getConvertToCurrency()), listener);
            printResult();
            updateCalcParams();
            updateOutput(getOutputField(), result);
        }

    }

    private String getConvertFromCurrency() {

        TextView currentSelectedFiled = getCurrentSelectedField();
        if (currentSelectedFiled != null) {
            if (currentSelectedFiled.getId() == R.id.edit1) {
                return extractCurrencyCode(currency1.getText().toString());
            } else if (currentSelectedFiled.getId() == R.id.edit2) {
                return extractCurrencyCode(currency2.getText().toString());
            }
        }
        return null;
    }

    private String getConvertToCurrency() {

        TextView outputField = getOutputField();
        if (outputField != null) {
            if (outputField.getId() == R.id.edit1) {
                return extractCurrencyCode(currency1.getText().toString());
            } else if (outputField.getId() == R.id.edit2) {
                return extractCurrencyCode(currency2.getText().toString());
            }
        }
        return null;
    }

    private void updateOutput(TextView outputField, BigDecimal convertFromValue) {
        if (outputField != null && result != null) {

            //convert
            /*BigDecimal result = convert(getConvertFromCurrency(), getConvertToCurrency(), convertFromValue);*/
            //outputField.setText(convertFromValue.toPlainString());
            ConvertAndDisplay task = new ConvertAndDisplay(getContext().getApplicationContext(), outputField);
            task.execute(new ConvertAndDisplayParams(getConvertFromCurrency(), getConvertToCurrency(), convertFromValue));

        }
    }

    private void updateOutput(TextView outputField, String errorMessage) {
        if (outputField != null) {
            Log.d(TAG, "updateOutput: " + errorMessage);
            outputField.setText(errorMessage);
        }
    }

    private void printResult() {
        TextView currentSelectedField = getCurrentSelectedField();
        if (!isInErrorState(currentSelectedField) && result != null) {
            currentSelectedField.setText(result.toPlainString());
        }
    }

    private void updateCalcParams() {
        operatorOn = false;
        TextView currentSelectedField = getCurrentSelectedField();

        try {
            if (isInErrorState(currentSelectedField)) return;

            currentSelectedField.setTag(R.id.TAG_CURRENT_OP, null);
            if (!currentSelectedField.getText().toString().isEmpty()) {

                value1 = new StringBuffer(currentSelectedField.getText().toString());
                result = new BigDecimal(currentSelectedField.getText().toString());
                value2 = new StringBuffer();

            } else {
                value1 = new StringBuffer();
                result = null;
                value2 = new StringBuffer();

            }


        } catch (Exception e) {
            Log.d(TAG, "updateCalcParams: exception--" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCalcParams(CalculatorParams params) {
        TextView currentSelectedField = getCurrentSelectedField();
        if (currentSelectedField == null) return;
        operatorOn = params.isOperatorOn();
        currentSelectedField.setTag(R.id.TAG_CURRENT_OP, params.getOperator());
        value1 = params.getVal1();
        value2 = params.getVal2();
        result = params.getResult();


    }

    private void clearFields() {
        if (edit1 != null) edit1.setText("");
        if (edit2 != null) edit2.setText("");
        updateCalcParams(new CalculatorParams(new StringBuffer(), new StringBuffer(), null, false, null));

    }

    private boolean isInErrorState(TextView field) {
        if (field != null)
            return field.getText().toString().equals(getResources().getString(R.string.err_divide_by_zero)) || field.getText().toString().equals(getResources().getString(R.string.err_data_na));
        else return true;

    }

    private void onBackSpaceClick() {
        TextView currentSelectedFiled = getCurrentSelectedField();
        TextView outputField = getOutputField();
        if (currentSelectedFiled == null || currentSelectedFiled.getText().toString().isEmpty() || isInErrorState(currentSelectedFiled)) {
            return;
        }
        String valueString = currentSelectedFiled.getText().toString();
        valueString = valueString.substring(0, valueString.length() - 1);
        if (valueString.isEmpty()) {
            currentSelectedFiled.setText("");
            if (outputField != null) outputField.setText("");
            result = null;
            return;
        }
        currentSelectedFiled.setText(valueString);

        CalculatorParams params = Calculator.calculateFromString(valueString);
        updateCalcParams(params);
        BigDecimal paramsResult = params.getResult();

        if (paramsResult != null) {
            Log.d(TAG, "onBackSpaceClick: result = " + paramsResult.toPlainString());
            ConvertAndDisplay convertAndDisplay = new ConvertAndDisplay(this.getContext(), getOutputField());
            convertAndDisplay.execute(new ConvertAndDisplayParams(getConvertFromCurrency(), getConvertToCurrency(), paramsResult));
        } else Log.d(TAG, "onBackSpaceClick: result null");
    }

    private FavPairDBOperationsListener listener = new FavPairDBOperationsListener() {
        @Override
        public void onDbOpenFailed() {

        }

        @Override
        public void onGenericError(String errorMessage) {

        }

        @Override
        public void onFavPairAdded() {
            Toast.makeText(getContext(), "Added to favourites !", Toast.LENGTH_SHORT).show();
            btnAddFav.setColorFilter(Color.RED);
        }

        @Override
        public void onFavPairAddFailed(String errorMessage) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFavPairsFetched(List<FavouritePair> favouritePairList) {

        }

        @Override
        public void onFavPairDeleted() {

        }

        @Override
        public void onFavPairDeleteFailed(String message) {

        }

        @Override
        public void onIsPairExistResult(boolean isExist) {
            Log.d(TAG, "onIsPairExistResult: " + String.valueOf(isExist));
            if (isExist) {
                btnAddFav.setColorFilter(Color.RED);
            } else {
                btnAddFav.setColorFilter(Color.BLACK);
            }
        }
    };

    private void addFavPair(String convertFromCurrency, String convertToCurrency) {

        FavPairsDB.getInstance(getActivity()).addFavPair(new FavouritePair(convertFromCurrency, convertToCurrency), listener);
    }

    private String extractCurrencyCode(String textViewString) {
        String arr[] = textViewString.split("\\(");
        return arr[1].substring(0, arr[1].length() - 1);
    }

}

