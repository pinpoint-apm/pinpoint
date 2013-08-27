package com.nhn.pinpoint.rpc.client;


import com.nhn.pinpoint.common.util.PinpointThreadFactory;
import com.nhn.pinpoint.rpc.codec.PacketDecoder;
import com.nhn.pinpoint.rpc.codec.PacketEncoder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.ThreadNameDeterminer;
import org.jboss.netty.util.Timer;

import java.util.concurrent.TimeUnit;

/**
 *
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

        PinpointSocketHandler pinpointSocketHandler = new PinpointSocketHandler(pinpointSocketFactory);
        pipeline.addLast("writeTimeout", new WriteTimeoutHandler(pinpointSocketHandler.getTimer(), 3));
        pipeline.addLast("socketHandler", pinpointSocketHandler);
        return pipeline;
    }
}
