package com.nhn.hippo.web.service;

/**
 *
 */
public class AgentIdNotFoundException extends RuntimeException {
	private static final long serialVersionUID = -2017517623014042821L;
	private String agentId;
    private long startTime;

    public AgentIdNotFoundException(String agentId, long startTime) {
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
