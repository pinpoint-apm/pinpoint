package com.navercorp.pinpoint.web.vo;

import java.io.Serializable;

public class AgentParam implements Serializable {
    private String agentId;

    private long timeStamp;

    public AgentParam(String agentId, long timeStamp) {
        this.agentId = agentId;
        this.timeStamp = timeStamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
