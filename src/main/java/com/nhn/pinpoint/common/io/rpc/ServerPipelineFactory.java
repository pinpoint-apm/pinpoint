package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.codec.PacketDecoder;
import com.nhn.pinpoint.common.io.rpc.codec.PacketEncoder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 *
 */
public class ServerPipelineFactory implements ChannelPipelineFactory {
    private PinpointServerSocket serverSocket;

    public ServerPipelineFactory(PinpointServerSocket pinpointServerSocket) {
        if (pinpointServerSocket == null) {
            throw new NullPointerException("pinpointServerSocket");
        }
        this.serverSocket = pinpointServerSocket;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();

        pipeline.addLast("decoder", new PacketDecoder());
        pipeline.addLast("encoder", new PacketEncoder());
        pipeline.addLast("handler", serverSocket);

        return pipeline;
    }
}
