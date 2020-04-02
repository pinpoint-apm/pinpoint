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

import com.navercorp.pinpoint.common.server.bo.codec.stat.ResponseTimeDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.web.dao.stat.ResponseTimeDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Repository("responseTimeDaoV2")
public class HbaseResponseTimeDaoV2 implements ResponseTimeDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final ResponseTimeDecoder responseTimeDecoder;

    public HbaseResponseTimeDaoV2(HbaseAgentStatDaoOperationsV2 operations, ResponseTimeDecoder responseTimeDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.responseTimeDecoder = Objects.requireNonNull(responseTimeDecoder, "responseTimeDecoder");
    }

    @Override
    public List<ResponseTimeBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<ResponseTimeBo> mapper = operations.createRowMapper(responseTimeDecoder, range);
        return operations.getAgentStatList(AgentStatType.RESPONSE_TIME, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<ResponseTimeBo> mapper = operations.createRowMapper(responseTimeDecoder, range);
        return operations.agentStatExists(AgentStatType.RESPONSE_TIME, mapper, agentId, range);
    }

}
