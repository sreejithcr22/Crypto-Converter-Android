package com.codit.cryptoconverter.listener;

import java.math.BigDecimal;

public interface ConverterFragmentCallback {
    public void pasteClipboard(BigDecimal value);
}
