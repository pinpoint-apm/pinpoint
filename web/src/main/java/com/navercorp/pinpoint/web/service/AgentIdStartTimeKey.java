package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.Objects;

public class AgentIdStartTimeKey {
    private final String agentId;
    private final long agentStartTime;

    public AgentIdStartTimeKey(String agentId, long agentStartTime) {
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
    }

    public AgentIdStartTimeKey(AgentId agentId, long agentStartTime) {
        this(agentId.value(), agentStartTime);
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentIdStartTimeKey that = (AgentIdStartTimeKey) o;
        return agentStartTime == that.agentStartTime && Objects.equals(agentId, that.agentId);
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        return result;
    }

    public static AgentIdStartTimeKey toAgentStartTimeKey(SpanBo spanBo) {
        return new AgentIdStartTimeKey(spanBo.getAgentId(), spanBo.getAgentStartTime());
    }

}
