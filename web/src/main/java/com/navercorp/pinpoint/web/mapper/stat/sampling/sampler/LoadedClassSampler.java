/*
 * Copyright 2017 NAVER Corp.
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


import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

@Component
public class LoadedClassSampler implements AgentStatSampler<LoadedClassBo, SampledLoadedClassCount> {

    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(SampledLoadedClassCount.UNCOLLECTED_VALUE);


    @Override
    public SampledLoadedClassCount sampleDataPoints(int timeWindowIndex, long timestamp, List<LoadedClassBo> dataPoints, LoadedClassBo previousDataPoint) {
        final AgentStatPoint<Long> loadedClassCount = newAgentStatPoint(timestamp, dataPoints, LoadedClassBo::getLoadedClassCount);
        final AgentStatPoint<Long> unloadedClassCount = newAgentStatPoint(timestamp, dataPoints, LoadedClassBo::getUnloadedClassCount);

        SampledLoadedClassCount sampledLoadedClassCount = new SampledLoadedClassCount(loadedClassCount, unloadedClassCount);
        return sampledLoadedClassCount;
    }

    private AgentStatPoint<Long> newAgentStatPoint(long timestamp, List<LoadedClassBo> dataPoints, ToLongFunction<LoadedClassBo> filter) {
        List<Long> loadedCounts = filter(dataPoints, filter);
        return createPoint(timestamp, loadedCounts);
    }

    private List<Long> filter(List<LoadedClassBo> dataPoints, ToLongFunction<LoadedClassBo> filter) {
        final List<Long> result = new ArrayList<>(dataPoints.size());
        for (LoadedClassBo loadedClassBo : dataPoints) {
            final long apply = filter.applyAsLong(loadedClassBo);
            if (apply != LoadedClassBo.UNCOLLECTED_VALUE) {
                result.add(apply);
            }
        }
        return result;
    }

    private AgentStatPoint<Long> createPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return SampledLoadedClassCount.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }
        return new AgentStatPoint<>(timestamp, values, LONG_DOWN_SAMPLER);
    }
}
