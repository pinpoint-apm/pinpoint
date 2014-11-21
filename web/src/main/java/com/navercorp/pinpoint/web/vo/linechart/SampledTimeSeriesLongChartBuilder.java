package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

import com.nhn.pinpoint.web.util.TimeWindow;

/**
 * @author hyungil.jeong
 */
public class SampledTimeSeriesLongChartBuilder extends SampledTimeSeriesChartBuilder<Long> {
    
    private static final Long DEFAULT_VALUE = 0L;
    
    public SampledTimeSeriesLongChartBuilder(TimeWindow timeWindow) {
        super(timeWindow, DEFAULT_VALUE);
    }
    
    public SampledTimeSeriesLongChartBuilder(TimeWindow timeWindow, long defaultValue) {
        super(timeWindow, defaultValue);
    }

    @Override
    protected Long sampleMin(List<Long> sampleBuffer) {
        return DownSamplers.MIN.sampleLong(sampleBuffer);
    }

    @Override
    protected Long sampleMax(List<Long> sampleBuffer) {
        return DownSamplers.MAX.sampleLong(sampleBuffer);
    }

    @Override
    protected Long sampleAvg(List<Long> sampleBuffer) {
        return DownSamplers.AVG.sampleLong(sampleBuffer);
    }

}
