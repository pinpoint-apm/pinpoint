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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SampledCpuLoadResultExtractor extends SampledAgentStatResultExtractor<CpuLoadBo, SampledCpuLoad> {

    private static final double UNCOLLECTED_CPU_LOAD = -1D;
    private static final int NUM_DECIMAL_PLACES = 1;
    public static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(UNCOLLECTED_CPU_LOAD, NUM_DECIMAL_PLACES);

    public SampledCpuLoadResultExtractor(TimeWindow timeWindow, AgentStatMapper<CpuLoadBo> rowMapper) {
        super(timeWindow, rowMapper);
    }

    @Override
    protected SampledCpuLoad sampleCurrentBatch(long timestamp, List<CpuLoadBo> dataPointsToSample) {
        List<Double> jvmCpuLoads = new ArrayList<>(dataPointsToSample.size());
        List<Double> systemCpuLoads = new ArrayList<>(dataPointsToSample.size());
        for (CpuLoadBo cpuLoadBo : dataPointsToSample) {
            jvmCpuLoads.add(cpuLoadBo.getJvmCpuLoad() * 100);
            systemCpuLoads.add(cpuLoadBo.getSystemCpuLoad() * 100);
        }
        SampledCpuLoad sampledCpuLoad = new SampledCpuLoad();
        sampledCpuLoad.setJvmCpuLoad(createPoint(timestamp, jvmCpuLoads));
        sampledCpuLoad.setSystemCpuLoad(createPoint(timestamp, systemCpuLoads));
        return sampledCpuLoad;
    }

    private Point<Long, Double> createPoint(long timestamp, List<Double> values) {
        return new Point<>(timestamp, DOUBLE_DOWN_SAMPLER.sampleMin(values), DOUBLE_DOWN_SAMPLER.sampleMax(values), DOUBLE_DOWN_SAMPLER.sampleAvg(values));
    }
}
