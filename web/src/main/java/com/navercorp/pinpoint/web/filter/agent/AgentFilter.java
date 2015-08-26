package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public interface AgentFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean accept(String fromAgent, String toAgent);
    boolean acceptFrom(String fromAgent);
    boolean acceptTo(String toAgent);
}
