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
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

/**
 * @author koo.taejin
 */
public class ClientStreamChannel extends StreamChannel {

    private final ClientStreamChannelMessageListener messageListener;

    public ClientStreamChannel(Channel channel, int streamId, StreamChannelRepository streamChannelRepository, ClientStreamChannelMessageListener messageListener) {
        super(channel, streamId, streamChannelRepository);
        this.messageListener = Assert.requireNonNull(messageListener, "messageListener must not be null");
    }

    public ChannelFuture sendCreate(byte[] payload) {
        state.assertState(StreamChannelStateCode.CONNECT_AWAIT);

        StreamCreatePacket packet = new StreamCreatePacket(getStreamId(), payload);
        return channel.write(packet);
    }

    boolean changeStateConnectAwait() {
        return changeStateTo(StreamChannelStateCode.CONNECT_AWAIT);
    }

    public void handleStreamData(StreamResponsePacket packet) {
        messageListener.handleStreamData(this, packet);
    }

    @Override
    public void handleStreamClose(StreamClosePacket packet) {
        messageListener.handleStreamClose(this, packet);
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
