package com.nhn.pinpoint.rpc.server;

import com.nhn.pinpoint.rpc.packet.*;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class ServerStreamChannel {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int NONE = 0;
    // OPEN이 도착함.
    private static final int OPEN_ARRIVED = 1;
    // create success 던짐. 동작중
    private static final int RUN = 2;
    // 닫힘
    private static final int CLOSED = 2;

    private final AtomicInteger state = new AtomicInteger(NONE);

    private final int channelId;

    private ServerStreamChannelManager serverStreamChannelManager;

    private Channel channel;

    public ServerStreamChannel(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }


//    public boolean receiveStreamPacket(StreamPacket packet) {
//        final short packetType = packet.getPacketType();
//        switch (packetType) {
//            case PacketType.APPLICATION_STREAM_CREATE:
//                logger.info("APPLICATION_STREAM_CREATE_SUCCESS");
//                return receiveChannelCreate((StreamCreatePacket) packet);
//        }
//        return false;
//    }

    public boolean receiveChannelCreate(StreamCreatePacket streamCreateResponse) {
        if (state.compareAndSet(NONE, OPEN_ARRIVED)) {
            return true;
        } else {
            logger.info("invalid state:{}", state.get());
            return false;
        }
    }

    public boolean sendOpenResult(boolean success, byte[] bytes) {
        if(success ) {
            if(!state.compareAndSet(OPEN_ARRIVED, RUN)) {
                return false;
            }
            StreamCreateSuccessPacket streamCreateSuccessPacket = new StreamCreateSuccessPacket(channelId, bytes);
            this.channel.write(streamCreateSuccessPacket);
            return true;
        } else {
            if(!state.compareAndSet(OPEN_ARRIVED, CLOSED)) {
                return false;
            }
            StreamCreateFailPacket streamCreateFailPacket = new StreamCreateFailPacket(channelId, bytes);
            this.channel.write(streamCreateFailPacket);
            return true;
        }
    }

    public boolean sendStreamMessage(byte[] bytes) {
        if (state.get() != RUN) {
            return false;
        }
        StreamResponsePacket response = new StreamResponsePacket(bytes);
        this.channel.write(response);
        return true;
    }


    public boolean close() {
        return close0(true);
    }

    boolean closeInternal() {
        return close0(false);
    }

    private boolean close0(boolean safeClose) {
        if (!state.compareAndSet(RUN, CLOSED)) {
            return false;
        }

        if (safeClose) {
            StreamClosePacket streamClosePacket = new StreamClosePacket(channelId);
            this.channel.write(streamClosePacket);

            ServerStreamChannelManager serverStreamChannelManager = this.serverStreamChannelManager;
            if (serverStreamChannelManager != null) {
                serverStreamChannelManager.closeChannel(channelId);
                this.serverStreamChannelManager = null;
            }
        }
        return true;
    }

    public void setServerStreamChannelManager(ServerStreamChannelManager serverStreamChannelManager) {
        this.serverStreamChannelManager = serverStreamChannelManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerStreamChannel that = (ServerStreamChannel) o;

        if (channelId != that.channelId) return false;
        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = channelId;
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServerStreamChannel");
        sb.append("{state=").append(state);
        sb.append(", channelId=").append(channelId);
        sb.append(", channel=").append(channel);
        sb.append('}');
        return sb.toString();
    }
}

