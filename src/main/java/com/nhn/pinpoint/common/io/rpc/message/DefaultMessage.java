package com.nhn.pinpoint.common.io.rpc.message;

/**
 *
 */
public class DefaultMessage implements Message {
    private int type;
    private byte[] message;

    public DefaultMessage(int type) {
        this.type = type;
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
