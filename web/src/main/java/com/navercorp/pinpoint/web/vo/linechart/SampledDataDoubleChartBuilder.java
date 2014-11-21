package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public class SampledDataDoubleChartBuilder extends SampledDataChartBuilder<Long, Double> {

    public SampledDataDoubleChartBuilder(int sampleRate) {
        super(sampleRate);
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
