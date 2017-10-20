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
import com.navercorp.pinpoint.thrift.dto.TDeadlock;

/**
 * @author Taejin Koo
 */
public class DeadlockEventBo extends AgentEventBo {

    private final TDeadlock deadlock;

    public DeadlockEventBo(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType, TDeadlock deadlock) {
        super(agentId, startTimestamp, eventTimestamp, eventType);
        this.deadlock = deadlock;
    }

    public TDeadlock getDeadlock() {
        return deadlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DeadlockEventBo that = (DeadlockEventBo) o;

        return deadlock != null ? deadlock.equals(that.deadlock) : that.deadlock == null;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getAgentId() == null) ? 0 : getAgentId().hashCode());
        result = prime * result + (int)(getEventTimestamp() ^ (getEventTimestamp() >>> 32));
        result = prime * result + ((getEventType() == null) ? 0 : getEventType().hashCode());
        result = prime * result + (int)(getStartTimestamp() ^ (getStartTimestamp() >>> 32));
        result = prime * result + getVersion();
        result = prime * result + (deadlock != null ? deadlock.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentEventBo{");
        sb.append("version=").append(this.getVersion());
        sb.append(", agentId='").append(this.getAgentId()).append('\'');
        sb.append(", startTimestamp=").append(this.getStartTimestamp());
        sb.append(", eventTimestamp=").append(this.getEventTimestamp());
        sb.append(", eventType='").append(this.getEventType().getDesc()).append('\'');
        sb.append(", deadlock=").append(deadlock).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
