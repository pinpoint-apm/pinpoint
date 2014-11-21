package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

/**
 * @author hyungil.jeong
 */
public final class SampledDataLongChartBuilder extends SampledDataChartBuilder<Long, Long> {

    public SampledDataLongChartBuilder(int sampleRate) {
        super(sampleRate);
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
