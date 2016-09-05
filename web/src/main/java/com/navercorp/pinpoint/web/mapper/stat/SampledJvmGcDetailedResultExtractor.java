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

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class SampledJvmGcDetailedResultExtractor extends SampledAgentStatResultExtractor<JvmGcDetailedBo, SampledJvmGcDetailed> {

    private static final long UNCOLLECTED_VALUE = -1L;
    private static final int NUM_DECIMAL_PLACES = 1;
    public static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(UNCOLLECTED_VALUE);
    public static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(UNCOLLECTED_VALUE, NUM_DECIMAL_PLACES);

    public SampledJvmGcDetailedResultExtractor(TimeWindow timeWindow, AgentStatMapper<JvmGcDetailedBo> rowMapper) {
        super(timeWindow, rowMapper);
    }

    @Override
    protected SampledJvmGcDetailed sampleCurrentBatch(long timestamp, List<JvmGcDetailedBo> dataPointsToSample) {
        List<Long> gcNewCounts = new ArrayList<>(dataPointsToSample.size());
        List<Long> gcNewTimes = new ArrayList<>(dataPointsToSample.size());
        List<Double> codeCacheUseds = new ArrayList<>(dataPointsToSample.size());
        List<Double> newGenUseds = new ArrayList<>(dataPointsToSample.size());
        List<Double> oldGenUseds = new ArrayList<>(dataPointsToSample.size());
        List<Double> survivorSpaceUseds = new ArrayList<>(dataPointsToSample.size());
        List<Double> permGenUseds = new ArrayList<>(dataPointsToSample.size());
        List<Double> metaspaceUseds = new ArrayList<>(dataPointsToSample.size());
        for (JvmGcDetailedBo jvmGcDetailedBo : dataPointsToSample) {
            gcNewCounts.add(jvmGcDetailedBo.getGcNewCount());
            gcNewTimes.add(jvmGcDetailedBo.getGcNewTime());
            codeCacheUseds.add(jvmGcDetailedBo.getCodeCacheUsed() * 100);
            newGenUseds.add(jvmGcDetailedBo.getNewGenUsed() * 100);
            oldGenUseds.add(jvmGcDetailedBo.getOldGenUsed() * 100);
            survivorSpaceUseds.add(jvmGcDetailedBo.getSurvivorSpaceUsed() * 100);
            permGenUseds.add(jvmGcDetailedBo.getPermGenUsed() * 100);
            metaspaceUseds.add(jvmGcDetailedBo.getMetaspaceUsed() * 100);
        }
        SampledJvmGcDetailed sampledJvmGcDetailed = new SampledJvmGcDetailed();
        sampledJvmGcDetailed.setGcNewCount(createLongPoint(timestamp, gcNewCounts));
        sampledJvmGcDetailed.setGcNewTime(createLongPoint(timestamp, gcNewTimes));
        sampledJvmGcDetailed.setCodeCacheUsed(createDoublePoint(timestamp, codeCacheUseds));
        sampledJvmGcDetailed.setNewGenUsed(createDoublePoint(timestamp, newGenUseds));
        sampledJvmGcDetailed.setOldGenUsed(createDoublePoint(timestamp, oldGenUseds));
        sampledJvmGcDetailed.setSurvivorSpaceUsed(createDoublePoint(timestamp, survivorSpaceUseds));
        sampledJvmGcDetailed.setPermGenUsed(createDoublePoint(timestamp, permGenUseds));
        sampledJvmGcDetailed.setMetaspaceUsed(createDoublePoint(timestamp, metaspaceUseds));
        return sampledJvmGcDetailed;
    }

    private Point<Long, Long> createLongPoint(long timestamp, List<Long> values) {
        return new Point<>(timestamp, LONG_DOWN_SAMPLER.sampleMin(values), LONG_DOWN_SAMPLER.sampleMax(values), LONG_DOWN_SAMPLER.sampleAvg(values));
    }

    private Point<Long, Double> createDoublePoint(long timestamp, List<Double> values) {
        return new Point(timestamp, DOUBLE_DOWN_SAMPLER.sampleMin(values), DOUBLE_DOWN_SAMPLER.sampleMax(values), DOUBLE_DOWN_SAMPLER.sampleAvg(values));
    }
}
