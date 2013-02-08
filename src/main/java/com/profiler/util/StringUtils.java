package com.profiler.util;

public class StringUtils {

    public static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static String toString(final Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

    public static String drop(final String str) {
        return drop(str, 30);
    }

    public static String getDropString(final String str) {
        return "...(" + str.length() + ")";
    }

    public static String drop(final String str, final int length) {
        if (str == null) {
            return "null";
        }

        if (str.length() > length) {
            StringBuilder buffer = new StringBuilder(length + 10);
            buffer.append(str.substring(0, length));
            buffer.append(getDropString(str));
            return buffer.toString();
        } else {
            return str;
        }
    }
}
