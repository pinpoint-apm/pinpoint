/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentUriStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.web.dao.stat.SampledAgentUriStatDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledUriStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentUriStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("sampledAgentUriStatDaoV2")
public class HbaseSampledAgentUriStatDaoV2 implements SampledAgentUriStatDao {

    private final HbaseAgentUriStatDaoOperationsV2 operations;

    private final AgentUriStatDecoder agentUriStatDecoder;

    private final AgentUriStatSampler agentUriStatSampler;

    public HbaseSampledAgentUriStatDaoV2(HbaseAgentUriStatDaoOperationsV2 operations, AgentUriStatDecoder agentUriStatDecoder, AgentUriStatSampler agentUriStatSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.agentUriStatDecoder = Objects.requireNonNull(agentUriStatDecoder, "agentUriStatDecoder");
        this.agentUriStatSampler = Objects.requireNonNull(agentUriStatSampler, "agentUriStatSampler");
    }

    @Override
    public List<SampledAgentUriStat> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);

        AgentStatMapperV2<AgentUriStatBo> mapper = operations.createRowMapper(agentUriStatDecoder, range);
        SampledUriStatResultExtractor resultExtractor = new SampledUriStatResultExtractor(timeWindow, mapper, agentUriStatSampler);

        List<SampledAgentUriStat> sampledAgentUriStatList = operations.getSampledAgentStatList(AgentStatType.URI, resultExtractor, agentId, range);
        return sampledAgentUriStatList;
    }

}
