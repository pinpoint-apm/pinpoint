package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.stub.ClientCallStreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ResponseStreamObserverTest {

    @Test
    void beforeStart_onReady_memoize() {
        StreamEventListener<String> listener = mock(StreamEventListener.class);
        ClientCallStreamObserver<String> request = mock(ClientCallStreamObserver.class);

        ResponseStreamObserver<String, String> observer = new ResponseStreamObserver<>(listener);

        observer.beforeStart(request);

        ArgumentCaptor<Runnable> argumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(request).setOnReadyHandler(argumentCaptor.capture());

        Runnable value = argumentCaptor.getValue();
        value.run();
        value.run();
        value.run();

        Mockito.verify(listener, times(1)).start(Mockito.any());
    }
}