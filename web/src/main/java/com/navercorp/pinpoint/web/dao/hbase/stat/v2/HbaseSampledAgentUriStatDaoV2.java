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

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.SampledAgentUriStatDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledUriStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("sampledAgentUriStatDaoV2")
public class HbaseSampledAgentUriStatDaoV2 implements SampledAgentUriStatDao {

    private final AgentStatType statType = AgentStatType.URI;
    private final HbaseAgentUriStatDaoOperationsV2 operations;

    private final AgentStatDecoder<AgentUriStatBo> agentUriStatDecoder;
    private final AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> agentUriStatSampler;

    public HbaseSampledAgentUriStatDaoV2(HbaseAgentUriStatDaoOperationsV2 operations,
                                         AgentStatDecoder<AgentUriStatBo> agentUriStatDecoder,
                                         AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> agentUriStatSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.agentUriStatDecoder = Objects.requireNonNull(agentUriStatDecoder, "agentUriStatDecoder");
        this.agentUriStatSampler = Objects.requireNonNull(agentUriStatSampler, "agentUriStatSampler");
    }

    @Override
    public List<SampledAgentUriStat> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        AgentStatMapperV2<AgentUriStatBo> mapper = operations.createRowMapper(agentUriStatDecoder, range);
        ResultsExtractor<List<SampledAgentUriStat>> resultExtractor = getResultExtractor(timeWindow, mapper);
        return operations.getSampledAgentStatList(statType, resultExtractor, agentId, range);
    }

    private ResultsExtractor<List<SampledAgentUriStat>> getResultExtractor(TimeWindow timeWindow, AgentStatMapperV2<AgentUriStatBo> mapper) {
        return new SampledUriStatResultExtractor(timeWindow, mapper, agentUriStatSampler);
    }

}
