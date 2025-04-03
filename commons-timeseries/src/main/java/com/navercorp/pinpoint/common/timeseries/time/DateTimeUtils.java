package com.navercorp.pinpoint.common.timeseries.time;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

public final class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static final TemporalAdjuster PREV_OR_SAME_SUNDAY = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT).withZone(DEFAULT_ZONE);


    /**
     * Returns the current time in milliseconds.
     * precision : milliseconds
     * @return the current time in milliseconds
     */
    public static Instant epochMilli() {
        return Instant.ofEpochMilli(System.currentTimeMillis());
    }

    public static ZonedDateTime previousOrSameSunday(Instant instant, ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.ofInstant(instant, zoneId);
        return now.truncatedTo(ChronoUnit.DAYS)
                .with(PREV_OR_SAME_SUNDAY);
    }

    public static long previousOrSameSundayToMillis(Instant instant, ZoneId zoneId) {
        ZonedDateTime prev = previousOrSameSunday(instant, zoneId);
        return prev.toInstant().toEpochMilli();
    }

    public static long previousOrSameSundayToMillis() {
        Instant now = DateTimeUtils.epochMilli();
        return previousOrSameSundayToMillis(now, DEFAULT_ZONE);
    }

    public static String formatSimple(long timestamp) {
        return formatSimple(Instant.ofEpochMilli(timestamp));
    }

    public static String formatSimple(TemporalAccessor temporalAccessor) {
        Objects.requireNonNull(temporalAccessor, "temporalAccessor");
        return SIMPLE_DATE_FORMATTER.format(temporalAccessor);
    }

}
