package com.nhn.pinpoint.common.io.rpc.packet;

import com.nhn.pinpoint.common.io.rpc.packet.Packet;

/**
 *
 */
public abstract class AbstractPacket implements Packet {

    protected byte[] payload;

    protected AbstractPacket() {
    }

    public AbstractPacket(byte[] payload) {
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
