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

import org.jboss.netty.channel.Channel;

import com.navercorp.pinpoint.rpc.packet.stream.StreamPacket;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.StreamChannelManager;

/**
 * @author Taejin Koo
 */
public class PinpointSocketHandlerContext {
    private final Channel channel;
    private final StreamChannelManager streamChannelManager;

    public PinpointSocketHandlerContext(Channel channel, StreamChannelManager streamChannelManager) {
        if (channel == null) {
            throw new NullPointerException("channel must not be null");
        }
        if (streamChannelManager == null) {
            throw new NullPointerException("streamChannelManager must not be null");
        }
        this.channel = channel;
        this.streamChannelManager = streamChannelManager;
    }

    public Channel getChannel() {
        return channel;
    }

    public ClientStreamChannelContext createStream(byte[] payload, ClientStreamChannelMessageListener clientStreamChannelMessageListener) {
        return streamChannelManager.openStreamChannel(payload, clientStreamChannelMessageListener);
    }

    public void handleStreamEvent(StreamPacket message) {
        streamChannelManager.messageReceived(message);
    }

    public void closeAllStreamChannel() {
        streamChannelManager.close();
    }

    public StreamChannelContext getStreamChannel(int streamChannelId) {
        return streamChannelManager.findStreamChannel(streamChannelId);
    }
    
}
