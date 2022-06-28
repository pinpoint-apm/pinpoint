package com.navercorp.pinpoint.pluginit.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogUtils {
    private static final String LINE_BREAK_REGEX = "((\\r?\\n)|(\\r))";
    private static final String LINE_BREAK_AT_END_REGEX = LINE_BREAK_REGEX + "$";
    private static final Pattern LINE_BREAK_AT_END_REGEX_PATTERN = Pattern.compile(LINE_BREAK_AT_END_REGEX);

    private LogUtils() {
    }

    public static String removeLineBreak(String log) {
        Matcher matcher = LINE_BREAK_AT_END_REGEX_PATTERN.matcher(log);
        return matcher.replaceAll("");
    }
}
