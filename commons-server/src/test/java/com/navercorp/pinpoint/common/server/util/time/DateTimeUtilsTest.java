package com.navercorp.pinpoint.common.server.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

class DateTimeUtilsTest {

    @Test
    public void previousOrSameSundayToMillis() {
        // If the timezone is different, the time may not be the same.
        long saveTime = getSaveTime(System.currentTimeMillis());
        long prevSunDay = DateTimeUtils.previousOrSameSundayToMillis();
        Assertions.assertEquals(saveTime, prevSunDay);
    }

    private long getSaveTime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTimeInMillis();
    }

}