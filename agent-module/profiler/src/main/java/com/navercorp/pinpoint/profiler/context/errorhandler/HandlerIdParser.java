package com.navercorp.pinpoint.profiler.context.errorhandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the handlerId from a {@code profiler.ignore-error-handler.<handlerId>.<option>} key.
 * The compiled key pattern lives with the parser instance, so it is released once the options are parsed at startup.
 */
public class HandlerIdParser {

    private final Pattern keyPattern = Pattern.compile(OptionKey.PATTERN_REGEX);

    /**
     * @return the handlerId, or null when the key is not an ignore-error-handler option
     */
    public String parse(String key) {
        final Matcher matcher = keyPattern.matcher(key);
        if (matcher.find()) {
            return matcher.group(3);
        }
        return null;
    }
}
