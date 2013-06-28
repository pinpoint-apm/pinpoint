package com.nhn.pinpoint.common.io.rpc.message;

import com.nhn.pinpoint.common.io.rpc.packet.Packet;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 *
 */
public class Encoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (!(msg instanceof Packet)) {
            return null;
        }
        Packet packet = (Packet) msg;
        return packet.toBuffer();
    }
}
