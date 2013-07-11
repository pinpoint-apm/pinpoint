package com.nhn.pinpoint.common.rpc.codec;

import com.nhn.pinpoint.common.rpc.packet.Packet;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PacketEncoder extends OneToOneEncoder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof Packet)) {
            logger.error("invalid packet:{} channel:{}", msg, channel);
            return null;
        }
        Packet packet = (Packet) msg;
        return packet.toBuffer();
    }
}
