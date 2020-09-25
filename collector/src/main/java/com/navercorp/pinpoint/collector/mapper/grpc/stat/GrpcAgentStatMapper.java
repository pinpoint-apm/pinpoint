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
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GrpcAgentStatMapper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GrpcStatMapper[] mappers;

    public GrpcAgentStatMapper(GrpcStatMapper[] mappers) {
        this.mappers = Objects.requireNonNull(mappers, "mappers");
        for (GrpcStatMapper mapper : mappers) {
            logger.info("GrpcStatMapper:{}", mapper.getClass().getSimpleName());
        }
    }

    public AgentStatBo map(PAgentStat agentStat) {
        if (agentStat == null) {
            return null;
        }

        final Header agentInfo = ServerContext.getAgentInfo();
        final String agentId = agentInfo.getAgentId();
        final long startTimestamp = agentInfo.getAgentStartTime();


        final AgentStatBo.Builder builder = AgentStatBo.newBuilder(agentId, startTimestamp);

        this.map(agentStat, builder);

        return builder.build();
    }

    void map(PAgentStat agentStat, AgentStatBo.Builder builder) {
        final long timestamp = agentStat.getTimestamp();
        AgentStatBo.Builder.StatBuilder statBuilder = builder.newStatBuilder(timestamp);
        for (GrpcStatMapper mapper : mappers) {
            mapper.map(statBuilder, agentStat);
        }
    }

}