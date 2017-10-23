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

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class JvmGcDetailedSampler implements AgentStatSampler<JvmGcDetailedBo, SampledJvmGcDetailed> {

    private static final int NUM_DECIMAL_PLACES = 1;
    private static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(SampledJvmGcDetailed.UNCOLLECTED_VALUE);
    private static final DownSampler<Double> DOUBLE_DOWN_SAMPLER = DownSamplers.getDoubleDownSampler(SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE, NUM_DECIMAL_PLACES);

    @Override
    public SampledJvmGcDetailed sampleDataPoints(int timeWindowIndex, long timestamp, List<JvmGcDetailedBo> dataPoints, JvmGcDetailedBo previousDataPoint) {
        List<Long> gcNewCounts = new ArrayList<>(dataPoints.size());
        List<Long> gcNewTimes = new ArrayList<>(dataPoints.size());
        List<Double> codeCacheUseds = new ArrayList<>(dataPoints.size());
        List<Double> newGenUseds = new ArrayList<>(dataPoints.size());
        List<Double> oldGenUseds = new ArrayList<>(dataPoints.size());
        List<Double> survivorSpaceUseds = new ArrayList<>(dataPoints.size());
        List<Double> permGenUseds = new ArrayList<>(dataPoints.size());
        List<Double> metaspaceUseds = new ArrayList<>(dataPoints.size());
        for (JvmGcDetailedBo jvmGcDetailedBo : dataPoints) {
            if (jvmGcDetailedBo.getGcNewCount() != JvmGcDetailedBo.UNCOLLECTED_VALUE) {
                gcNewCounts.add(jvmGcDetailedBo.getGcNewCount());
            }
            if (jvmGcDetailedBo.getGcNewTime() != JvmGcDetailedBo.UNCOLLECTED_VALUE) {
                gcNewTimes.add(jvmGcDetailedBo.getGcNewTime());
            }
            if (jvmGcDetailedBo.getCodeCacheUsed() != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                codeCacheUseds.add(jvmGcDetailedBo.getCodeCacheUsed() * 100);
            }
            if (jvmGcDetailedBo.getNewGenUsed() != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                newGenUseds.add(jvmGcDetailedBo.getNewGenUsed() * 100);
            }
            if (jvmGcDetailedBo.getOldGenUsed() != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                oldGenUseds.add(jvmGcDetailedBo.getOldGenUsed() * 100);
            }
            if (jvmGcDetailedBo.getSurvivorSpaceUsed() != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                survivorSpaceUseds.add(jvmGcDetailedBo.getSurvivorSpaceUsed() * 100);
            }
            if (jvmGcDetailedBo.getPermGenUsed() != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                permGenUseds.add(jvmGcDetailedBo.getPermGenUsed() * 100);
            }
            if (jvmGcDetailedBo.getMetaspaceUsed() != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                metaspaceUseds.add(jvmGcDetailedBo.getMetaspaceUsed() * 100);
            }
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

    private AgentStatPoint<Long> createLongPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return SampledJvmGcDetailed.UNCOLLECTED_VALUE_POINT_CREATER.createUnCollectedPoint(timestamp);
        } else {
            return new AgentStatPoint<>(
                    timestamp,
                    LONG_DOWN_SAMPLER.sampleMin(values),
                    LONG_DOWN_SAMPLER.sampleMax(values),
                    LONG_DOWN_SAMPLER.sampleAvg(values, 0),
                    LONG_DOWN_SAMPLER.sampleSum(values));
        }
    }

    private AgentStatPoint<Double> createDoublePoint(long timestamp, List<Double> values) {
        if (values.isEmpty()) {
            return SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE_POINT_CREATOR.createUnCollectedPoint(timestamp);
        } else {
            return new AgentStatPoint<>(
                    timestamp,
                    DOUBLE_DOWN_SAMPLER.sampleMin(values),
                    DOUBLE_DOWN_SAMPLER.sampleMax(values),
                    DOUBLE_DOWN_SAMPLER.sampleAvg(values),
                    DOUBLE_DOWN_SAMPLER.sampleSum(values));
        }
    }
}
