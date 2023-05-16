/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcAgentStatBatchMapper {


    private final GrpcAgentStatMapper mapper;

    public GrpcAgentStatBatchMapper(GrpcAgentStatMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public AgentStatBo map(final PAgentStatBatch agentStatBatch, final Header header) {
        if (agentStatBatch == null) {
            return null;
        }
        final String agentId = header.getAgentId();
        final long startTimestamp = header.getAgentStartTime();

        final AgentStatBo.Builder builder = new AgentStatBo.Builder(agentId, startTimestamp);
        for (PAgentStat agentStat : agentStatBatch.getAgentStatList()) {
            this.mapper.map(agentStat, builder);
        }
        return builder.build();
    }

}