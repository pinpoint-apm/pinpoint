package com.nhn.pinpoint.common.io.rpc;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

/**
 *
 */
public class DiscardServerHandler extends SimpleChannelUpstreamHandler {


    private long transferredBytes;

    public long getTransferredBytes() {
        return transferredBytes;
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            System.out.println(e.toString());
        }

        // Let SimpleChannelHandler call actual event handler methods below.
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        // Discard received data silently by doing nothing.

        transferredBytes += ((ChannelBuffer) e.getMessage()).readableBytes();
        System.out.println("먼가 메시지를 받았당. :" + ((ChannelBuffer) e.getMessage()).readableBytes());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        System.out.println("Unexpected exception from downstream. Caused:" + e.getCause());
        e.getChannel().close();
    }
}
