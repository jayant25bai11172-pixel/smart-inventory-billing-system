package com.retail.inventory.gui.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility for formatting amounts with Rupee currency symbol.
 */
public class CurrencyFormatter {
    private static final Locale INDIA_LOCALE = Locale.of("en", "IN");
    private static final NumberFormat FORMATTER = NumberFormat.getCurrencyInstance(INDIA_LOCALE);

    static {
        FORMATTER.setMaximumFractionDigits(2);
        FORMATTER.setMinimumFractionDigits(2);
    }

    public static String format(double amount) {
        return String.format("₹%.2f", amount);
    }

    public static String formatPlain(double amount) {
        return String.format("%.2f", amount);
    }
}
