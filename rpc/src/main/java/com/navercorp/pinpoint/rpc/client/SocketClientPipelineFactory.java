package com.nhn.pinpoint.rpc.client;


import com.nhn.pinpoint.rpc.codec.PacketDecoder;
import com.nhn.pinpoint.rpc.codec.PacketEncoder;
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
