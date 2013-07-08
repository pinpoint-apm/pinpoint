package com.nhn.pinpoint.common.io.rpc;

/**
 *
 */
public class StreamCreateResponse extends ResponseMessage {
    private boolean success;

    public StreamCreateResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
