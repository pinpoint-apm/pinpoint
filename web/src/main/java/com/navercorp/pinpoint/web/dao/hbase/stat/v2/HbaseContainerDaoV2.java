/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.codec.stat.ContainerDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.ContainerBo;
import com.navercorp.pinpoint.web.dao.stat.ContainerDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository("containerDaoV2")
public class HbaseContainerDaoV2 implements ContainerDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final ContainerDecoder containerDecoder;

    public HbaseContainerDaoV2(HbaseAgentStatDaoOperationsV2 operations, ContainerDecoder containerDecoder){
        this.operations = Objects.requireNonNull(operations, "operations");
        this.containerDecoder = Objects.requireNonNull(containerDecoder, "containerDecoder");
    }

    @Override
    public List<ContainerBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<ContainerBo> mapper = operations.createRowMapper(containerDecoder, range);
        return operations.getAgentStatList(AgentStatType.CONTAINER, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<ContainerBo> mapper = operations.createRowMapper(containerDecoder, range);
        return operations.agentStatExists(AgentStatType.CONTAINER, mapper, agentId, range);
    }
}
