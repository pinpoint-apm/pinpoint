package com.nhn.pinpoint.rpc;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DiscardServerHandler extends SimpleChannelUpstreamHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private long transferredBytes;

    public long getTransferredBytes() {
        return transferredBytes;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info("event:{}", e);
        }

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

        transferredBytes += ((ChannelBuffer) e.getMessage()).readableBytes();
        logger.info("messageReceived. meg:{} channel:{}", e.getMessage(), e.getChannel());
        logger.info("transferredBytes. transferredBytes:{}", transferredBytes);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from downstream. Caused:{}", e, e.getCause());
    }
}
