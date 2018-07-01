package com.codit.cryptoconverter.model;

import java.math.BigDecimal;

public class CalculatorParams {

    public CalculatorParams(StringBuffer val1, StringBuffer val2, BigDecimal result, boolean operatorOn, String operator) {
        this.val1 = val1;
        this.val2 = val2;
        this.result = result;
        this.operatorOn = operatorOn;
        this.operator = operator;
    }

    private StringBuffer val1;
    private StringBuffer val2;
    private BigDecimal result;

    public StringBuffer getVal1() {
        return val1;
    }

    public StringBuffer getVal2() {
        return val2;
    }

    public BigDecimal getResult() {
        return result;
    }

    public boolean isOperatorOn() {
        return operatorOn;
    }

    public String getOperator() {
        return operator;
    }

    private boolean operatorOn;
    private String operator;
}
