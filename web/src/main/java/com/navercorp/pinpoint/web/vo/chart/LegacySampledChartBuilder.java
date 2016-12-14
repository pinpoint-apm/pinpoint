/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.chart;

import java.util.List;

import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;

/**
 * @author hyungil.jeong
 */
@Deprecated
public abstract class LegacySampledChartBuilder<D extends DataPoint<X, Y>, X extends Number, Y extends Number> extends LegacyChartBuilder<D, X, Y> {

    private final DownSampler<Y> downSampler;
    private final Integer avgNumDecimals;
    protected final Y defaultValue;

    protected LegacySampledChartBuilder(DownSampler<Y> downSampler, Integer avgNumDecimals) {
        if (downSampler == null) {
            throw new NullPointerException("downSampler must not be null");
        }
        this.downSampler = downSampler;
        this.avgNumDecimals = avgNumDecimals;
        this.defaultValue = downSampler.getDefaultValue();
    }

    protected final Y sampleMin(List<Y> sampleBuffer) {
        return this.downSampler.sampleMin(sampleBuffer);
    }

    protected final Y sampleMax(List<Y> sampleBuffer) {
        return this.downSampler.sampleMax(sampleBuffer);
    }

    protected final double sampleAvg(List<Y> sampleBuffer) {
        return this.downSampler.sampleAvg(sampleBuffer, this.avgNumDecimals);
    }

    protected final Y sampleSum(List<Y> sampleBuffer) {
        return this.downSampler.sampleSum(sampleBuffer);
    }
}
