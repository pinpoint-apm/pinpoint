package com.navercorp.pinpoint.profiler.receiver.grpc;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

class ActiveThreadCountStreamSocketTest {

    @Test
    void close_NPE() {
        GrpcStreamService grpcStreamService = mock(GrpcStreamService.class);
        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(1, grpcStreamService);
        socket.close(null);
    }

    @Test
    void close() {
        GrpcStreamService grpcStreamService = mock(GrpcStreamService.class);
        ActiveThreadCountStreamSocket socket = new ActiveThreadCountStreamSocket(1, grpcStreamService);
        socket.close(new IOException("test"));
    }
}