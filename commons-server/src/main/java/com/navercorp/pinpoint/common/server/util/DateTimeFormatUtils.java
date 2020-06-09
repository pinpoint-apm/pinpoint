/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DateTimeFormatUtils {


    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
//   log4j2 fast date format
//    private static final FixedDateFormat FIXED_DEFAULT_PERIOD_FORMAT = FixedDateFormat.create(FixedDateFormat.FixedFormat.DEFAULT_PERIOD);

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss SSS";
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT).withZone(DEFAULT_ZONE_ID);

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT).withZone(DEFAULT_ZONE_ID);

    public static final String ABSOLUTE_DATE_FORMAT = "HH:mm:ss SSS";
    private static final DateTimeFormatter ABSOLUTE_DATE_FORMATTER = DateTimeFormatter.ofPattern(ABSOLUTE_DATE_FORMAT).withZone(DEFAULT_ZONE_ID);

    /**
     * Date pattern : {@value DEFAULT_DATE_FORMAT}
     */
    public static String format(long epochMillis) {
        return format0(DEFAULT_DATE_FORMATTER, epochMillis);
    }


    public static long parse(String dateSource) throws DateTimeParseException {
        Objects.requireNonNull(dateSource, "dateSource");
        return parse0(DEFAULT_DATE_FORMATTER, dateSource);
    }


    /**
     * Date pattern : {@value SIMPLE_DATE_FORMAT}
     */
    public static String formatSimple(long epochMillis) {
        return format0(SIMPLE_DATE_FORMATTER, epochMillis);
    }

    public static long parseSimple(String dateSource) throws DateTimeParseException {
        Objects.requireNonNull(dateSource, "dateSource");
        return parse0(SIMPLE_DATE_FORMATTER, dateSource);
    }

    /**
     * Date pattern : {@value ABSOLUTE_DATE_FORMAT}
     */
    public static String formatAbsolute(long epochMillis) {
        return format0(ABSOLUTE_DATE_FORMATTER, epochMillis);
    }

    private static String format0(DateTimeFormatter formatter, long epochMillis) {
        final Instant instant = Instant.ofEpochMilli(epochMillis);
        return formatter.format(instant);
    }

    private static long parse0(DateTimeFormatter formatter, String dateSource) {
        final Instant instant = formatter.parse(dateSource, Instant::from);
        return instant.toEpochMilli();
    }
}
