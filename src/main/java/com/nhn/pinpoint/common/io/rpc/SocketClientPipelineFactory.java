package com.nhn.pinpoint.common.io.rpc;


import com.nhn.pinpoint.common.io.rpc.codec.Decoder;
import com.nhn.pinpoint.common.io.rpc.codec.Encoder;
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
        pipeline.addLast("encoder", new Encoder());
        pipeline.addLast("decoder", new Decoder());
        return pipeline;
    }
}
