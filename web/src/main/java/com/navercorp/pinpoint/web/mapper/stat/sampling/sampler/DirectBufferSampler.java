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

import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * @author Roy Kim
 */
@Component
public class DirectBufferSampler implements AgentStatSampler<DirectBufferBo, SampledDirectBuffer> {

    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(SampledDirectBuffer.UNCOLLECTED_VALUE);


    @Override
    public SampledDirectBuffer sampleDataPoints(int timeWindowIndex, long timestamp, List<DirectBufferBo> dataPoints, DirectBufferBo previousDataPoint) {
        final AgentStatPoint<Long> directCount = newAgentStatPoint(timestamp, dataPoints, DirectBufferBo::getDirectCount);
        final AgentStatPoint<Long> directMemoryUsed = newAgentStatPoint(timestamp, dataPoints, DirectBufferBo::getDirectMemoryUsed);
        final AgentStatPoint<Long> mappedCount = newAgentStatPoint(timestamp, dataPoints, DirectBufferBo::getMappedCount);
        final AgentStatPoint<Long> mappedMemoryUsed = newAgentStatPoint(timestamp, dataPoints, DirectBufferBo::getMappedMemoryUsed);

        SampledDirectBuffer sampledDirectBuffer = new SampledDirectBuffer(directCount, directMemoryUsed, mappedCount, mappedMemoryUsed);
        return sampledDirectBuffer;
    }

    private AgentStatPoint<Long> newAgentStatPoint(long timestamp, List<DirectBufferBo> dataPoints, ToLongFunction<DirectBufferBo> filter) {
        List<Long> directBuffers = filter(dataPoints, filter);
        return createPoint(timestamp, directBuffers);
    }

    private List<Long> filter(List<DirectBufferBo> dataPoints, ToLongFunction<DirectBufferBo> filter) {
        final List<Long> result = new ArrayList<>(dataPoints.size());
        for (DirectBufferBo directBufferBo : dataPoints) {
            final long apply = filter.applyAsLong(directBufferBo);
            if (apply != DirectBufferBo.UNCOLLECTED_VALUE) {
                result.add(apply);
            }
        }
        return result;
    }

    private AgentStatPoint<Long> createPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return SampledDirectBuffer.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                LONG_DOWN_SAMPLER.sampleMin(values),
                LONG_DOWN_SAMPLER.sampleMax(values),
                LONG_DOWN_SAMPLER.sampleAvg(values),
                LONG_DOWN_SAMPLER.sampleSum(values));

    }
}
