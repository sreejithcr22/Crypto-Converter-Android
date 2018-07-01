package com.codit.cryptoconverter.util;

import android.util.Log;

import com.codit.cryptoconverter.model.CalculatorParams;

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

    enum Operator {OPERATION_ADD, OPERATION_SUBTRACT, OPERATION_MULTIPLY, OPERATION_DIVIDE}

    ;
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

    public static boolean isOperator(char c) {
        String character = String.valueOf(c);
        return character.equals(OPERATION_ADD) || character.equals(OPERATION_SUBTRACT) ||
                character.equals(OPERATION_DIVIDE) || character.equals(OPERATION_MULTIPLY);
    }

    public static CalculatorParams calculateFromString(String valueString) {
        BigDecimal result = null;
        String currentOperator = null;
        StringBuffer value1String = new StringBuffer(), value2String = new StringBuffer();


        try {
            char[] valueStringArray = valueString.toCharArray();
            for (char c : valueStringArray) {

                if (Calculator.isOperator(c)) {
                    currentOperator = String.valueOf(c);
                    value2String = new StringBuffer();
                    if (result != null) value1String = new StringBuffer(result.toPlainString());
                    continue;
                }

                if (currentOperator != null) {
                    value2String.append(c);
                    result = Calculator.calculate(value1String.toString(), value2String.toString(), currentOperator);
                } else {
                    value1String.append(c);
                    result = new BigDecimal(value1String.toString());
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "onBackSpaceClick: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Log.d(TAG, "calculateFromString: val1=" + value1String.toString() + ", val2=" + value2String.toString() + ", op=" + currentOperator + ", result=" + result.toPlainString());
            CalculatorParams calculatorParams = new CalculatorParams(value1String, value2String, result, (currentOperator != null && value2String.toString().isEmpty()), currentOperator);
            return calculatorParams;
        }


    }


}
