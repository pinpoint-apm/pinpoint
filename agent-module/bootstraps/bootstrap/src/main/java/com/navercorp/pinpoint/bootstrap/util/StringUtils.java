package com.navercorp.pinpoint.bootstrap.util;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }
}
