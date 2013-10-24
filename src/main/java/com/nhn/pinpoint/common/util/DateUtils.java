package com.nhn.pinpoint.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class DateUtils {

    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss SSS";

    public static String longToDateStr(long date) {
        return longToDateStr(date, FORMAT);
    }

    public static String longToDateStr(long date, String fmt) {
        final SimpleDateFormat format = new SimpleDateFormat((fmt == null) ? FORMAT : fmt);
        return format.format(new Date(date));
    }
}
