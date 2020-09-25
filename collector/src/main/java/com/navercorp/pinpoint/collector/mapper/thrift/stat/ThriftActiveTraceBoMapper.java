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

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class ThriftActiveTraceBoMapper implements ThriftStatMapper<ActiveTraceBo, TActiveTrace> {

    @Override
    public ActiveTraceBo map(TActiveTrace tActiveTrace) {
        TActiveTraceHistogram tActiveTraceHistogram = tActiveTrace.getHistogram();
        ActiveTraceHistogram activeTraceHistogram = createActiveTraceCountMap(tActiveTraceHistogram.getActiveTraceCount());
        ActiveTraceBo activeTraceBo = new ActiveTraceBo();
        activeTraceBo.setVersion(tActiveTraceHistogram.getVersion());
        activeTraceBo.setHistogramSchemaType(tActiveTraceHistogram.getHistogramSchemaType());
        activeTraceBo.setActiveTraceHistogram(activeTraceHistogram);
        return activeTraceBo;
    }

    @Override
    public void map(AgentStatBo.Builder.StatBuilder agentStatBuilder, TAgentStat tAgentStat) {
        // activeTrace
        if (tAgentStat.isSetActiveTrace() && tAgentStat.getActiveTrace().isSetHistogram()) {
            ActiveTraceBo activeTraceBo = this.map(tAgentStat.getActiveTrace());
            agentStatBuilder.addActiveTrace(activeTraceBo);
        }
    }

    private ActiveTraceHistogram createActiveTraceCountMap(List<Integer> activeTraceCounts) {
        if (CollectionUtils.isEmpty(activeTraceCounts)) {
            return ActiveTraceHistogram.UNCOLLECTED;
        }
        if (activeTraceCounts.size() != 4) {
            return ActiveTraceHistogram.UNCOLLECTED;
        }

        final int fast = activeTraceCounts.get(0);
        final int normal = activeTraceCounts.get(1);
        final int slow = activeTraceCounts.get(2);
        final int verySlow = activeTraceCounts.get(3);

        return new ActiveTraceHistogram(fast, normal, slow, verySlow);
    }
}
