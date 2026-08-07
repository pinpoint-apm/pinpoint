package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.grpc.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.collector.grpc.lifecycle.PingSession;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderV1;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.grpc.trace.PResult;
import io.grpc.Context;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    private static final long TRANSPORT_ID = 10L;

    @Mock
    RequestResponseHandler<PAgentInfo, PResult> handler;
    @Mock
    PingEventHandler pingEventHandler;
    @Mock
    ServerRequestFactory serverRequestFactory;
    @Mock
    ServerResponseFactory serverResponseFactory;
    @Mock
    TransportMetadata transportMetadata;
    @Mock
    ServerCallStreamObserver<PPing> responseObserver;

    private final Header header = HeaderV1.simple("cho-test", "agentId", "agentName", "applicationName", 0, 1234567890L);
    private final PingSession pingSession = new PingSession(TRANSPORT_ID, 1, header, null);

    private AgentService agentService;
    private Context grpcContext;

    @BeforeEach
    void setUp() {
        agentService = new AgentService(handler, pingEventHandler, Runnable::run, serverRequestFactory, serverResponseFactory);

        when(pingEventHandler.newPingSession(eq(TRANSPORT_ID), eq(header), any())).thenReturn(pingSession);
        when(transportMetadata.getTransportId()).thenReturn(TRANSPORT_ID);

        grpcContext = Context.ROOT
                .withValue(ServerContext.getAgentInfoKey(), header)
                .withValue(ServerContext.getTransportMetadataKey(), transportMetadata);
    }

    private StreamObserver<PPing> openPingStream() {
        AtomicReference<StreamObserver<PPing>> ref = new AtomicReference<>();
        grpcContext.run(() -> ref.set(agentService.pingSession(responseObserver)));
        return ref.get();
    }

    @Test
    void pingSession_sessionCreatedOnStreamOpen() {
        when(responseObserver.isReady()).thenReturn(true);
        StreamObserver<PPing> pingStream = openPingStream();

        // the serviceUid is resolved where it is used, so opening the stream never rejects
        verify(pingEventHandler).newPingSession(eq(TRANSPORT_ID), eq(header), any());
        verify(responseObserver).setOnCancelHandler(any());
        verify(responseObserver, never()).onError(any());

        pingStream.onNext(PPing.getDefaultInstance());

        verify(pingEventHandler).ping(pingSession);
        verify(responseObserver).onNext(any(PPing.class));
    }

    @Test
    void pingSession_sessionReusedOnEveryPing() {
        when(responseObserver.isReady()).thenReturn(true);
        StreamObserver<PPing> pingStream = openPingStream();

        pingStream.onNext(PPing.getDefaultInstance());
        pingStream.onNext(PPing.getDefaultInstance());

        verify(pingEventHandler).newPingSession(eq(TRANSPORT_ID), eq(header), any());
        verify(pingEventHandler, times(2)).ping(pingSession);
        verify(responseObserver, times(2)).onNext(any(PPing.class));
    }

    @Test
    void pingSession_streamNotReady_pingNotAnswered() {
        when(responseObserver.isReady()).thenReturn(false);

        StreamObserver<PPing> pingStream = openPingStream();
        pingStream.onNext(PPing.getDefaultInstance());

        verify(pingEventHandler).ping(pingSession);
        verify(responseObserver, never()).onNext(any(PPing.class));
    }

    @Test
    void pingSession_cancelClosesSession() {
        openPingStream();

        ArgumentCaptor<Runnable> cancelHandler = ArgumentCaptor.forClass(Runnable.class);
        verify(responseObserver).setOnCancelHandler(cancelHandler.capture());
        cancelHandler.getValue().run();

        verify(pingEventHandler).close(pingSession);
    }

    @Test
    void pingSession_completeClosesSession() {
        StreamObserver<PPing> pingStream = openPingStream();

        pingStream.onCompleted();

        verify(responseObserver).onCompleted();
        verify(pingEventHandler).close(pingSession);
    }

    @Test
    void pingSession_errorClosesSession() {
        StreamObserver<PPing> pingStream = openPingStream();

        pingStream.onError(new RuntimeException("broken stream"));

        verify(pingEventHandler).close(pingSession);
    }
}