package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.TransportMutableContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PingSessionTest {

    @Test
    void getServiceType() {
        Header header = mock(Header.class);
        when(header.getServiceType()).thenReturn((int) ServiceType.SPRING.getCode());
        PingSession session = new PingSession(1L, 0, header, null);

        Assertions.assertEquals(ServiceType.SPRING.getCode(), session.getServiceType());
    }

    @Test
    void getServiceTypeFromTransportContext() {
        Header header = mock(Header.class);
        when(header.getServiceType()).thenReturn(-1);
        TransportMutableContext context = new TransportMutableContext();
        PingSession session = new PingSession(1L, 0, header, context);

        Assertions.assertEquals(-1, session.getServiceType());
        context.setServiceType(1010);
        Assertions.assertEquals(1010, session.getServiceType());
    }

    @Test
    void setServiceType() {
        Header header = mock(Header.class);
        when(header.getServiceType()).thenReturn(-1);
        TransportMutableContext context = new TransportMutableContext();
        PingSession session = new PingSession(1L, 0, header, context);

        session.setServiceType(1010);
        Assertions.assertEquals(1010, session.getServiceType());
        Assertions.assertEquals(1010, context.getServiceType());
    }

    @Test
    void nextEventIdAllocator() {
        Header header = mock(Header.class);
        PingSession session = new PingSession(1L, 0, header, null);

        Assertions.assertEquals(1, session.nextEventIdAllocator());
        Assertions.assertEquals(2, session.nextEventIdAllocator());
    }

    @Test
    void ping() {
        Header header = mock(Header.class);
        PingSession session = new PingSession(1L, 0, header, null);

        Assertions.assertTrue(session.firstPing());
        Assertions.assertFalse(session.firstPing());
        Assertions.assertFalse(session.firstPing());
    }

    @Test
    void updateServiceUid() {
        PingSession session = new PingSession(1L, 0, mock(Header.class), null);
        Assertions.assertNull(session.getServiceUid());

        session.updateServiceUid(serviceName -> CompletableFuture.completedFuture(ServiceUid.of(100)));

        Assertions.assertEquals(ServiceUid.of(100), session.getServiceUid());
    }

    @Test
    void updateServiceUid_notFound() {
        PingSession session = new PingSession(1L, 0, mock(Header.class), null);

        session.updateServiceUid(serviceName -> CompletableFuture.completedFuture(null));

        Assertions.assertNull(session.getServiceUid());
    }

    @Test
    void updateServiceUid_notFound_laterRegistered() {
        PingSession session = new PingSession(1L, 0, mock(Header.class), null);
        session.updateServiceUid(serviceName -> CompletableFuture.completedFuture(null));

        session.updateServiceUid(serviceName -> CompletableFuture.completedFuture(ServiceUid.of(100)));

        Assertions.assertEquals(ServiceUid.of(100), session.getServiceUid());
    }

    @Test
    void updateServiceUid_failedLookup() {
        PingSession session = new PingSession(1L, 0, mock(Header.class), null);

        session.updateServiceUid(serviceName -> CompletableFuture.failedFuture(new RuntimeException("registry down")));
        session.updateServiceUid(serviceName -> {
            throw new RuntimeException("fetcher broken");
        });

        Assertions.assertNull(session.getServiceUid());
    }

    @Test
    void updateServiceUid_pendingLookup_visibleOnCompletion() {
        PingSession session = new PingSession(1L, 0, mock(Header.class), null);
        CompletableFuture<ServiceUid> future = new CompletableFuture<>();

        session.updateServiceUid(serviceName -> future);
        Assertions.assertNull(session.getServiceUid());

        future.complete(ServiceUid.of(100));
        Assertions.assertEquals(ServiceUid.of(100), session.getServiceUid());
    }

}