package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.grpc.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.mockito.Mockito.mock;

class DefaultPingSessionRegistryTest {

    @Test
    void getEmpty() {
        Long transportId = 1L;
        PingSessionRegistry registry = new DefaultPingSessionRegistry();
        PingSession pingSession = registry.get(transportId);
        Assertions.assertNull(pingSession);
    }

    @Test
    void limit() {
        Long transportId = 1L;

        PingSession pingSession0 = new PingSession(transportId, 0, mock(Header.class));
        PingSession pingSession1 = new PingSession(transportId, 1, mock(Header.class));
        PingSession pingSession2 = new PingSession(transportId, 2, mock(Header.class));

        PingSessionRegistry registry = new DefaultPingSessionRegistry(2);
        registry.add(pingSession0);
        registry.add(pingSession1);
        registry.add(pingSession2);

        Assertions.assertEquals(2, registry.size());

        registry.remove(pingSession1);
        registry.remove(pingSession2);


        Assertions.assertEquals(0, registry.size());
    }


    @Test
    void add() {
        PingSessionRegistry registry = new DefaultPingSessionRegistry();

        Header header = mock(Header.class);
        PingSession pingSession = new PingSession(1L, 0, header);

        Long transportId = 1L;

        registry.add(pingSession);
        PingSession session = registry.get(transportId);

        Assertions.assertSame(pingSession, session);

        registry.remove(pingSession);

        Assertions.assertNull(registry.get(transportId));
    }


    @Test
    void values() {
        PingSessionRegistry registry = new DefaultPingSessionRegistry();

        Long transportId = 1L;
        Header header = mock(Header.class);
        PingSession pingSession0 = new PingSession(transportId, 0, header);
        PingSession pingSession1 = new PingSession(transportId, 1, header);

        PingSession pingSession2 = new PingSession(++transportId, 1, header);

        registry.add(pingSession0);
        registry.add(pingSession1);
        registry.add(pingSession2);

        Collection<PingSession> values = registry.values();

        Assertions.assertEquals(2, values.size());
    }
}