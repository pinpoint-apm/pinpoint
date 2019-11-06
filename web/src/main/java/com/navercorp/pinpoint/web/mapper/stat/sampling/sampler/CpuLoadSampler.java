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

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * @author HyunGil Jeong
 */
@Component
public class CpuLoadSampler implements AgentStatSampler<CpuLoadBo, SampledCpuLoad> {

    private static final int NUM_DECIMAL_PLACES = 1;
    private static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(SampledCpuLoad.UNCOLLECTED_PERCENTAGE, NUM_DECIMAL_PLACES);

    @Override
    public SampledCpuLoad sampleDataPoints(int timeWindowIndex, long timestamp, List<CpuLoadBo> dataPoints, CpuLoadBo previousDataPoint) {
        final AgentStatPoint<Double> jvmCpuLoad = newAgentStatPoint(timestamp, dataPoints, CpuLoadBo::getJvmCpuLoad);
        final AgentStatPoint<Double> systemCpuLoad = newAgentStatPoint(timestamp, dataPoints, CpuLoadBo::getSystemCpuLoad);

        SampledCpuLoad sampledCpuLoad = new SampledCpuLoad(jvmCpuLoad, systemCpuLoad);
        return sampledCpuLoad;
    }

    private AgentStatPoint<Double> newAgentStatPoint(long timestamp, List<CpuLoadBo> dataPoints, ToDoubleFunction<CpuLoadBo> filter) {
        List<Double> jvmCpuLoads = filter(dataPoints, filter);
        return createPoint(timestamp, jvmCpuLoads);
    }

    private List<Double> filter(List<CpuLoadBo> dataPoints, ToDoubleFunction<CpuLoadBo> filter) {
        final List<Double> result = new ArrayList<>(dataPoints.size());
        for (CpuLoadBo cpuLoadBo : dataPoints) {
            final double apply = filter.applyAsDouble(cpuLoadBo);
            if (apply != CpuLoadBo.UNCOLLECTED_VALUE) {
                result.add(apply * 100);
            }
        }
        return result;
    }

    private AgentStatPoint<Double> createPoint(long timestamp, List<Double> values) {
        if (values.isEmpty()) {
            return SampledCpuLoad.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                DOUBLE_DOWN_SAMPLER.sampleMin(values),
                DOUBLE_DOWN_SAMPLER.sampleMax(values),
                DOUBLE_DOWN_SAMPLER.sampleAvg(values),
                DOUBLE_DOWN_SAMPLER.sampleSum(values));

    }
}
