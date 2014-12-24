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


import com.navercorp.pinpoint.rpc.codec.PacketDecoder;
import com.navercorp.pinpoint.rpc.codec.PacketEncoder;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class SocketClientPipelineFactory implements ChannelPipelineFactory {

    private final PinpointSocketFactory pinpointSocketFactory;

    public SocketClientPipelineFactory(PinpointSocketFactory pinpointSocketFactory) {
        if (pinpointSocketFactory == null) {
            throw new NullPointerException("pinpointSocketFactory must not be null");
        }
        this.pinpointSocketFactory = pinpointSocketFactory;
    }


    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("encoder", new PacketEncoder());
        pipeline.addLast("decoder", new PacketDecoder());
        
        long pingDelay = pinpointSocketFactory.getPingDelay();
        long enableWorkerPacketDelay = pinpointSocketFactory.getEnableWorkerPacketDelay();
        long timeoutMillis = pinpointSocketFactory.getTimeoutMillis();
        
        PinpointSocketHandler pinpointSocketHandler = new PinpointSocketHandler(pinpointSocketFactory, pingDelay, enableWorkerPacketDelay, timeoutMillis);
        pipeline.addLast("writeTimeout", new WriteTimeoutHandler(pinpointSocketHandler.getChannelTimer(), 3000, TimeUnit.MILLISECONDS));
        pipeline.addLast("socketHandler", pinpointSocketHandler);
        
        return pipeline;
    }
}
