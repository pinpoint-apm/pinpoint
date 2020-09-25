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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class ThriftAgentStatBatchMapper {

    private final ThriftAgentStatMapper statMapper;

    public ThriftAgentStatBatchMapper(ThriftAgentStatMapper statMapper) {
        this.statMapper = Objects.requireNonNull(statMapper, "statMapper");
    }

    public AgentStatBo map(TAgentStatBatch tAgentStatBatch) {
        if (!tAgentStatBatch.isSetAgentStats()) {
            return null;
        }


        final String agentId = tAgentStatBatch.getAgentId();

        final long startTimestamp = tAgentStatBatch.getStartTimestamp();
        AgentStatBo.Builder builder = AgentStatBo.newBuilder(agentId, startTimestamp);

        for (TAgentStat tAgentStat : tAgentStatBatch.getAgentStats()) {
            this.statMapper.map(builder, tAgentStat);
        }

        return builder.build();
    }

}
