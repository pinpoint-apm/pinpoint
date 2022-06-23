package com.navercorp.pinpoint.web.vo;

public interface AgentInfoFilter {
    boolean ACCEPT = true;
    boolean REJECT = false;

    boolean filter(AgentInfo agentInfo);

    static boolean accept(AgentInfo agentInfo) {
        return ACCEPT;
    }

    static boolean reject(AgentInfo agentInfo) {
        return REJECT;
    }

}
