package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.ClientCallStreamObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PinpointClientResponseObserverTest {

    @Test
    void isReady_true() {
        GrpcProfilerStreamSocket<String, Empty> socket = mock(GrpcProfilerStreamSocket.class);
        PinpointClientResponseObserver<String, Empty> responseObserver = new PinpointClientResponseObserver<>(socket);

        ClientCallStreamObserver<String> requestStream = mock(ClientCallStreamObserver.class);
        when(requestStream.isReady()).thenReturn(true);
        responseObserver.beforeStart(requestStream);

        Assertions.assertTrue(responseObserver.isReady());
    }

    @Test
    void isReady_false() {
        GrpcProfilerStreamSocket<String, Empty> socket = mock(GrpcProfilerStreamSocket.class);
        PinpointClientResponseObserver<String, Empty> responseObserver = new PinpointClientResponseObserver<>(socket);

        Assertions.assertFalse(responseObserver.isReady());

        ClientCallStreamObserver<String> requestStream = mock(ClientCallStreamObserver.class);
        responseObserver.beforeStart(requestStream);
        Assertions.assertFalse(responseObserver.isReady());
    }
}