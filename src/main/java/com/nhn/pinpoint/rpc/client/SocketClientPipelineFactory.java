package com.nhn.pinpoint.rpc.client;


import com.nhn.pinpoint.rpc.codec.PacketDecoder;
import com.nhn.pinpoint.rpc.codec.PacketEncoder;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

/**
 *
 */
public class SocketClientPipelineFactory implements ChannelPipelineFactory {
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("encoder", new PacketEncoder());
        pipeline.addLast("decoder", new PacketDecoder());
        pipeline.addLast("socketHandler", new SocketHandler());
        return pipeline;
    }
}
