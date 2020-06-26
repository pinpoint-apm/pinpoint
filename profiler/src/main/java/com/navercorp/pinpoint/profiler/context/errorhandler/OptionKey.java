package com.navercorp.pinpoint.profiler.context.errorhandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OptionKey {
    /**
     * prefix.handlerId.option
     */
    private static final String KEY_PATTERN = "%s.%s.%s";

    public static final String PREFIX = "profiler.error-handler";
    public static final String PATTERN_REGEX = "^(profiler).(error-handler).([A-Za-z0-9\\-_]+).([A-Za-z0-9\\-_.]+)";

    // option
    public static final String CLASSNAME = "class-name";
    public static final String EXCEPTION_MESSAGE_CONTAINS = "exception-message:contains";
//    public static final String EXCEPTION_MESSAGE_STARTWITH = "exception-message:startwith";

    // find caused exception
    public static final String NESTED = "nested";


    public static String getKey(String errorHandlerId, String optionKey) {
        return String.format(KEY_PATTERN, PREFIX, errorHandlerId, optionKey);
    }

    public static String getClassName(String errorHandlerId) {
        return getKey(errorHandlerId, CLASSNAME);
    }

    public static String getExceptionMessageContains(String errorHandlerId) {
        return getKey(errorHandlerId, EXCEPTION_MESSAGE_CONTAINS);
    }

    public static String getNested(String errorHandlerId) {
        return getKey(errorHandlerId, NESTED);
    }

    public static String parseHandlerId(String pattern) {
        Pattern compile = Pattern.compile(PATTERN_REGEX);
        final Matcher matcher = compile.matcher(pattern);
        if (matcher.find()) {
            return matcher.group(3);
        }
        return null;
    }


}
