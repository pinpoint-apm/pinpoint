package com.nhn.pinpoint.rpc.packet;

/**
 * @author emeroad
 */
public abstract class BasicStreamPacket implements StreamPacket {

    protected byte[] payload;

    protected int channelId;

    public BasicStreamPacket() {
    }

    public BasicStreamPacket(byte[] payload) {
        if (payload == null) {
            throw new NullPointerException("payload");
        }
        this.payload = payload;
    }

    protected BasicStreamPacket(int channelId) {
        this.channelId = channelId;
    }

    protected BasicStreamPacket(int channelId, byte[] payload) {
        this.payload = payload;
        this.channelId = channelId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
}
