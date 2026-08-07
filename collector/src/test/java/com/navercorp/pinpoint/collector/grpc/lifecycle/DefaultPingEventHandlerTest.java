package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import com.navercorp.pinpoint.io.request.UidFetcherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultPingEventHandlerTest {

    private static final ServiceUid SERVICE_UID = ServiceUid.of(100);

    @Mock
    PingSessionRegistry pingSessionRegistry;
    @Mock
    LifecycleListener lifecycleListener;

    private static UidFetcherService fetcher(CompletableFuture<ServiceUid> future) {
        return () -> serviceName -> future;
    }

    private static Header newHeader() {
        return HeaderV1.simple("name", "agentId", "agentName", "applicationName", 1010, 1234567890L);
    }

    @Test
    void newPingSession_eagerlyInitializesServiceUid() {
        DefaultPingEventHandler handler = new DefaultPingEventHandler(pingSessionRegistry, lifecycleListener,
                fetcher(CompletableFuture.completedFuture(SERVICE_UID)));

        PingSession pingSession = handler.newPingSession(1L, newHeader(), null);

        assertThat(pingSession.getServiceUid()).isEqualTo(SERVICE_UID);
        verify(pingSessionRegistry).add(pingSession);
    }

    @Test
    void newPingSession_serviceNotFound_leavesServiceUidNull() {
        DefaultPingEventHandler handler = new DefaultPingEventHandler(pingSessionRegistry, lifecycleListener,
                fetcher(CompletableFuture.completedFuture(null)));

        PingSession pingSession = handler.newPingSession(1L, newHeader(), null);

        assertThat(pingSession.getServiceUid()).isNull();
        verify(pingSessionRegistry).add(pingSession);
    }

    @Test
    void newPingSession_delayedLookup_visibleOnCompletion() {
        CompletableFuture<ServiceUid> future = new CompletableFuture<>();
        DefaultPingEventHandler handler = new DefaultPingEventHandler(pingSessionRegistry, lifecycleListener, fetcher(future));

        PingSession pingSession = handler.newPingSession(1L, newHeader(), null);
        assertThat(pingSession.getServiceUid()).isNull();

        future.complete(SERVICE_UID);
        assertThat(pingSession.getServiceUid()).isEqualTo(SERVICE_UID);
    }

    @Test
    void newPingSession_failedLookup_doesNotThrow() {
        DefaultPingEventHandler handler = new DefaultPingEventHandler(pingSessionRegistry, lifecycleListener,
                fetcher(CompletableFuture.failedFuture(new RuntimeException("registry down"))));

        PingSession pingSession = handler.newPingSession(1L, newHeader(), null);

        assertThat(pingSession.getServiceUid()).isNull();
        verify(pingSessionRegistry).add(pingSession);
    }
}