package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.grpc.Header;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.mockito.Mockito.mock;

class DefaultPingSessionRegistryTest {

    private PingSession newPingSession(Long transportId, long sessionId) {
        return new PingSession(transportId, sessionId, mock(Header.class), null);
    }

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

        PingSession pingSession0 = newPingSession(transportId, 0);
        PingSession pingSession1 = newPingSession(transportId, 1);
        PingSession pingSession2 = newPingSession(transportId, 2);

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

        PingSession pingSession = newPingSession(1L, 0);

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
        PingSession pingSession0 = newPingSession(transportId, 0);
        PingSession pingSession1 = newPingSession(transportId, 1);

        PingSession pingSession2 = newPingSession(++transportId, 1);

        registry.add(pingSession0);
        registry.add(pingSession1);
        registry.add(pingSession2);

        Collection<PingSession> values = registry.values();

        Assertions.assertEquals(2, values.size());
    }
}