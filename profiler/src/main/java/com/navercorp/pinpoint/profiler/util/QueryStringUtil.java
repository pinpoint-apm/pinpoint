package com.nhn.pinpoint.profiler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public final class QueryStringUtil {

    private  QueryStringUtil() {
    }

    public static String removeCarriageReturn(String query) {
//		query.replaceAll(regex, replacement)
        String result = query.replaceAll("[\r\n]", " ");
        return result;
    }

    private static final Pattern MULTI_SPACE_ESCAPE = Pattern.compile(" +");

    public static String removeAllMultiSpace(String query) {
        if (query == null || query.length() == 0) {
            return "";
        }
        Matcher matcher = MULTI_SPACE_ESCAPE.matcher(query);
        return matcher.replaceAll(" ");
    }
}
