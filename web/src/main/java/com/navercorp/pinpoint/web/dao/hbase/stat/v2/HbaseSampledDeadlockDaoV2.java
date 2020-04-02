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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.DeadlockDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.web.dao.stat.SampledDeadlockDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DeadlockSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("sampledDeadlockDaoV2")
public class HbaseSampledDeadlockDaoV2 implements SampledDeadlockDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final DeadlockDecoder deadlockDecoder;
    private final DeadlockSampler deadlockSampler;

    public HbaseSampledDeadlockDaoV2(HbaseAgentStatDaoOperationsV2 operations, DeadlockDecoder deadlockDecoder, DeadlockSampler deadlockSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.deadlockDecoder = Objects.requireNonNull(deadlockDecoder, "deadlockDecoder");
        this.deadlockSampler = Objects.requireNonNull(deadlockSampler, "deadlockSampler");
    }

    @Override
    public List<SampledDeadlock> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        AgentStatMapperV2<DeadlockThreadCountBo> mapper = operations.createRowMapper(deadlockDecoder, range);

        SampledAgentStatResultExtractor<DeadlockThreadCountBo, SampledDeadlock> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, deadlockSampler);
        return operations.getSampledAgentStatList(AgentStatType.DEADLOCK, resultExtractor, agentId, range);
    }

}
