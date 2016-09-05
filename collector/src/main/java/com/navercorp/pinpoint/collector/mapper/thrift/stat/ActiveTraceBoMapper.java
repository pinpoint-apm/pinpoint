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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@Component
public class ActiveTraceBoMapper implements ThriftBoMapper<ActiveTraceBo, TActiveTrace> {

    @Override
    public ActiveTraceBo map(TActiveTrace tActiveTrace) {
        TActiveTraceHistogram tActiveTraceHistogram = tActiveTrace.getHistogram();
        Map<SlotType, Integer> activeTraceCounts = createActiveTraceCountMap(tActiveTraceHistogram.getActiveTraceCount());
        ActiveTraceBo activeTraceBo = new ActiveTraceBo();
        activeTraceBo.setVersion(tActiveTraceHistogram.getVersion());
        activeTraceBo.setHistogramSchemaType(tActiveTraceHistogram.getHistogramSchemaType());
        activeTraceBo.setActiveTraceCounts(activeTraceCounts);
        return activeTraceBo;
    }

    private Map<SlotType, Integer> createActiveTraceCountMap(List<Integer> activeTraceCounts) {
        if (activeTraceCounts == null || activeTraceCounts.isEmpty()) {
            return Collections.emptyMap();
        } else {
            if (activeTraceCounts.size() != 4) {
                return Collections.emptyMap();
            } else {
                Map<SlotType, Integer> activeTraceCountMap = new HashMap<SlotType, Integer>();
                activeTraceCountMap.put(SlotType.FAST, activeTraceCounts.get(0));
                activeTraceCountMap.put(SlotType.NORMAL, activeTraceCounts.get(1));
                activeTraceCountMap.put(SlotType.SLOW, activeTraceCounts.get(2));
                activeTraceCountMap.put(SlotType.VERY_SLOW, activeTraceCounts.get(3));
                return activeTraceCountMap;
            }
        }
    }
}
