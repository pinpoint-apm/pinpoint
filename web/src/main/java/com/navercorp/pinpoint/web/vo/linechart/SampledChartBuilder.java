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

import java.util.List;

import com.navercorp.pinpoint.web.vo.linechart.Chart.ChartBuilder;

/**
 * @author hyungil.jeong
 */
public abstract class SampledChartBuilder<X extends Number, Y extends Number> extends ChartBuilder<X, Y> {

    private final DownSampler<Y> downSampler;

    protected SampledChartBuilder(DownSampler<Y> downSampler) {
        if (downSampler == null) {
            throw new NullPointerException("downSampler must not be null");
        }
        this.downSampler = downSampler;
    }

    protected final Y sampleMin(List<Y> sampleBuffer) {
        return this.downSampler.sampleMin(sampleBuffer);
    }

    protected final Y sampleMax(List<Y> sampleBuffer) {
        return this.downSampler.sampleMax(sampleBuffer);
    }

    protected final Y sampleAvg(List<Y> sampleBuffer) {
        return this.downSampler.sampleAvg(sampleBuffer);
    }

}
