package com.nhn.pinpoint.rpc.server;

import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * @author emeroad
 */
public class SocketChannel {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;

    private ChannelFutureListener responseWriteFail;

    public SocketChannel(final Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
        this.responseWriteFail = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    logger.warn("responseWriteFail. {}", channel);
                }
            }
        };
    }

    public void sendResponseMessage(RequestPacket requestPacket, byte[] responseMessage) {
        if (requestPacket == null) {
            throw new NullPointerException("requestPacket must not be null");
        }
        ResponsePacket responsePacket = new ResponsePacket(requestPacket.getRequestId(), responseMessage);
        ChannelFuture write = this.channel.write(responsePacket);
        write.addListener(responseWriteFail);
    }

    public void sendRequestMessage(byte[] requestMessage) {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketChannel that = (SocketChannel) o;

        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SocketChannel");
        sb.append("{channel=").append(channel);
        sb.append('}');
        return sb.toString();
    }

    public SocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }
}
