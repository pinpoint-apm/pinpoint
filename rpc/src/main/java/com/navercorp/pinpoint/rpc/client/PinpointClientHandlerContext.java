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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamChannelManager;
import com.navercorp.pinpoint.rpc.stream.StreamException;

import org.jboss.netty.channel.Channel;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class PinpointClientHandlerContext {
    private final Channel channel;
    private final StreamChannelManager streamChannelManager;

    public PinpointClientHandlerContext(Channel channel, StreamChannelManager streamChannelManager) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.streamChannelManager = Objects.requireNonNull(streamChannelManager, "streamChannelManager");
    }

    public Channel getChannel() {
        return channel;
    }

    public ClientStreamChannel openStream(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) throws StreamException {
        return streamChannelManager.openStream(payload, streamChannelEventHandler);
    }

    public ClientStreamChannel openStreamAndAwait(byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler, long timeout) throws StreamException {
        return streamChannelManager.openStreamAndAwait(payload, streamChannelEventHandler, timeout);
    }

    public void handleStreamEvent(StreamPacket message) {
        streamChannelManager.messageReceived(message);
    }

    public void closeAllStreamChannel() {
        streamChannelManager.close();
    }

}
