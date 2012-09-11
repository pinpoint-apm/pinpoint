package com.profiler.util;

public class StringUtils {

    public static String defaultString(String str, String defaultStr) {
        return str == null ? defaultStr : str;
    }

    public static String toString(Object object) {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }

    public static String drop(String str) {
        return drop(str, 20, getDropString(str));
    }

    public static String getDropString(String str) {
        return "...(" + str.length() + ")";
    }

    public static String drop(String str, int length, String padding) {
        if (str == null) {
            return "null";
        }
        StringBuilder buffer = new StringBuilder(length + 10);
        if(str.length() > length) {
            buffer.append(str.substring(0, length));
            buffer.append(padding);
        }
        return buffer.toString();
    }
}
