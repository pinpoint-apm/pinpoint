package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PingSessionTest {

    @Test
    void getServiceType() {
        Header header = mock(Header.class);
        when(header.getServiceType()).thenReturn((int) ServiceType.SPRING.getCode());
        PingSession session = new PingSession(1L, 0, header);

        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getHeader().getServiceType());
    }


    @Test
    void nextEventIdAllocator() {
        Header header = mock(Header.class);
        PingSession session = new PingSession(1L, 0, header);

        Assertions.assertEquals(1, session.nextEventIdAllocator());
        Assertions.assertEquals(2, session.nextEventIdAllocator());
    }

    @Test
    void ping() {
        Header header = mock(Header.class);
        PingSession session = new PingSession(1L, 0, header);

        Assertions.assertTrue(session.firstPing());
        Assertions.assertFalse(session.firstPing());
        Assertions.assertFalse(session.firstPing());
    }

}