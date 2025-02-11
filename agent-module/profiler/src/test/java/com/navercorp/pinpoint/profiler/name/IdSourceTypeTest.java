package com.navercorp.pinpoint.profiler.name;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IdSourceTypeTest {

    @Test
    void getAgentId() {
        IdSourceType idSourceType = IdSourceType.SYSTEM;

        Assertions.assertEquals("pinpoint.agentId", idSourceType.getAgentId());
        Assertions.assertEquals("pinpoint.agentName", idSourceType.getAgentName());
        Assertions.assertEquals("pinpoint.applicationName", idSourceType.getApplicationName());
        Assertions.assertEquals("pinpoint.serviceName", idSourceType.getServiceName());
    }
}