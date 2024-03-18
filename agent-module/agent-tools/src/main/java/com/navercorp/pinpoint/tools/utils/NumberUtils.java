package com.navercorp.pinpoint.tools.utils;

public final class NumberUtils {
    public static int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }
}
