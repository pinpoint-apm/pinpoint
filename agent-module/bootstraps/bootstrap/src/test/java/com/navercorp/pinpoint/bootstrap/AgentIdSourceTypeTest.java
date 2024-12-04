package com.navercorp.pinpoint.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AgentIdSourceTypeTest {

    @Test
    void agentId() {
        Assertions.assertNotNull(AgentIdSourceType.AGENT_ARGUMENT.getAgentId());
    }
}