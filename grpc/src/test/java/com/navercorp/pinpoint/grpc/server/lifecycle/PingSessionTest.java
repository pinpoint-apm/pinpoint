package com.navercorp.pinpoint.grpc.server.lifecycle;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class PingSessionTest {

    @Test
    void getServiceType() {
        Header header = new Header("name", "agentId", "agentName", "appName",
                ServiceType.SPRING.getCode(),  11, 22, Collections.emptyList());
        PingSession session = new PingSession(1L, header);

        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());

        session.setServiceType(ServiceType.TEST.getCode());
        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());
    }

    @Test
    void getServiceType_undefined() {
        Header header = new Header("name", "agentId", "agentName", "appName",
                ServiceType.UNDEFINED.getCode(),  11, 22, Collections.emptyList());
        PingSession session = new PingSession(1L, header);

        Assertions.assertEquals(ServiceType.UNDEFINED.getCode(), session.getServiceType());

        session.setServiceType(ServiceType.SPRING.getCode());
        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());
    }

    @Test
    void nextEventIdAllocator() {
        Header header = new Header("name", "agentId", "agentName", "appName",
                ServiceType.SPRING.getCode(),  11, 22, Collections.emptyList());

        PingSession session = new PingSession(1L, header);

        Assertions.assertEquals(1, session.nextEventIdAllocator());
        Assertions.assertEquals(2, session.nextEventIdAllocator());

    }

}