package com.navercorp.pinpoint.grpc.server.lifecycle;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class PingSessionTest {

    @Test
    void getServiceType() {
        PingSession session = new PingSession(1L, "name", "agentId", 1234,
                ServiceType.SPRING.getCode(),  11, Collections.emptyMap());

        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());

        session.setServiceType(ServiceType.TEST.getCode());
        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());
    }

    @Test
    void getServiceType_undefined() {
        PingSession session = new PingSession(1L, "name", "agentId", 1234,
                ServiceType.UNDEFINED.getCode(),  11, Collections.emptyMap());

        Assertions.assertEquals(ServiceType.UNDEFINED.getCode(), session.getServiceType());

        session.setServiceType(ServiceType.SPRING.getCode());
        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());
    }

    @Test
    void nextEventIdAllocator() {


        PingSession session = new PingSession(1L, "name", "agentId", 1234,
                ServiceType.SPRING.getCode(),  11, Collections.emptyMap());

        Assertions.assertEquals(1, session.nextEventIdAllocator());
        Assertions.assertEquals(2, session.nextEventIdAllocator());

    }

}