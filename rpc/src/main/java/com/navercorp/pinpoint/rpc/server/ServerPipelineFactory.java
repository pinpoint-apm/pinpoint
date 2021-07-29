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

package com.navercorp.pinpoint.rpc.server;


import com.navercorp.pinpoint.rpc.codec.PacketEncoder;
import com.navercorp.pinpoint.rpc.codec.ServerPacketDecoder;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor.PinpointServerChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import java.util.Objects;

/**
 * @author emeroad
 */
public class ServerPipelineFactory implements ChannelPipelineFactory {
    private final PinpointServerChannelHandler pinpointServerChannelHandler;

    public ServerPipelineFactory(PinpointServerChannelHandler pinpointServerChannelHandler) {
        this.pinpointServerChannelHandler = Objects.requireNonNull(pinpointServerChannelHandler, "pinpointServerChannelHandler");
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        // ServerPacketDecoder passes the PING related packets(without status value) to the pinpointServerChannelHandler.
        pipeline.addLast("decoder", new ServerPacketDecoder());
        pipeline.addLast("encoder", new PacketEncoder());
        pipeline.addLast("handler", pinpointServerChannelHandler);

        return pipeline;
    }

}
