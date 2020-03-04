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

import com.navercorp.pinpoint.common.server.bo.event.DeadlockBo;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.grpc.trace.PDeadlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Taejin Koo
 * @author jaehong.kim - Add DeadlockBoMapper
 */
@Component
public class GrpcDeadlockEventBoMapper {

    @Autowired
    private GrpcDeadlockBoMapper deadlockBoMapper;

    public DeadlockEventBo map(String agentId, long startTimeStamp, long eventTimestamp, PDeadlock deadlock) {
        final DeadlockBo deadlockBo = this.deadlockBoMapper.map(deadlock);
        return new DeadlockEventBo(agentId, startTimeStamp, eventTimestamp, AgentEventType.AGENT_DEADLOCK_DETECTED, deadlockBo);
    }
}