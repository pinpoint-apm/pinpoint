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

import com.navercorp.pinpoint.common.server.bo.codec.stat.JvmGcDetailedDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDetailedDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository("jvmGcDetailedDaoV2")
public class HbaseJvmGcDetailedDaoV2 implements JvmGcDetailedDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final JvmGcDetailedDecoder jvmGcDetailedDecoder;

    public HbaseJvmGcDetailedDaoV2(HbaseAgentStatDaoOperationsV2 operations, JvmGcDetailedDecoder jvmGcDetailedDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.jvmGcDetailedDecoder = Objects.requireNonNull(jvmGcDetailedDecoder, "jvmGcDetailedDecoder");
    }

    @Override
    public List<JvmGcDetailedBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<JvmGcDetailedBo> mapper = operations.createRowMapper(jvmGcDetailedDecoder, range);
        return operations.getAgentStatList(AgentStatType.JVM_GC_DETAILED, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<JvmGcDetailedBo> mapper = operations.createRowMapper(jvmGcDetailedDecoder, range);
        return operations.agentStatExists(AgentStatType.JVM_GC_DETAILED, mapper, agentId, range);
    }
}