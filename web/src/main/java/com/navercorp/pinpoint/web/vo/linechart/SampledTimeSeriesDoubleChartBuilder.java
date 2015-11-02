/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.linechart;

import java.math.BigDecimal;
import java.util.List;

import com.navercorp.pinpoint.web.util.TimeWindow;

/**
 * @author hyungil.jeong
 */
public class SampledTimeSeriesDoubleChartBuilder extends SampledTimeSeriesChartBuilder<Double> {

    private static final Double DEFAULT_VALUE = 0D;
    private static final int DEFAULT_SCALE = 2;

    private final int scale;

    public SampledTimeSeriesDoubleChartBuilder(TimeWindow timeWindow) {
        this(timeWindow, DEFAULT_VALUE, DEFAULT_SCALE);
    }

    public SampledTimeSeriesDoubleChartBuilder(TimeWindow timeWindow, double defaultValue) {
        this(timeWindow, defaultValue, DEFAULT_SCALE);
    }

    public SampledTimeSeriesDoubleChartBuilder(TimeWindow timeWindow, double defaultValue, int scale) {
        super(timeWindow, defaultValue);
        if (scale < 1) {
            this.scale = DEFAULT_SCALE;
        } else {
            this.scale = scale;
        }
    }

    @Override
    protected Double sampleMin(List<Double> sampleBuffer) {
        return roundToScale(DownSamplers.MIN.sampleDouble(sampleBuffer));
    }

    @Override
    protected Double sampleMax(List<Double> sampleBuffer) {
        return roundToScale(DownSamplers.MAX.sampleDouble(sampleBuffer));
    }

    @Override
    protected Double sampleAvg(List<Double> sampleBuffer) {
        return roundToScale(DownSamplers.AVG.sampleDouble(sampleBuffer));
    }

    private double roundToScale(double value) {
        return new BigDecimal(value).setScale(this.scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

}
