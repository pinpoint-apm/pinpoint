package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.stub.StreamObserver;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CompletableFutureObserver<T, R> implements StreamObserver<T> {
    private final CompletableFuture<R> future = new CompletableFuture<>();
    private final Function<T, R> converter;

    CompletableFutureObserver(Function<T, R> converter) {
        this.converter = Objects.requireNonNull(converter, "converter");
    }

    @Override
    public void onNext(T value) {
        R response = converter.apply(value);
        this.future.complete(response);
    }

    @Override
    public void onError(Throwable throwable) {
        this.future.completeExceptionally(throwable);
    }

    @Override
    public void onCompleted() {
        final CompletableFuture<R> future = this.future;
        if (!future.isDone()) {
            future.completeExceptionally(new Exception("Response is not arrived"));
        }
    }

    public CompletableFuture<R> future() {
        return future;
    }
}
