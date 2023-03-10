package com.navercorp.pinpoint.pinot.mybatis;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class TransformFuture<T, R> implements Future<R> {
    private final Future<T> delegate;
    private final Function<T, R> transform;

    public TransformFuture(Future<T> delegate, Function<T, R> transform) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.transform = Objects.requireNonNull(transform, "transform");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.delegate.isDone();
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        T value = this.delegate.get();
        return transform(value);
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        T value = this.delegate.get(timeout, unit);
        return transform(value);
    }

    private R transform(T value) throws ExecutionException {
        try {
            return transform.apply(value);
        } catch (Throwable th) {
            throw new ExecutionException(th);
        }
    }

}
