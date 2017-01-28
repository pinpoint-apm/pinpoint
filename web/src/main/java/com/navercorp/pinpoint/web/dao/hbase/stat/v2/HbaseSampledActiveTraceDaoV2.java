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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.ActiveTraceDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.web.dao.stat.SampledActiveTraceDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ActiveTraceSampler;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Repository("sampledActiveTraceDaoV2")
public class HbaseSampledActiveTraceDaoV2 implements SampledActiveTraceDao {

    @Autowired
    private ActiveTraceDecoder activeTraceDecoder;

    @Autowired
    private ActiveTraceSampler activeTraceSampler;

    @Autowired
    private HbaseAgentStatDaoOperationsV2 operations;

    @Override
    public List<SampledActiveTrace> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        AgentStatMapperV2<ActiveTraceBo> mapper = operations.createRowMapper(activeTraceDecoder, range);
        SampledAgentStatResultExtractor<ActiveTraceBo, SampledActiveTrace> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, activeTraceSampler);
        return operations.getSampledAgentStatList(AgentStatType.ACTIVE_TRACE, resultExtractor, agentId, range);
    }
}
