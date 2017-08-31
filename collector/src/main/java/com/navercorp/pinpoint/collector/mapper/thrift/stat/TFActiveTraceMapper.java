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
package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTrace;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTraceHistogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class TFActiveTraceMapper {

    public TFActiveTrace map(ActiveTraceBo activeTraceBo) {
        TFActiveTraceHistogram tFActiveTraceHistogram = new TFActiveTraceHistogram();
        tFActiveTraceHistogram.setVersion(activeTraceBo.getVersion());
        tFActiveTraceHistogram.setHistogramSchemaType(activeTraceBo.getHistogramSchemaType());
        tFActiveTraceHistogram.setActiveTraceCount(createActiveTraceCount(activeTraceBo.getActiveTraceCounts()));

        TFActiveTrace tFActiveTrace = new TFActiveTrace();
        tFActiveTrace.setHistogram(tFActiveTraceHistogram);
        return tFActiveTrace;
    }

    private List<Integer> createActiveTraceCount(Map<SlotType, Integer> activeTraceCountMap) {
        if (activeTraceCountMap == null || activeTraceCountMap.size() == 0) {
            return Collections.emptyList();
        }

        List<Integer> activeTraceCountList = new ArrayList<>();
        activeTraceCountList.add(0, activeTraceCountMap.get(SlotType.FAST));
        activeTraceCountList.add(1, activeTraceCountMap.get(SlotType.NORMAL));
        activeTraceCountList.add(2, activeTraceCountMap.get(SlotType.SLOW));
        activeTraceCountList.add(3, activeTraceCountMap.get(SlotType.VERY_SLOW));
        return activeTraceCountList;
    }
}
