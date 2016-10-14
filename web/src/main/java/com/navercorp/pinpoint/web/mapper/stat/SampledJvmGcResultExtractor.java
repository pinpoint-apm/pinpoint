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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SampledJvmGcResultExtractor extends SampledAgentStatResultExtractor<JvmGcBo, SampledJvmGc> {

    private static final long UNCOLLECTED_VALUE = -1L;
    public static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(UNCOLLECTED_VALUE);

    public SampledJvmGcResultExtractor(TimeWindow timeWindow, AgentStatMapper<JvmGcBo> rowMapper) {
        super(timeWindow, rowMapper);
    }

    @Override
    protected SampledJvmGc sampleCurrentBatch(long timestamp, List<JvmGcBo> dataPointsToSample) {
        JvmGcType jvmGcType = JvmGcType.UNKNOWN;
        List<Long> heapUseds = new ArrayList<>(dataPointsToSample.size());
        List<Long> heapMaxes = new ArrayList<>(dataPointsToSample.size());
        List<Long> nonHeapUseds = new ArrayList<>(dataPointsToSample.size());
        List<Long> nonHeapMaxes = new ArrayList<>(dataPointsToSample.size());
        List<Long> gcOldCounts = new ArrayList<>(dataPointsToSample.size());
        List<Long> gcOldTimes = new ArrayList<>(dataPointsToSample.size());
        for (JvmGcBo jvmGcBo : dataPointsToSample) {
            jvmGcType = jvmGcBo.getGcType();
            heapUseds.add(jvmGcBo.getHeapUsed());
            heapMaxes.add(jvmGcBo.getHeapMax());
            nonHeapUseds.add(jvmGcBo.getNonHeapUsed());
            nonHeapMaxes.add(jvmGcBo.getNonHeapMax());
            gcOldCounts.add(jvmGcBo.getGcOldCount());
            gcOldTimes.add(jvmGcBo.getGcOldTime());
        }
        SampledJvmGc sampledJvmGc = new SampledJvmGc();
        sampledJvmGc.setJvmGcType(jvmGcType);
        sampledJvmGc.setHeapUsed(createPoint(timestamp, heapUseds));
        sampledJvmGc.setHeapMax(createPoint(timestamp, heapMaxes));
        sampledJvmGc.setNonHeapUsed(createPoint(timestamp, nonHeapUseds));
        sampledJvmGc.setNonHeapMax(createPoint(timestamp, nonHeapMaxes));
        sampledJvmGc.setGcOldCount(createPoint(timestamp, gcOldCounts));
        sampledJvmGc.setGcOldTime(createPoint(timestamp, gcOldTimes));
        return sampledJvmGc;
    }

    private Point<Long, Long> createPoint(long timestamp, List<Long> values) {
        return new Point<>(
                timestamp,
                LONG_DOWN_SAMPLER.sampleMin(values),
                LONG_DOWN_SAMPLER.sampleMax(values),
                LONG_DOWN_SAMPLER.sampleAvg(values, 0));
    }

}
