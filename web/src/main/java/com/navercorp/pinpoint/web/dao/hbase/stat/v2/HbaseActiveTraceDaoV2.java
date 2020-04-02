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
import com.navercorp.pinpoint.web.dao.stat.ActiveTraceDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository("activeTraceDaoV2")
public class HbaseActiveTraceDaoV2 implements ActiveTraceDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final ActiveTraceDecoder activeTraceDecoder;

    public HbaseActiveTraceDaoV2(HbaseAgentStatDaoOperationsV2 operations, ActiveTraceDecoder activeTraceDecoder) {
        this.activeTraceDecoder = Objects.requireNonNull(activeTraceDecoder, "activeTraceDecoder");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<ActiveTraceBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<ActiveTraceBo> mapper = operations.createRowMapper(activeTraceDecoder, range);
        return operations.getAgentStatList(AgentStatType.ACTIVE_TRACE, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<ActiveTraceBo> mapper = operations.createRowMapper(activeTraceDecoder, range);
        return operations.agentStatExists(AgentStatType.ACTIVE_TRACE, mapper, agentId, range);
    }
}
