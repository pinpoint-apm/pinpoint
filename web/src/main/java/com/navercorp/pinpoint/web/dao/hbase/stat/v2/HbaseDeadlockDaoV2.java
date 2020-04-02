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
import com.navercorp.pinpoint.web.dao.stat.DeadlockDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("deadlockDaoV2")
public class HbaseDeadlockDaoV2 implements DeadlockDao {

    private final DeadlockDecoder deadlockDecoder;

    private final HbaseAgentStatDaoOperationsV2 operations;

    public HbaseDeadlockDaoV2(HbaseAgentStatDaoOperationsV2 operations, DeadlockDecoder deadlockDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.deadlockDecoder = Objects.requireNonNull(deadlockDecoder, "deadlockDecoder");
    }

    @Override
    public List<DeadlockThreadCountBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<DeadlockThreadCountBo> mapper = operations.createRowMapper(deadlockDecoder, range);
        return operations.getAgentStatList(AgentStatType.DEADLOCK, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<DeadlockThreadCountBo> mapper = operations.createRowMapper(deadlockDecoder, range);
        return operations.agentStatExists(AgentStatType.DEADLOCK, mapper, agentId, range);
    }

}
