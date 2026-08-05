package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.grpc.lifecycle.PingSession;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingSessionRegistry;
import com.navercorp.pinpoint.collector.service.ApplicationServiceTypeService;
import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import com.navercorp.pinpoint.io.request.UidFetcherService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KeepAliveServiceTest {

    private static final ServiceUid SERVICE_UID = ServiceUid.of(100);

    @Mock
    AgentEventAsyncTaskService agentEventAsyncTask;
    @Mock
    AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask;
    @Mock
    PingSessionRegistry pingSessionRegistry;
    @Mock
    ApplicationServiceTypeService applicationServiceTypeService;

    private KeepAliveService newKeepAliveService(UidFetcherService uidFetcherService) {
        return new KeepAliveService(agentEventAsyncTask, agentLifeCycleAsyncTask, pingSessionRegistry,
                applicationServiceTypeService, uidFetcherService);
    }

    private static UidFetcherService fetcher(CompletableFuture<ServiceUid> future) {
        return () -> serviceName -> future;
    }

    private static UidFetcherService countingFetcher(CompletableFuture<ServiceUid> future, AtomicInteger lookupCount) {
        return () -> serviceName -> {
            lookupCount.incrementAndGet();
            return future;
        };
    }

    private static PingSession newPingSession() {
        Header header = new HeaderV1("name", "agentId", "agentName", "applicationName",
                1010, 1234567890L, 1L, Collections.emptyList(), false, Collections.emptyMap());
        return new PingSession(1L, 1, header, null);
    }

    private static PingSession newResolvedPingSession(ServiceUid serviceUid) {
        PingSession pingSession = newPingSession();
        pingSession.updateServiceUid(serviceName -> CompletableFuture.completedFuture(serviceUid));
        return pingSession;
    }

    @Test
    void ping_unresolvedSession_resolvesAndWritesEvent() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(CompletableFuture.completedFuture(SERVICE_UID)));
        PingSession pingSession = newPingSession();

        keepAliveService.updateState(pingSession);

        ArgumentCaptor<AgentProperty> captor = ArgumentCaptor.forClass(AgentProperty.class);
        verify(agentLifeCycleAsyncTask).handlePingEvent(captor.capture(), anyLong());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE_UID);
        assertThat(pingSession.getServiceUid()).isEqualTo(SERVICE_UID);
    }

    @Test
    void ping_resolvedSession_skipsLookup() {
        AtomicInteger lookupCount = new AtomicInteger();
        KeepAliveService keepAliveService = newKeepAliveService(countingFetcher(CompletableFuture.completedFuture(SERVICE_UID), lookupCount));
        PingSession pingSession = newResolvedPingSession(SERVICE_UID);

        keepAliveService.updateState(pingSession);

        ArgumentCaptor<AgentProperty> captor = ArgumentCaptor.forClass(AgentProperty.class);
        verify(agentLifeCycleAsyncTask).handlePingEvent(captor.capture(), anyLong());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE_UID);
        assertThat(lookupCount.get()).isZero();
    }

    @Test
    void ping_unresolvedSession_serviceNotFound_skipsEvent() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(CompletableFuture.completedFuture(null)));
        PingSession pingSession = newPingSession();

        keepAliveService.updateState(pingSession);

        verifyNoInteractions(agentLifeCycleAsyncTask);
        assertThat(pingSession.getServiceUid()).isNull();
    }

    @Test
    void ping_unresolvedSession_pendingLookup_skipsEventAfterTimeout() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(new CompletableFuture<>()));
        PingSession pingSession = newPingSession();

        keepAliveService.updateState(pingSession);

        verifyNoInteractions(agentLifeCycleAsyncTask);
    }

    @Test
    void ping_unresolvedSession_failedLookup_skipsEvent() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(CompletableFuture.failedFuture(new RuntimeException("registry down"))));
        PingSession pingSession = newPingSession();

        keepAliveService.updateState(pingSession);

        verifyNoInteractions(agentLifeCycleAsyncTask);
        assertThat(pingSession.getServiceUid()).isNull();
    }

    @Test
    void ping_lookupTimedOut_resolvedOnNextPing() {
        CompletableFuture<ServiceUid> future = new CompletableFuture<>();
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(future));
        PingSession pingSession = newPingSession();

        // blocks up to the supplier timeout, then skips the event
        keepAliveService.updateState(pingSession);
        verifyNoInteractions(agentLifeCycleAsyncTask);

        future.complete(SERVICE_UID);

        // the completed lookup resolves instantly on the next ping
        keepAliveService.updateState(pingSession);

        ArgumentCaptor<AgentProperty> captor = ArgumentCaptor.forClass(AgentProperty.class);
        verify(agentLifeCycleAsyncTask).handlePingEvent(captor.capture(), anyLong());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE_UID);
        assertThat(pingSession.getServiceUid()).isEqualTo(SERVICE_UID);
    }

    @Test
    void close_resolvedSession_writesLifeCycleEvent() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(CompletableFuture.completedFuture(SERVICE_UID)));
        PingSession pingSession = newResolvedPingSession(SERVICE_UID);

        keepAliveService.updateState(pingSession, true, AgentLifeCycleState.UNEXPECTED_SHUTDOWN, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER);

        ArgumentCaptor<AgentProperty> captor = ArgumentCaptor.forClass(AgentProperty.class);
        verify(agentLifeCycleAsyncTask).handleLifeCycleEvent(captor.capture(), anyLong(),
                eq(AgentLifeCycleState.UNEXPECTED_SHUTDOWN), anyLong());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE_UID);
        verify(agentEventAsyncTask).handleEvent(captor.capture(), anyLong(),
                eq(AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER));
    }

    @Test
    void close_unresolvedSession_serviceNotFound_skipsEvent() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(CompletableFuture.completedFuture(null)));
        PingSession pingSession = newPingSession();

        keepAliveService.updateState(pingSession, true, AgentLifeCycleState.UNEXPECTED_SHUTDOWN, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER);

        verifyNoInteractions(agentLifeCycleAsyncTask);
        verifyNoInteractions(agentEventAsyncTask);
    }

    @Test
    void updateState_resolvedSession_neverBlocks() {
        KeepAliveService keepAliveService = newKeepAliveService(fetcher(new CompletableFuture<>()));
        PingSession pingSession = newResolvedPingSession(SERVICE_UID);

        long start = System.nanoTime();
        keepAliveService.updateState(pingSession);
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        verify(agentLifeCycleAsyncTask, times(1)).handlePingEvent(any(), anyLong());
        Assertions.assertTrue(elapsedMillis < 900, "updateState blocked on the uid lookup: " + elapsedMillis + "ms");
    }
}