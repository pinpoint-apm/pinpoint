package com.nhn.pinpoint.rpc.server;


import com.nhn.pinpoint.rpc.codec.PacketDecoder;
import com.nhn.pinpoint.rpc.codec.PacketEncoder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 * @author emeroad
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
