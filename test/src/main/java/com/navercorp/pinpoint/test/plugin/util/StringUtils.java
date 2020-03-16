package com.navercorp.pinpoint.test.plugin.util;

import java.util.List;
import java.util.Map;

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

    public static String join(List<String> stringList, String separator) {

        final StringBuilder buffer = new StringBuilder();
        boolean first = true;

        for (String lib : stringList) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }

            buffer.append(lib);
        }
        return buffer.toString();
    }

    public static String join(Map<String, String> map, String kvDelimiter, String separator) {
        final StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                buffer.append(separator);
            }
            buffer.append(entry.getKey());
            buffer.append(kvDelimiter);
            buffer.append(entry.getValue());
        }
        return buffer.toString();
    }
}
