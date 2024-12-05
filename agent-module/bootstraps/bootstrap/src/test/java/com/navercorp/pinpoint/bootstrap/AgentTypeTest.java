package com.navercorp.pinpoint.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class AgentTypeTest {

    @Test
    void getAgentType() {
        AgentType agentType = AgentType.getAgentType(AgentType.DEFAULT_AGENT.name());
        Assertions.assertEquals(AgentType.DEFAULT_AGENT, agentType);
    }

    @Test
    void getAgentType_map() {
        Map<String, String> map = new HashMap<>();
        map.put(AgentType.AGENT_TYPE_KEY, AgentType.DEFAULT_AGENT.name());

        AgentType agentType = AgentType.of(map::get);
        Assertions.assertEquals(AgentType.DEFAULT_AGENT, agentType);
    }
}