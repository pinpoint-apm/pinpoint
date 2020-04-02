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

import com.navercorp.pinpoint.common.server.bo.codec.stat.CpuLoadDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.web.dao.stat.CpuLoadDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository("cpuLoadDaoV2")
public class HbaseCpuLoadDaoV2 implements CpuLoadDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final CpuLoadDecoder cpuLoadDecoder;

    public HbaseCpuLoadDaoV2(HbaseAgentStatDaoOperationsV2 operations, CpuLoadDecoder cpuLoadDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.cpuLoadDecoder = Objects.requireNonNull(cpuLoadDecoder, "cpuLoadDecoder");
    }

    @Override
    public List<CpuLoadBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<CpuLoadBo> mapper = operations.createRowMapper(cpuLoadDecoder, range);
        return operations.getAgentStatList(AgentStatType.CPU_LOAD, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<CpuLoadBo> mapper = operations.createRowMapper(cpuLoadDecoder, range);
        return operations.agentStatExists(AgentStatType.CPU_LOAD, mapper, agentId, range);
    }
}
