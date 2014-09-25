package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimeWindowOneMinuteSampler implements TimeWindowSampler {

    public static final TimeWindowSampler SAMPLER = new TimeWindowOneMinuteSampler();

    @Override
    public long getWindowSize(Range range) {
        return 1000*60;
    }
}
