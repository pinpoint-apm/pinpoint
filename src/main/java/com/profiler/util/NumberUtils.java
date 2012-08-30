package com.profiler.util;

public class NumberUtils {
    public static long parseLong(String str, long defaultLong) {
        if(str== null) {
            return defaultLong;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultLong;
        }
    }

    public static int parseInteger(String str, int defaultInt) {
        if(str == null) {
            return defaultInt;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultInt;
        }
    }

}
