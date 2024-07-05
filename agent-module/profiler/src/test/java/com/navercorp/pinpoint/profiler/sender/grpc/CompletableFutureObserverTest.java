package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.profiler.message.ResultResponse;
import com.navercorp.pinpoint.grpc.trace.PResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CompletableFutureObserverTest {
    @Test
    void testFuture_response_not_arrive() {
        CompletableFutureObserver<PResult, ResultResponse> observer = new CompletableFutureObserver<>(PResults::toResponse);
        observer.onCompleted();

        CompletableFuture<ResultResponse> future = observer.future();
        assertThrows(Exception.class, future::get);
    }

    @Test
    void testFuture_response_arrive() {
        CompletableFutureObserver<PResult, ResultResponse> observer = new CompletableFutureObserver<>(PResults::toResponse);
        PResult result = PResult.newBuilder()
                .setSuccess(true)
                .setMessage("hello")
                .build();
        observer.onNext(result);

        CompletableFuture<ResultResponse> future = observer.future();
        String message = future.join().getMessage();
        Assertions.assertEquals("hello", message);
    }


    @Test
    void testFuture_response_arrive_and_complete() {
        CompletableFutureObserver<PResult, ResultResponse> observer = new CompletableFutureObserver<>(PResults::toResponse);
        PResult result = PResult.newBuilder()
                .setSuccess(true)
                .setMessage("hello")
                .build();
        observer.onNext(result);
        observer.onCompleted();

        CompletableFuture<ResultResponse> future = observer.future();
        String message = future.join().getMessage();
        Assertions.assertEquals("hello", message);
    }
}