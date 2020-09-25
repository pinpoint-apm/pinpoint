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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class ThriftAgentStatMapper {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ThriftStatMapper<?, ?>[] mappers;

    public ThriftAgentStatMapper(ThriftStatMapper<?, ?>[] mappers) {
        this.mappers = Objects.requireNonNull(mappers, "mappers");
        for (ThriftStatMapper<?, ?> mapper : mappers) {
            logger.info("ThriftStatMapper:{}", mapper.getClass().getSimpleName());
        }
    }

    public AgentStatBo map(TAgentStat tAgentStat) {
        if (tAgentStat == null) {
            return null;
        }
        final String agentId = tAgentStat.getAgentId();
        final long startTimestamp = tAgentStat.getStartTimestamp();

        AgentStatBo.Builder builder = AgentStatBo.newBuilder(agentId, startTimestamp);
        this.map(builder, tAgentStat);
        return builder.build();
    }

    void map(AgentStatBo.Builder builder, TAgentStat tAgentStat) {
        final long timestamp = tAgentStat.getTimestamp();
        final AgentStatBo.Builder.StatBuilder agentStatBuilder = builder.newStatBuilder(timestamp);
        for (ThriftStatMapper<?, ?> mapper : mappers) {
            mapper.map(agentStatBuilder, tAgentStat);
        }
    }

}