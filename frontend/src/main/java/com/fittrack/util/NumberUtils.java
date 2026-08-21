package com.fittrack.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class NumberUtils {

    private static final Locale APP_LOCALE = Locale.getDefault();

    private NumberUtils() {}

    public static double parseDecimal(String text) {
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    public static String format(String pattern, Object... args) {
        return String.format(APP_LOCALE, pattern, args);
    }

    public static String formatRange(double from, double to) {
        return format("%.1f", from) + " - " + format("%.1f", to);
    }

    public static String formatWhole(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(APP_LOCALE);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(value);
    }

    public static String formatDecimal(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }

        return String.valueOf(value);
    }
}