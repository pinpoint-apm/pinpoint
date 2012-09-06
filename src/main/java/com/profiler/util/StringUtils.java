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
}
