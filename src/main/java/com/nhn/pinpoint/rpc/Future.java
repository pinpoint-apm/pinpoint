package com.nhn.pinpoint.rpc;

/**
 * @author emeroad
 * @author koo.taejin
 */
public interface Future<T> {

    T getResult();

    Throwable getCause();

    boolean isReady();

    boolean isSuccess();

    boolean addListener(FutureListener<T> listener);

    boolean await(long timeoutMillis);

    boolean await();
}
