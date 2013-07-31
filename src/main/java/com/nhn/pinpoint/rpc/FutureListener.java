package com.nhn.pinpoint.rpc;

/**
 *
 */
public interface FutureListener<T> {
    void onComplete(Future<T> future);
}
