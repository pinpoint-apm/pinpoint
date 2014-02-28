package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

/**
 * @author emeroad
 */
public class TimeWindowDownSampler implements TimeWindowSampler {

    private static final int ONE_MINUTE = 60000;
    private static final int ONE_HOUR = ONE_MINUTE * 60;
    private static final int SIX_HOURS = ONE_HOUR * 6;
    private static final int ONE_DAY = SIX_HOURS * 4;

    public static final TimeWindowSampler SAMPLER = new TimeWindowDownSampler();

    @Override
    public long getWindowSize(Range range) {
        long diff = range.getRange();
        int size;
        // 구간 설정 부분은 제고의 여지가 있음.
        if (diff <= ONE_HOUR) {
            size = ONE_MINUTE;
        } else if (diff <= SIX_HOURS) {
            size = ONE_MINUTE * 5;
        } else if (diff <= ONE_DAY) {
            size = ONE_MINUTE * 10;
        } else if (diff <= ONE_DAY * 2) {
            size = ONE_MINUTE * 15;
        } else {
            size = ONE_MINUTE * 20;
        }

        return size;
    }
}
