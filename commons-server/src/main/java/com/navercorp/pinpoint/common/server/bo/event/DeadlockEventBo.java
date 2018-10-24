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

package com.navercorp.pinpoint.common.server.bo.event;

import com.navercorp.pinpoint.common.server.util.AgentEventType;

/**
 * @author Taejin Koo
 * @author jaehong.kim - Replace TDeadlock to DeadlockBo
 */
public class DeadlockEventBo extends AgentEventBo {

    private final DeadlockBo deadlockBo;

    public DeadlockEventBo(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType, DeadlockBo deadlockBo) {
        super(agentId, startTimestamp, eventTimestamp, eventType);
        this.deadlockBo = deadlockBo;
    }

    public DeadlockBo getDeadlockBo() {
        return deadlockBo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadlockEventBo{");
        sb.append("deadlockBo=").append(deadlockBo);
        sb.append('}');
        return sb.toString();
    }
}
