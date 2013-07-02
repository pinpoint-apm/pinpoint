package com.nhn.pinpoint.common.io.rpc;

/**
 *
 */
public class ResponseMessage implements Message {
    private byte[] message;

    public ResponseMessage() {
    }

    public void setMessage(byte[] payload) {
        if (payload == null) {
            throw new NullPointerException("message");
        }
        this.message = payload;
    }

    @Override
    public byte[] getMessage() {
        return message;
    }
}
