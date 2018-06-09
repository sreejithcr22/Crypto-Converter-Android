package com.codit.cryptoconverter.util;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Sreejith on 20-Mar-18.
 */

public class Calculator {
    public static final char INPUT_DECIMAL_POINT = '.';
    public static final String INPUT_CLEAR = "C";
    public static final String OPERATION_ADD = "+";
    public static final String OPERATION_SUBTRACT = "-";
    public static final String OPERATION_MULTIPLY = "*";
    private static final String OPERATION_DIVIDE = "÷";
    public static final String EXCEPTION_DIVIDE_BY_ZERO = "divide_by_zero_exception";
    private static final String TAG = Calculator.class.getSimpleName();

    public static BigDecimal calculate(String value1, String value2, String operator) throws ArithmeticException
    {
        BigDecimal val1 = new BigDecimal(value1), val2 = new BigDecimal(value2);

        switch (operator) {
            case Calculator.OPERATION_ADD: return val1.add(val2);
            case Calculator.OPERATION_SUBTRACT: return val1.subtract(val2);
            case Calculator.OPERATION_MULTIPLY: return val1.multiply(val2);
            case Calculator.OPERATION_DIVIDE:
                if(val2.compareTo(BigDecimal.ZERO) == 0) {
                    Log.d(TAG, "calculate: ");
                    throw new ArithmeticException(Calculator.EXCEPTION_DIVIDE_BY_ZERO);
                }
                else {
                    return val1.divide(val2, 8, RoundingMode.HALF_EVEN);
                }

            default: return null;
        }

    }

    public static String convert(BigDecimal valToConvert, Double unitPrice) {
        BigDecimal unitPriceVal = new BigDecimal(String.valueOf(unitPrice));
        return (valToConvert.multiply(unitPriceVal)).toPlainString();
    }
}
