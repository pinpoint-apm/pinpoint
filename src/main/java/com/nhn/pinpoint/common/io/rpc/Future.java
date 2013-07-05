package com.nhn.pinpoint.common.io.rpc;

/**
 *
 */
public interface Future<T> {
    T getObject();

    Throwable getCause();

    boolean isReady();

    boolean isSuccess();

    boolean setListener(FutureListener<T> listener);

    boolean await(long timeoutMillis);

    boolean await();
}
