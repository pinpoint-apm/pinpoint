package com.nhn.pinpoint.common.io.rpc;

/**
 *
 */
public interface FailureHandle {

    void handleFailure(int requestId);
}
