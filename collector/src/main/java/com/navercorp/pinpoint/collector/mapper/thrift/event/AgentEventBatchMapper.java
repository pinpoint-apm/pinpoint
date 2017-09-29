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

package com.navercorp.pinpoint.collector.mapper.thrift.event;

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class AgentEventBatchMapper implements ThriftBoMapper<List<AgentEventBo>, TAgentStatBatch> {

    @Autowired
    DeadlockEventBoMapper deadlockEventBoMapper;

    @Override
    public List<AgentEventBo> map(TAgentStatBatch tAgentStatBatch) {
        if (tAgentStatBatch == null) {
            return Collections.emptyList();
        }

        final String agentId = tAgentStatBatch.getAgentId();
        final long startTimestamp = tAgentStatBatch.getStartTimestamp();

        int agentStatsSize = tAgentStatBatch.getAgentStatsSize();
        List<AgentEventBo> agentEventBoList = new ArrayList<>(agentStatsSize);

        for (TAgentStat tAgentStat : tAgentStatBatch.getAgentStats()) {
            final long timestamp = tAgentStat.getTimestamp();
            if (tAgentStat.isSetDeadlock()) {
                TDeadlock deadlock = tAgentStat.getDeadlock();
                if (deadlock != null && deadlock.isSetDeadlockedThreadList()) {
                    agentEventBoList.add(deadlockEventBoMapper.map(agentId, startTimestamp, timestamp, deadlock));
                }
            }
        }
        return agentEventBoList;
    }

}
