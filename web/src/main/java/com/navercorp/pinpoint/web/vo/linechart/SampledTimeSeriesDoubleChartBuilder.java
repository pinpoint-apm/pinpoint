package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

import com.nhn.pinpoint.web.util.TimeWindow;

/**
 * @author hyungil.jeong
 */
public class SampledTimeSeriesDoubleChartBuilder extends SampledTimeSeriesChartBuilder<Double> {
    
    private static final Double DEFAULT_VALUE = 0D;
    
    public SampledTimeSeriesDoubleChartBuilder(TimeWindow timeWindow) {
        super(timeWindow, DEFAULT_VALUE);
    }
    
    public SampledTimeSeriesDoubleChartBuilder(TimeWindow timeWindow, double defaultValue) {
        super(timeWindow, defaultValue);
    }
    
    @Override
    protected Double sampleMin(List<Double> sampleBuffer) {
        return DownSamplers.MIN.sampleDouble(sampleBuffer);
    }

    @Override
    protected Double sampleMax(List<Double> sampleBuffer) {
        return DownSamplers.MAX.sampleDouble(sampleBuffer);
    }

    @Override
    protected Double sampleAvg(List<Double> sampleBuffer) {
        return DownSamplers.AVG.sampleDouble(sampleBuffer);
    }

}
