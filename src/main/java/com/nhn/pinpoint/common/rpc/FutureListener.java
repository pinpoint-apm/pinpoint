package com.nhn.pinpoint.common.rpc;

/**
 *
 */
public interface FutureListener<T> {
    void onComplete(Future<T> future);
}
