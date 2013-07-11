package com.nhn.pinpoint.common.rpc;

/**
 *
 */
public interface Future<T> {

    T getResult();

    Throwable getCause();

    boolean isReady();

    boolean isSuccess();

    boolean setListener(FutureListener<T> listener);

    boolean await(long timeoutMillis);

    boolean await();
}
