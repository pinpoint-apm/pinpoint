/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPingPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPongPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

import org.jboss.netty.channel.Channel;

import java.net.SocketAddress;

/**
 * @author koo.taejin
 */
public class NettyClientStreamChannel extends AbstractStreamChannel implements ClientStreamChannel {

    private final Channel channel;
    private final ClientStreamChannelEventHandler streamChannelEventHandler;

    public NettyClientStreamChannel(Channel channel, int streamId, StreamChannelRepository streamChannelRepository, ClientStreamChannelEventHandler streamChannelEventHandler) {
        super(streamId, streamChannelRepository);
        this.channel = Assert.requireNonNull(channel, "channel");
        this.streamChannelEventHandler = Assert.requireNonNull(streamChannelEventHandler, "streamChannelEventHandler");
    }

    public void connect(byte[] payload, long timeout) throws StreamException {
        changeStateTo(StreamChannelStateCode.CONNECT_AWAIT, true);

        sendCreate(payload);

        boolean connected = awaitOpen(timeout);
        if (connected) {
            logger.info("Open streamChannel initialization completed. streamChannel:{} ", this);
        } else {
            throw new StreamException(StreamCode.CONNECTION_TIMEOUT);
        }
    }

    private void sendCreate(byte[] payload) {
        StreamCreatePacket packet = new StreamCreatePacket(getStreamId(), payload);
        write(StreamChannelStateCode.CONNECT_AWAIT, packet);
    }

    @Override
    public void sendPing(int requestId) {
        StreamPingPacket packet = new StreamPingPacket(getStreamId(), requestId);
        write(StreamChannelStateCode.CONNECTED, packet);
    }

    @Override
    public void sendPong(int requestId) {
        StreamPongPacket packet = new StreamPongPacket(getStreamId(), requestId);
        write(StreamChannelStateCode.CONNECTED, packet);
    }

    @Override
    public void close(StreamCode code) {
        clearStreamChannelResource();
        if (!StreamCode.isConnectionError(code)) {
            try {
                StreamClosePacket packet = new StreamClosePacket(getStreamId(), code);
                write(packet);
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    private void write(StreamPacket packet) {
        write(null, packet);
    }

    private void write(StreamChannelStateCode expectedCode, StreamPacket packet) {
        if (expectedCode != null) {
            state.assertState(expectedCode);
        }
        channel.write(packet);
    }

    @Override
    public void handleStreamResponsePacket(StreamResponsePacket packet) throws StreamException {
        if (state.checkState(StreamChannelStateCode.CONNECTED)) {
            streamChannelEventHandler.handleStreamResponsePacket(this, packet);
        } else if (state.checkState(StreamChannelStateCode.CONNECT_AWAIT)) {
            // may happen in the timing
        } else {
            throw new StreamException(StreamCode.STATE_NOT_CONNECTED);
        }
    }

    @Override
    public void handleStreamClosePacket(StreamClosePacket packet) {
        streamChannelEventHandler.handleStreamClosePacket(this, packet);
        disconnect(packet.getCode());
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    @Override
    protected StreamChannelStateChangeEventHandler getStateChangeEventHandler() {
        return streamChannelEventHandler;
    }

}
