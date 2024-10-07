package com.navercorp.pinpoint.grpc.stream;

import io.grpc.stub.ClientCallStreamObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClientCallStateStreamObserverTest {

    @Test
    void state() {
        ClientCallStreamObserver<String> clientCall = mock(ClientCallStreamObserver.class);
        ClientCallStateStreamObserver<String> adaptor = ClientCallStateStreamObserver.clientCall(clientCall);
        Assertions.assertTrue(adaptor.state().isRun());

        adaptor.onCompleted();
        Assertions.assertTrue(adaptor.isClosed());
    }

    @Test
    void onError() {
        ClientCallStreamObserver<String> clientCall = mock(ClientCallStreamObserver.class);
        ClientCallStateStreamObserver<String> adaptor = ClientCallStateStreamObserver.clientCall(clientCall);

        adaptor.onError(new RuntimeException("test"));
        adaptor.onError(new RuntimeException("test"));

        Assertions.assertTrue(adaptor.state().isError());
        Assertions.assertTrue(adaptor.state().isClosed());
        verify(clientCall).onError(any());
    }

    @Test
    void onCompleted() {
        ClientCallStreamObserver<String> clientCall = mock(ClientCallStreamObserver.class);
        ClientCallStateStreamObserver<String> adaptor = ClientCallStateStreamObserver.clientCall(clientCall);

        adaptor.onCompleted();
        adaptor.onCompleted();
        adaptor.onCompleted();

        Assertions.assertTrue(adaptor.state().isCompleted());
        Assertions.assertTrue(adaptor.state().isClosed());
        verify(clientCall).onCompleted();
    }
}