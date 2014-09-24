package com.nhn.pinpoint.web.service;

/**
 * @author emeroad
 */
public class AgentIdNotFoundException extends RuntimeException {

    private String agentId;
    private long startTime;

    public AgentIdNotFoundException(String agentId, long startTime) {
        super("agentId:" + agentId + " startTime:" + startTime + " not found");
        this.agentId = agentId;
        this.startTime = startTime;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTime() {
        return startTime;
    }

}
