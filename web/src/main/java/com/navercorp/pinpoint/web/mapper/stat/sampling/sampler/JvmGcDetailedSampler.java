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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPointSummary;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DoubleAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.LongAgentStatPoint;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * @author HyunGil Jeong
 */
@Component
public class JvmGcDetailedSampler implements AgentStatSampler<JvmGcDetailedBo, SampledJvmGcDetailed> {

    private static final int NUM_DECIMAL_PLACES = 1;

    @Override
    public SampledJvmGcDetailed sampleDataPoints(int timeWindowIndex, long timestamp, List<JvmGcDetailedBo> dataPoints, JvmGcDetailedBo previousDataPoint) {
        LongAgentStatPoint gcNewCounts = newLongPoint(timestamp, dataPoints, JvmGcDetailedBo::getGcNewCount);
        LongAgentStatPoint gcNewTimes = newLongPoint(timestamp, dataPoints, JvmGcDetailedBo::getGcNewTime);
        DoubleAgentStatPoint codeCacheUseds = newDoublePoint(timestamp, dataPoints, JvmGcDetailedBo::getCodeCacheUsed);
        DoubleAgentStatPoint newGenUseds = newDoublePoint(timestamp, dataPoints, JvmGcDetailedBo::getNewGenUsed);
        DoubleAgentStatPoint oldGenUseds = newDoublePoint(timestamp, dataPoints, JvmGcDetailedBo::getOldGenUsed);
        DoubleAgentStatPoint survivorSpaceUseds = newDoublePoint(timestamp, dataPoints, JvmGcDetailedBo::getSurvivorSpaceUsed);
        DoubleAgentStatPoint permGenUseds = newDoublePoint(timestamp, dataPoints, JvmGcDetailedBo::getPermGenUsed);
        DoubleAgentStatPoint metaspaceUseds = newDoublePoint(timestamp, dataPoints, JvmGcDetailedBo::getMetaspaceUsed);

        SampledJvmGcDetailed sampledJvmGcDetailed = new SampledJvmGcDetailed(gcNewCounts, gcNewTimes, codeCacheUseds, newGenUseds,
                oldGenUseds, survivorSpaceUseds, permGenUseds, metaspaceUseds);
        return sampledJvmGcDetailed;
    }

    private LongAgentStatPoint newLongPoint(long timestamp, List<JvmGcDetailedBo> dataPoints, ToLongFunction<JvmGcDetailedBo> filter) {
        List<Long> filteredList = longFilter(dataPoints, filter);
        return createLongPoint(timestamp, filteredList);
    }

    private List<Long> longFilter(List<JvmGcDetailedBo> dataPoints, ToLongFunction<JvmGcDetailedBo> filter) {
        final List<Long> result = new ArrayList<>(dataPoints.size());
        for (JvmGcDetailedBo jvmGcDetailedBo : dataPoints) {
            final long apply = filter.applyAsLong(jvmGcDetailedBo);
            if (apply != JvmGcDetailedBo.UNCOLLECTED_VALUE) {
                result.add(apply);
            }
        }
        return result;
    }

    private DoubleAgentStatPoint newDoublePoint(long timestamp, List<JvmGcDetailedBo> dataPoints, ToDoubleFunction<JvmGcDetailedBo> filter) {
        List<Double> filteredList = doubleFilter(dataPoints, filter);
        return createDoublePoint(timestamp, filteredList);
    }

    private List<Double> doubleFilter(List<JvmGcDetailedBo> dataPoints, ToDoubleFunction<JvmGcDetailedBo> filter) {
        final List<Double> result = new ArrayList<>(dataPoints.size());
        for (JvmGcDetailedBo jvmGcDetailedBo : dataPoints) {
            final double apply = filter.applyAsDouble(jvmGcDetailedBo);
            if (apply != JvmGcDetailedBo.UNCOLLECTED_PERCENTAGE) {
                final double percentage = apply * 100;
                result.add(percentage);
            }
        }
        return result;
    }


    private LongAgentStatPoint createLongPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return SampledJvmGcDetailed.UNCOLLECTED_VALUE_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.longSummary(timestamp, values);
    }

    private DoubleAgentStatPoint createDoublePoint(long timestamp, List<Double> values) {
        if (CollectionUtils.isEmpty(values)) {
            return SampledJvmGcDetailed.UNCOLLECTED_PERCENTAGE_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return AgentStatPointSummary.doubleSummaryWithAllScale(timestamp, values, NUM_DECIMAL_PLACES);
    }
}
