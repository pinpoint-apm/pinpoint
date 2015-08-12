package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public interface PreAgentFilter {
    boolean accept(String agentId);
}
