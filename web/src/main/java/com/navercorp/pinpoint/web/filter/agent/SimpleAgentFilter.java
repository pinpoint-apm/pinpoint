package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public interface SimpleAgentFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean accept(String agentId);
}
