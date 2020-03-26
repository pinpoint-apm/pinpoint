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

import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Taejin Koo
 * @author jaehong.kim - Add DeadlockBoMapper
 */
@Component
public class ThriftDeadlockEventBoMapper implements AgentEventBoMapper<DeadlockEventBo, TDeadlock> {

    private final ThriftDeadlockBoMapper deadlockBoMapper;

    public ThriftDeadlockEventBoMapper(ThriftDeadlockBoMapper deadlockBoMapper) {
        this.deadlockBoMapper = Objects.requireNonNull(deadlockBoMapper, "deadlockBoMapper");
    }

    @Override
    public DeadlockEventBo map(String agentId, long startTimeStamp, long eventTimestamp, TDeadlock tDeadlock) {
        final DeadlockBo deadlockBo = this.deadlockBoMapper.map(tDeadlock);
        return new DeadlockEventBo(agentId, startTimeStamp, eventTimestamp, AgentEventType.AGENT_DEADLOCK_DETECTED, deadlockBo);
    }
}