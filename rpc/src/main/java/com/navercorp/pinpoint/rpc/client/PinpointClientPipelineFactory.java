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
public class PinpointClientPipelineFactory implements ChannelPipelineFactory {

    private final DefaultPinpointClientFactory pinpointClientFactory;

    public PinpointClientPipelineFactory(DefaultPinpointClientFactory pinpointClientFactory) {
        if (pinpointClientFactory == null) {
            throw new NullPointerException("pinpointClientFactory must not be null");
        }
        this.pinpointClientFactory = pinpointClientFactory;
    }


    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("encoder", new PacketEncoder());
        pipeline.addLast("decoder", new PacketDecoder());
        
        long pingDelay = pinpointClientFactory.getPingDelay();
        long enableWorkerPacketDelay = pinpointClientFactory.getEnableWorkerPacketDelay();
        long timeoutMillis = pinpointClientFactory.getTimeoutMillis();
        
        DefaultPinpointClientHandler defaultPinpointClientHandler = new DefaultPinpointClientHandler(pinpointClientFactory, pingDelay, enableWorkerPacketDelay, timeoutMillis);
        pipeline.addLast("writeTimeout", new WriteTimeoutHandler(defaultPinpointClientHandler.getChannelTimer(), 3000, TimeUnit.MILLISECONDS));
        pipeline.addLast("socketHandler", defaultPinpointClientHandler);
        
        return pipeline;
    }
}
