package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import io.grpc.stub.ClientCallStreamObserver;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PinpointClientResponseObserverTest {

    @Test
    void isReady_true() {
        GrpcStreamService service = mock(GrpcStreamService.class);

        ClientCallStreamObserver<PCmdActiveThreadCountRes> requestStream = mock(ClientCallStreamObserver.class);
        when(requestStream.isReady()).thenReturn(true);

        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(1, 2, service);
        socket.beforeStart(requestStream);

        socket.send(PCmdActiveThreadCountRes.getDefaultInstance());
        verify(requestStream).onNext(PCmdActiveThreadCountRes.getDefaultInstance());
    }

    @Test
    void isReady_false() {
        GrpcStreamService service = mock(GrpcStreamService.class);

        ClientCallStreamObserver<PCmdActiveThreadCountRes> requestStream = mock(ClientCallStreamObserver.class);
        when(requestStream.isReady()).thenReturn(false);

        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(1, 2, service);
        socket.beforeStart(requestStream);

        socket.send(PCmdActiveThreadCountRes.getDefaultInstance());
        verify(requestStream, never()).onNext(PCmdActiveThreadCountRes.getDefaultInstance());
    }
}