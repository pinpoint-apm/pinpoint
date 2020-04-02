/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.codec.stat.DirectBufferDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.web.dao.stat.DirectBufferDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Repository("directBufferDaoV2")
public class HbaseDirectBufferDaoV2 implements DirectBufferDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final DirectBufferDecoder directBufferDecoder;

    public HbaseDirectBufferDaoV2(HbaseAgentStatDaoOperationsV2 operations, DirectBufferDecoder directBufferDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.directBufferDecoder = Objects.requireNonNull(directBufferDecoder, "directBufferDecoder");
    }

    @Override
    public List<DirectBufferBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<DirectBufferBo> mapper = operations.createRowMapper(directBufferDecoder, range);
        return operations.getAgentStatList(AgentStatType.DIRECT_BUFFER, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<DirectBufferBo> mapper = operations.createRowMapper(directBufferDecoder, range);
        return operations.agentStatExists(AgentStatType.DIRECT_BUFFER, mapper, agentId, range);
    }

}
