package com.navercorp.pinpoint.common.server.util.time;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public final class DateTimeUtils {
    private DateTimeUtils() {
    }

    public static final TemporalAdjuster PREV_OR_SAME_SUNDAY = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    public static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

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

}
