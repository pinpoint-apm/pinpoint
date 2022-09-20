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
package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTrace;
import com.navercorp.pinpoint.thrift.dto.flink.TFActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.flink.TFAgentStat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class TFActiveTraceMapper implements FlinkStatMapper<ActiveTraceBo, TFAgentStat> {

    public TFActiveTrace map(ActiveTraceBo activeTraceBo) {
        TFActiveTraceHistogram tFActiveTraceHistogram = new TFActiveTraceHistogram();
        tFActiveTraceHistogram.setVersion(activeTraceBo.getVersion());
        tFActiveTraceHistogram.setHistogramSchemaType(activeTraceBo.getHistogramSchemaType());
        tFActiveTraceHistogram.setActiveTraceCount(createActiveTraceCount(activeTraceBo.getActiveTraceHistogram()));

        TFActiveTrace tFActiveTrace = new TFActiveTrace();
        tFActiveTrace.setHistogram(tFActiveTraceHistogram);
        return tFActiveTrace;
    }

    private List<Integer> createActiveTraceCount(ActiveTraceHistogram activeTraceCountMap) {
        if (activeTraceCountMap == null) {
            return Collections.emptyList();
        }

        List<Integer> activeTraceCountList = new ArrayList<>();
        activeTraceCountList.add(0, activeTraceCountMap.getFastCount());
        activeTraceCountList.add(1, activeTraceCountMap.getNormalCount());
        activeTraceCountList.add(2, activeTraceCountMap.getSlowCount());
        activeTraceCountList.add(3, activeTraceCountMap.getVerySlowCount());
        return activeTraceCountList;
    }

    @Override
    public void map(ActiveTraceBo activeTraceBo, TFAgentStat tfAgentStat) {
        tfAgentStat.setActiveTrace(map(activeTraceBo));
    }

    @Override
    public void build(TFAgentStatMapper.TFAgentStatBuilder builder) {
        AgentStatBo agentStat = builder.getAgentStat();
        List<ActiveTraceBo> activeTraceBoList = agentStat.getActiveTraceBos();
        builder.build(activeTraceBoList, this);
    }
}
