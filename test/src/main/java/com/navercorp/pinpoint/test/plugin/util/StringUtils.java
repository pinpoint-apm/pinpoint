package com.navercorp.pinpoint.test.plugin.util;

import java.util.Collection;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean hasLength(final String string) {
        return string != null && string.length() > 0;
    }

    public static boolean hasText(String string) {
        if (isEmpty(string)) {
            return false;
        }

        final int length = string.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static <T> int getLength(final String string, final int nullValue) {
        if (string == null) {
            return nullValue;
        }
        return string.length();
    }

    public static String join(Collection<String> stringList, String separator) {
        StringJoiner joiner = new StringJoiner(separator);
        for (String str : stringList) {
            joiner.add(str);
        }
        return joiner.toString();
    }
}
