package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimeWindowDownSampler implements TimeWindowSampler {

    private static final long ONE_MINUTE = 6000 * 10;
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long SIX_HOURS = TimeUnit.HOURS.toMillis(6);
    private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long TWO_DAY = TimeUnit.DAYS.toMillis(2);


    public static final TimeWindowSampler SAMPLER = new TimeWindowDownSampler();

    @Override
    public long getWindowSize(Range range) {
        final long diff = range.getRange();
        long size;
        // 구간 설정 부분은 제고의 여지가 있음.
        if (diff <= ONE_HOUR) {
            size = ONE_MINUTE;
        } else if (diff <= SIX_HOURS) {
            size = ONE_MINUTE * 5;
        } else if (diff <= TWELVE_HOURS) {
            size = ONE_MINUTE * 10;
        } else if (diff <= ONE_DAY) {
            size = ONE_MINUTE * 20;
        } else if (diff <= TWO_DAY) {
            size = ONE_MINUTE * 30;
        } else {
            size = ONE_MINUTE * 60;
        }

        return size;
    }
}
