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

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
public class LegacySampledTitledTimeSeriesChartBuilder<Y extends Number> extends LegacySampledTimeSeriesChartBuilder<TitledDataPoint<Long, Y>, Y> {

    public LegacySampledTitledTimeSeriesChartBuilder(DownSampler<Y> downSampler, Integer avgNumDecimals, TimeWindow timeWindow) {
        super(downSampler, avgNumDecimals, timeWindow);
    }

    @Override
    protected Point<Long, Y> sampleDataPoints(long timestamp, List<TitledDataPoint<Long, Y>> samples) {
        String title = samples.get(0).getTitle();
        List<Y> values = new ArrayList<>(samples.size());
        for (TitledDataPoint<Long, Y> sample : samples) {
            values.add(sample.getYVal());
        }
        return new TitledPoint<>(title, timestamp, sampleMin(values), sampleMax(values), sampleAvg(values), sampleSum(values));
    }
}
