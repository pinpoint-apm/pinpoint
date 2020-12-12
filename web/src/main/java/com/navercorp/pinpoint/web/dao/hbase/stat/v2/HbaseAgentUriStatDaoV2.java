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
import com.navercorp.pinpoint.web.dao.stat.AgentUriStatDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Taejin Koo
 */
@Repository("agentUriStatDaoV2")
public class HbaseAgentUriStatDaoV2  implements AgentUriStatDao {

    @Autowired
    private AgentUriStatDecoder agentUriStatDecoder;

    @Autowired
    private HbaseAgentUriStatDaoOperationsV2 operations;

    @Override
    public List<AgentUriStatBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<AgentUriStatBo> mapper = operations.createRowMapper(agentUriStatDecoder, range);
        return operations.getAgentStatList(AgentStatType.URI, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<AgentUriStatBo> mapper = operations.createRowMapper(agentUriStatDecoder, range);
        return operations.agentStatExists(AgentStatType.URI, mapper, agentId, range);
    }

}
