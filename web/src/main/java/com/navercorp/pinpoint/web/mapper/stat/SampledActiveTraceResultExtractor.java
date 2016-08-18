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

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.chart.TitledPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class SampledActiveTraceResultExtractor extends SampledAgentStatResultExtractor<ActiveTraceBo, SampledActiveTrace> {

    private static final int UNCOLLECTED_COUNT = -1;
    public static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(UNCOLLECTED_COUNT);

    public SampledActiveTraceResultExtractor(TimeWindow timeWindow, AgentStatMapper<ActiveTraceBo> rowMapper) {
        super(timeWindow, rowMapper);
    }

    @Override
    protected SampledActiveTrace sampleCurrentBatch(long timestamp, List<ActiveTraceBo> dataPointsToSample) {
        HistogramSchema schema = BaseHistogramSchema.getDefaultHistogramSchemaByTypeCode(dataPointsToSample.get(0).getHistogramSchemaType());
        List<Integer> fastCounts = new ArrayList<>(dataPointsToSample.size());
        List<Integer> normalCounts = new ArrayList<>(dataPointsToSample.size());
        List<Integer> slowCounts = new ArrayList<>(dataPointsToSample.size());
        List<Integer> verySlowCounts = new ArrayList<>(dataPointsToSample.size());
        for (ActiveTraceBo activeTraceBo : dataPointsToSample) {
            Map<SlotType, Integer> activeTraceCounts = activeTraceBo.getActiveTraceCounts();
            fastCounts.add(activeTraceCounts.get(SlotType.FAST));
            normalCounts.add(activeTraceCounts.get(SlotType.NORMAL));
            slowCounts.add(activeTraceCounts.get(SlotType.SLOW));
            verySlowCounts.add(activeTraceCounts.get(SlotType.VERY_SLOW));
        }
        SampledActiveTrace sampledActiveTrace = new SampledActiveTrace();
        sampledActiveTrace.setFastCounts(createSampledTitledPoint(schema.getFastSlot().getSlotName(), timestamp, fastCounts));
        sampledActiveTrace.setNormalCounts(createSampledTitledPoint(schema.getNormalSlot().getSlotName(), timestamp, normalCounts));
        sampledActiveTrace.setSlowCounts(createSampledTitledPoint(schema.getSlowSlot().getSlotName(), timestamp, slowCounts));
        sampledActiveTrace.setVerySlowCounts(createSampledTitledPoint(schema.getVerySlowSlot().getSlotName(), timestamp, verySlowCounts));
        return sampledActiveTrace;
    }

    private TitledPoint<Long, Integer> createSampledTitledPoint(String title, long timestamp, List<Integer> values) {
        return new TitledPoint<>(
                title,
                timestamp,
                INTEGER_DOWN_SAMPLER.sampleMin(values),
                INTEGER_DOWN_SAMPLER.sampleMax(values),
                INTEGER_DOWN_SAMPLER.sampleAvg(values));
    }
}
