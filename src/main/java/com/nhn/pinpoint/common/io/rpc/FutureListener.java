package com.nhn.pinpoint.common.io.rpc;

/**
 *
 */
public interface FutureListener<T> {
    void onComplete(Future<T> future);
}
