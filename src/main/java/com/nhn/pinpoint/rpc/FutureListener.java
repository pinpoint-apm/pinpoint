package com.nhn.pinpoint.rpc;

/**
 * @author emeroad
 */
public interface FutureListener<T> {
    void onComplete(Future<T> future);
}
