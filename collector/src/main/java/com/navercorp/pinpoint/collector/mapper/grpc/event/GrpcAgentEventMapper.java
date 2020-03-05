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

package com.navercorp.pinpoint.collector.mapper.grpc.event;

import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PDeadlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Taejin Koo
 */
@Component
public class GrpcAgentEventMapper {

    @Autowired
    private GrpcDeadlockEventBoMapper deadlockEventBoMapper;

    public AgentEventBo map(final PAgentStat agentStat, final Header header) {
        final String agentId = header.getAgentId();
        final long startTimestamp = header.getAgentStartTime();
        final long timestamp = agentStat.getTimestamp();

        if (agentStat.hasDeadlock()) {
            final PDeadlock deadlock = agentStat.getDeadlock();
            if (CollectionUtils.hasLength(deadlock.getThreadDumpList())) {
                return deadlockEventBoMapper.map(agentId, startTimestamp, timestamp, deadlock);
            }
        }
        return null;
    }
}