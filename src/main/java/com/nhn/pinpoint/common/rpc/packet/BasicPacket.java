package com.nhn.pinpoint.common.rpc.packet;

/**
 *
 */
public abstract class BasicPacket implements Packet {

    protected byte[] payload;

    protected BasicPacket() {
    }

    public BasicPacket(byte[] payload) {
        if (payload == null) {
            throw new NullPointerException("payload");
        }
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

}
