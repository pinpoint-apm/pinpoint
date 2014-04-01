package com.nhn.pinpoint.testweb.netty;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * 
 * @author netspider
 * 
 */
public class EchoServerHandler extends SimpleChannelUpstreamHandler {
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		e.getChannel().write(e.getMessage());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		e.getChannel().close();
	}
}
