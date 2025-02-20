package com.navercorp.pinpoint.profiler.util;

import java.util.Arrays;

public class MaskUtils {

    public static String masking(String input, int prefix) {
        if (input == null) {
            return "null";
        }
        if (input.length() <= 4) {
            return "*****";
        }
        char[] chars = input.toCharArray();
        Arrays.fill(chars, prefix, chars.length, '*');
        return new String(chars);
    }

}
