package com.navercorp.pinpoint.test.plugin.util;

import java.util.List;

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

    public static String join(List<String> stringList, char separator) {

        StringBuilder classPath = new StringBuilder();
        boolean first = true;

        for (String lib : stringList) {
            if (first) {
                first = false;
            } else {
                classPath.append(separator);
            }

            classPath.append(lib);
        }
        return classPath.toString();
    }
}
