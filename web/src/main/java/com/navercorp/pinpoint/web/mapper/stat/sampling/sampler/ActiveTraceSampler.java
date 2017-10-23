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

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.TitledAgentStatPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author HyunGil Jeong
 */
@Component
public class ActiveTraceSampler implements AgentStatSampler<ActiveTraceBo, SampledActiveTrace> {

    private static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(SampledActiveTrace.UNCOLLECTED_COUNT);

    @Override
    public SampledActiveTrace sampleDataPoints(int timeWindowIndex, long timestamp, List<ActiveTraceBo> dataPoints, ActiveTraceBo previousDataPoint) {

        final HistogramSchema schema = BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(dataPoints.get(0).getHistogramSchemaType());
        if (schema == null) {
            SampledActiveTrace sampledActiveTrace = new SampledActiveTrace();
            sampledActiveTrace.setFastCounts(SampledActiveTrace.UNCOLLECTED_POINT_CREATER.createUnCollectedPoint(timestamp));
            sampledActiveTrace.setNormalCounts(SampledActiveTrace.UNCOLLECTED_POINT_CREATER.createUnCollectedPoint(timestamp));
            sampledActiveTrace.setSlowCounts(SampledActiveTrace.UNCOLLECTED_POINT_CREATER.createUnCollectedPoint(timestamp));
            sampledActiveTrace.setVerySlowCounts(SampledActiveTrace.UNCOLLECTED_POINT_CREATER.createUnCollectedPoint(timestamp));
            return sampledActiveTrace;
        }

        SampledActiveTrace sampledActiveTrace = new SampledActiveTrace();
        List<Integer> fastCounts = filterActiveTraceBoList(dataPoints, ActiveTraceHistogram::getFastCount);
        sampledActiveTrace.setFastCounts(createSampledTitledPoint(schema.getFastSlot().getSlotName(), timestamp, fastCounts));

        List<Integer> normalCounts = filterActiveTraceBoList(dataPoints, ActiveTraceHistogram::getNormalCount);
        sampledActiveTrace.setNormalCounts(createSampledTitledPoint(schema.getNormalSlot().getSlotName(), timestamp, normalCounts));

        List<Integer> slowCounts = filterActiveTraceBoList(dataPoints, ActiveTraceHistogram::getSlowCount);
        sampledActiveTrace.setSlowCounts(createSampledTitledPoint(schema.getSlowSlot().getSlotName(), timestamp, slowCounts));

        List<Integer> verySlowCounts = filterActiveTraceBoList(dataPoints, ActiveTraceHistogram::getVerySlowCount);
        sampledActiveTrace.setVerySlowCounts(createSampledTitledPoint(schema.getVerySlowSlot().getSlotName(), timestamp, verySlowCounts));

        return sampledActiveTrace;
    }

    private List<Integer> filterActiveTraceBoList(List<ActiveTraceBo> dataPoints, ToIntFunction<ActiveTraceHistogram> counter) {
        final List<Integer> result = new ArrayList<>(dataPoints.size());
        for (ActiveTraceBo activeTraceBo : dataPoints) {
            final ActiveTraceHistogram activeTraceHistogram = activeTraceBo.getActiveTraceHistogram();
            final int count = counter.applyAsInt(activeTraceHistogram);
            if (count != ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT) {
                result.add(count);
            }
        }
        return result;
    }

    private AgentStatPoint<Integer> createSampledTitledPoint(String title, long timestamp, List<Integer> values) {
        if (CollectionUtils.isEmpty(values)) {
            return SampledActiveTrace.UNCOLLECTED_POINT_CREATER.createUnCollectedPoint(timestamp);
        }

        return new TitledAgentStatPoint<>(
                title,
                timestamp,
                INTEGER_DOWN_SAMPLER.sampleMin(values),
                INTEGER_DOWN_SAMPLER.sampleMax(values),
                INTEGER_DOWN_SAMPLER.sampleAvg(values, 1),
                INTEGER_DOWN_SAMPLER.sampleSum(values));
    }
}
