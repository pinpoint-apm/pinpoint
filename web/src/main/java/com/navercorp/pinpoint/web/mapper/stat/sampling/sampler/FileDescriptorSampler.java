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

import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
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
public class FileDescriptorSampler implements AgentStatSampler<FileDescriptorBo, SampledFileDescriptor> {

    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(SampledFileDescriptor.UNCOLLECTED_VALUE);


    @Override
    public SampledFileDescriptor sampleDataPoints(int timeWindowIndex, long timestamp, List<FileDescriptorBo> dataPoints, FileDescriptorBo previousDataPoint) {
        final AgentStatPoint<Long> openFileDescriptorCount = newAgentStatPoint(timestamp, dataPoints, FileDescriptorBo::getOpenFileDescriptorCount);

        SampledFileDescriptor sampledFileDescriptor = new SampledFileDescriptor(openFileDescriptorCount);
        return sampledFileDescriptor;
    }

    private AgentStatPoint<Long> newAgentStatPoint(long timestamp, List<FileDescriptorBo> dataPoints, ToLongFunction<FileDescriptorBo> filter) {
        List<Long> fileDescriptors = filter(dataPoints, filter);
        return createPoint(timestamp, fileDescriptors);
    }

    private List<Long> filter(List<FileDescriptorBo> dataPoints, ToLongFunction<FileDescriptorBo> filter) {
        final List<Long> result = new ArrayList<>(dataPoints.size());
        for (FileDescriptorBo fileDescriptorBo : dataPoints) {
            final long apply = filter.applyAsLong(fileDescriptorBo);
            if (apply != FileDescriptorBo.UNCOLLECTED_VALUE) {
                result.add(apply);
            }
        }
        return result;
    }

    private AgentStatPoint<Long> createPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return SampledFileDescriptor.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                LONG_DOWN_SAMPLER.sampleMin(values),
                LONG_DOWN_SAMPLER.sampleMax(values),
                LONG_DOWN_SAMPLER.sampleAvg(values),
                LONG_DOWN_SAMPLER.sampleSum(values));

    }
}
