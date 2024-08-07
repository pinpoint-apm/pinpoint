package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.stub.ServerCallStreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerCallStreamTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mock
    ServerCallStreamObserver<GeneratedMessageV3> responseStream;
    @Mock
    ServerStreamDispatch<GeneratedMessageV3, GeneratedMessageV3> dispatch;

    @Test
    void onNextError_atomicity() {
        ServerCallStream<GeneratedMessageV3, GeneratedMessageV3> serverCallStream = new ServerCallStream<>(logger, 1, responseStream, dispatch, StreamCloseOnError.TRUE, Empty::getDefaultInstance);

        serverCallStream.onNextError(newError());
        verify(responseStream).onError(any());

        Assertions.assertEquals(ServerCallStream.HANDLE_ERROR_STATE_COMPLETED, serverCallStream.getHandleErrorState());


        reset(responseStream);
        serverCallStream.onNextError(newError());
        verify(responseStream, never()).onError(any());
    }

    @SuppressWarnings("unchecked")
    private void reset(ServerCallStreamObserver<?> responseStream) {
        Mockito.reset(responseStream);
    }

    @Test
    void onNextError_streamCancel() {
        when(responseStream.isCancelled()).thenReturn(true);

        ServerCallStream<GeneratedMessageV3, GeneratedMessageV3> serverCallStream = new ServerCallStream<>(logger, 1, responseStream, dispatch, StreamCloseOnError.TRUE, Empty::getDefaultInstance);

        serverCallStream.onNextError(newError());
        verify(responseStream).isCancelled();

        reset(responseStream);
        serverCallStream.onNextError(newError());
        verify(responseStream, never()).isCancelled();
    }

    @Test
    void onNextError_StreamCloseOnError() {
        ServerCallStream<GeneratedMessageV3, GeneratedMessageV3> serverCallStream = new ServerCallStream<>(logger, 1, responseStream, dispatch, StreamCloseOnError.FALSE, Empty::getDefaultInstance);

        serverCallStream.onNextError(newError());
        verify(responseStream, never()).isCancelled();

        serverCallStream.onNextError(newError());
        verify(responseStream, never()).isCancelled();
    }


    @Test
    void onError() {
        ServerCallStream<GeneratedMessageV3, GeneratedMessageV3> serverCallStream = new ServerCallStream<>(logger, 1, responseStream, dispatch, StreamCloseOnError.FALSE, Empty::getDefaultInstance);

        serverCallStream.onError(newError());
        verify(responseStream).onCompleted();

        reset(responseStream);
        when(responseStream.isCancelled()).thenReturn(true);

        serverCallStream.onError(newError());
        verify(responseStream, never()).onCompleted();

    }

    private RuntimeException newError() {
        return new RuntimeException("runtime error");
    }
}