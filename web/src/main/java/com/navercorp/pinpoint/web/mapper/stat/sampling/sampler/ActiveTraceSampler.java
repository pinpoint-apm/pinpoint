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
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.IntAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.TitledAgentStatPoint;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * @author HyunGil Jeong
 */
@Component
public class ActiveTraceSampler implements AgentStatSampler<ActiveTraceBo, SampledActiveTrace> {

    @Override
    public SampledActiveTrace sampleDataPoints(int timeWindowIndex, long timestamp, List<ActiveTraceBo> dataPoints, ActiveTraceBo previousDataPoint) {

        final HistogramSchema schema = BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(dataPoints.get(0).getHistogramSchemaType());
        if (schema == null) {
            return newUnSampledActiveTrace(timestamp);
        }

        IntAgentStatPoint fast = newAgentStatPoint(schema.getFastSlot(), timestamp, dataPoints, ActiveTraceHistogram::getFastCount);
        IntAgentStatPoint normal = newAgentStatPoint(schema.getNormalSlot(), timestamp, dataPoints, ActiveTraceHistogram::getNormalCount);
        IntAgentStatPoint slow = newAgentStatPoint(schema.getSlowSlot(), timestamp, dataPoints, ActiveTraceHistogram::getSlowCount);
        IntAgentStatPoint verySlow = newAgentStatPoint(schema.getVerySlowSlot(), timestamp, dataPoints, ActiveTraceHistogram::getVerySlowCount);
        SampledActiveTrace sampledActiveTrace = new SampledActiveTrace(fast, normal, slow, verySlow);

        return sampledActiveTrace;
    }

    private SampledActiveTrace newUnSampledActiveTrace(long timestamp) {
        Point.UncollectedPointCreator<IntAgentStatPoint> uncollected = SampledActiveTrace.UNCOLLECTED_POINT_CREATOR;
        IntAgentStatPoint fast = uncollected.createUnCollectedPoint(timestamp);
        IntAgentStatPoint normal = uncollected.createUnCollectedPoint(timestamp);
        IntAgentStatPoint slow = uncollected.createUnCollectedPoint(timestamp);
        IntAgentStatPoint verySlow = uncollected.createUnCollectedPoint(timestamp);
        return new SampledActiveTrace(fast, normal, slow, verySlow);
    }

    private IntAgentStatPoint newAgentStatPoint(HistogramSlot slot, long timestamp, List<ActiveTraceBo> dataPoints, ToIntFunction<ActiveTraceHistogram> counter) {
        List<Integer> fastCounts = filterActiveTraceBoList(dataPoints, counter);
        return createSampledTitledPoint(slot.getSlotName(), timestamp, fastCounts);
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

    private IntAgentStatPoint createSampledTitledPoint(String title, long timestamp, List<Integer> values) {
        if (CollectionUtils.isEmpty(values)) {
            return SampledActiveTrace.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }
        TitledAgentStatPoint agentStatPoint = new TitledAgentStatPoint(title, timestamp, 1);
        values.stream()
                .mapToInt(Integer::intValue)
                .forEach(agentStatPoint);
        return agentStatPoint;
    }
}
