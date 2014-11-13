package com.nhn.pinpoint.web.vo.linechart;

import java.util.List;

import com.nhn.pinpoint.web.vo.linechart.Chart.ChartBuilder;

/**
 * @author hyungil.jeong
 */
public abstract class SampledChartBuilder<X extends Number, Y extends Number> extends ChartBuilder<X, Y> {

    protected abstract Y sampleMin(List<Y> sampleBuffer);

    protected abstract Y sampleMax(List<Y> sampleBuffer);

    protected abstract Y sampleAvg(List<Y> sampleBuffer);
}
