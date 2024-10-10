package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import io.grpc.stub.ClientCallStreamObserver;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

class ActiveThreadCountStreamSocketTest {

    @Test
    void close_NPE() {
        GrpcStreamService grpcStreamService = mock(GrpcStreamService.class);
        ClientCallStreamObserver<PCmdActiveThreadCountRes> client = mock(ClientCallStreamObserver.class);

        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(1, 2, grpcStreamService);
        socket.beforeStart(client);
        socket.close(null);
    }

    @Test
    void close() {
        GrpcStreamService grpcStreamService = mock(GrpcStreamService.class);
        ClientCallStreamObserver<PCmdActiveThreadCountRes> client = mock(ClientCallStreamObserver.class);

        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(1, 2, grpcStreamService);
        socket.beforeStart(client);
        socket.close(new IOException("test"));
    }
}