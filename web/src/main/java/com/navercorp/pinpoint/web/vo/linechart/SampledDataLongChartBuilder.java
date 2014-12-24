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
