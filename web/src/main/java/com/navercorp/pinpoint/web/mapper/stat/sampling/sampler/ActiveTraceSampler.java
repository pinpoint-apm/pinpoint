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
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.chart.TitledPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@Component
public class ActiveTraceSampler implements AgentStatSampler<ActiveTraceBo, SampledActiveTrace> {

    public static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT);

    @Override
    public SampledActiveTrace sampleDataPoints(int timeWindowIndex, long timestamp, List<ActiveTraceBo> dataPoints, ActiveTraceBo previousDataPoint) {
        SampledActiveTrace sampledActiveTrace = new SampledActiveTrace();
        HistogramSchema schema = BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(dataPoints.get(0).getHistogramSchemaType());
        if (schema == null) {
            sampledActiveTrace.setFastCounts(new UncollectedPoint<>(timestamp, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
            sampledActiveTrace.setNormalCounts(new UncollectedPoint<>(timestamp, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
            sampledActiveTrace.setSlowCounts(new UncollectedPoint<>(timestamp, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
            sampledActiveTrace.setVerySlowCounts(new UncollectedPoint<>(timestamp, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT));
        } else {
            List<Integer> fastCounts = new ArrayList<>(dataPoints.size());
            List<Integer> normalCounts = new ArrayList<>(dataPoints.size());
            List<Integer> slowCounts = new ArrayList<>(dataPoints.size());
            List<Integer> verySlowCounts = new ArrayList<>(dataPoints.size());
            for (ActiveTraceBo activeTraceBo : dataPoints) {
                Map<SlotType, Integer> activeTraceCounts = activeTraceBo.getActiveTraceCounts();
                if (activeTraceCounts.get(SlotType.FAST) != ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT) {
                    fastCounts.add(activeTraceCounts.get(SlotType.FAST));
                }
                if (activeTraceCounts.get(SlotType.NORMAL) != ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT) {
                    normalCounts.add(activeTraceCounts.get(SlotType.NORMAL));
                }
                if (activeTraceCounts.get(SlotType.SLOW) != ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT) {
                    slowCounts.add(activeTraceCounts.get(SlotType.SLOW));
                }
                if (activeTraceCounts.get(SlotType.VERY_SLOW) != ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT) {
                    verySlowCounts.add(activeTraceCounts.get(SlotType.VERY_SLOW));
                }
            }
            sampledActiveTrace.setFastCounts(createSampledTitledPoint(schema.getFastSlot().getSlotName(), timestamp, fastCounts));
            sampledActiveTrace.setNormalCounts(createSampledTitledPoint(schema.getNormalSlot().getSlotName(), timestamp, normalCounts));
            sampledActiveTrace.setSlowCounts(createSampledTitledPoint(schema.getSlowSlot().getSlotName(), timestamp, slowCounts));
            sampledActiveTrace.setVerySlowCounts(createSampledTitledPoint(schema.getVerySlowSlot().getSlotName(), timestamp, verySlowCounts));
        }
        return sampledActiveTrace;
    }

    private Point<Long, Integer> createSampledTitledPoint(String title, long timestamp, List<Integer> values) {
        if (values.isEmpty()) {
            return new UncollectedPoint<>(timestamp, ActiveTraceBo.UNCOLLECTED_ACTIVE_TRACE_COUNT);
        } else {
            return new TitledPoint<>(
                    title,
                    timestamp,
                    INTEGER_DOWN_SAMPLER.sampleMin(values),
                    INTEGER_DOWN_SAMPLER.sampleMax(values),
                    INTEGER_DOWN_SAMPLER.sampleAvg(values, 1),
                    INTEGER_DOWN_SAMPLER.sampleSum(values));
        }
    }
}
