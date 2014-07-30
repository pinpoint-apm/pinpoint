package com.nhn.pinpoint.rpc.server;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.ChannelWriteFailListenableFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.client.RequestManager;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class SocketChannel {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final long timeoutMillis;
    private final Timer timer;
    private final RequestManager requestManager;
    
    private ChannelFutureListener responseWriteFail;

    public SocketChannel(final Channel channel, long timeoutMillis, Timer timer) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (timer == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
        this.timeoutMillis = timeoutMillis;
        this.timer = timer;
        this.requestManager = new RequestManager(this.timer);
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

    public Future sendRequestMessage(byte[] payload) {
    	if (payload == null) {
            throw new NullPointerException("requestMessage must not be null");
    	}
    	RequestPacket requestPacket = new RequestPacket(payload);
    	
    	ChannelWriteFailListenableFuture<ResponseMessage> messageFuture = this.requestManager.register(requestPacket, this.timeoutMillis);
    	
    	ChannelFuture write = this.channel.write(requestPacket);
    	write.addListener(messageFuture);
    	
    	return messageFuture;
    }
    
    public void sendMessage(byte[] payload) {
        SendPacket send = new SendPacket(payload);
        this.channel.write(send);
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
