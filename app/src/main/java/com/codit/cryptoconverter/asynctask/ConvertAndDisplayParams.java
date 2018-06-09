package com.codit.cryptoconverter.asynctask;

import java.math.BigDecimal;

public class ConvertAndDisplayParams {
    private String convertFrom,convertTo;

    public String getConvertFrom() {
        return convertFrom;
    }

    public void setConvertFrom(String convertFrom) {
        this.convertFrom = convertFrom;
    }

    public String getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(String convertTo) {
        this.convertTo = convertTo;
    }

    public BigDecimal getConvertValue() {
        return convertValue;
    }

    public void setConvertValue(BigDecimal convertValue) {
        this.convertValue = convertValue;
    }

    private BigDecimal convertValue;

    public ConvertAndDisplayParams(String convertFrom, String convertTo, BigDecimal convertValue) {
        this.convertFrom = convertFrom;
        this.convertTo = convertTo;
        this.convertValue = convertValue;
    }
}
