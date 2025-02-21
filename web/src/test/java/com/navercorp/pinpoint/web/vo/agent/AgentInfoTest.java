package com.navercorp.pinpoint.web.vo.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AgentInfoTest {

    @Test
    void setAgentName() {
        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setAgentName("agentName");

        Assertions.assertEquals("agentName", agentInfo.getAgentName());
    }

    @Test
    void setAgentName_emptyAgentName() {
        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setAgentName("", "agentId");

        Assertions.assertEquals("agentId", agentInfo.getAgentName());
    }

    @Test
    void setAgentName_agentName() {
        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setAgentName("agentName", "agentId");

        Assertions.assertEquals("agentName", agentInfo.getAgentName());
    }
}