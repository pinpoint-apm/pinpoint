/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

@Component
public class TotalThreadCountSampler implements AgentStatSampler<TotalThreadCountBo, SampledTotalThreadCount> {
    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(SampledTotalThreadCount.UNCOLLECTED_VALUE);

    @Override
    public SampledTotalThreadCount sampleDataPoints(int index, long timestamp, List<TotalThreadCountBo> dataPoints, TotalThreadCountBo previousDataPoint) {
        final AgentStatPoint<Long> totalThreadCount = newAgentStatPoint(timestamp, dataPoints, TotalThreadCountBo::getTotalThreadCount);
        return new SampledTotalThreadCount(totalThreadCount);
    }

    private AgentStatPoint<Long> newAgentStatPoint(long timestamp, List<TotalThreadCountBo> dataPoints, ToLongFunction<TotalThreadCountBo> filter) {
        List<Long> totalThreadCounts = filter(dataPoints, filter);
        return createPoint(timestamp, totalThreadCounts);
    }

    private List<Long> filter(List<TotalThreadCountBo> dataPoints, ToLongFunction<TotalThreadCountBo> filter) {
        final List<Long> result = new ArrayList<>(dataPoints.size());
        for (TotalThreadCountBo totalThreadCountBo : dataPoints) {
            final long apply = filter.applyAsLong(totalThreadCountBo);
            if (apply != TotalThreadCountBo.UNCOLLECTED_VALUE) {
                result.add(apply);
            }
        }
        return result;
    }

    private AgentStatPoint<Long> createPoint(long timestamp, List<Long> values) {
        if(values.isEmpty()) {
            return SampledTotalThreadCount.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }
        return new AgentStatPoint<>(timestamp, values, LONG_DOWN_SAMPLER);
    }

}
