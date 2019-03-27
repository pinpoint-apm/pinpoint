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
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 * @author koo.taejin
 */
public class ClientStreamChannel extends StreamChannel {

    private final ClientStreamChannelEventHandler streamChannelEventHandler;

    public ClientStreamChannel(Channel channel, int streamId, StreamChannelRepository streamChannelRepository, ClientStreamChannelEventHandler streamChannelEventHandler) {
        super(channel, streamId, streamChannelRepository);
        this.streamChannelEventHandler = Assert.requireNonNull(streamChannelEventHandler, "streamChannelEventHandler must not be null");
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

    private ChannelFuture sendCreate(byte[] payload) {
        state.assertState(StreamChannelStateCode.CONNECT_AWAIT);

        StreamCreatePacket packet = new StreamCreatePacket(getStreamId(), payload);
        return channel.write(packet);
    }

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
    StreamChannelStateChangeEventHandler getStateChangeEventHandler() {
        return streamChannelEventHandler;
    }

    @Override
    public String toString() {
        return "ClientStreamChannel{" +
                "remoteAddress=" + getRemoteAddress() +
                ", streamId=" + getStreamId() +
                ", state=" + state +
                '}';
    }

}
