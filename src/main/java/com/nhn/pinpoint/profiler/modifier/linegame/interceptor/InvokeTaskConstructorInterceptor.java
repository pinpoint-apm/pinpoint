package com.nhn.pinpoint.profiler.modifier.linegame.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class InvokeTaskConstructorInterceptor implements SimpleAroundInterceptor, TargetClassLoader {

	private MetaObject<org.jboss.netty.channel.ChannelHandlerContext> setChannelHandlerContext = new MetaObject<org.jboss.netty.channel.ChannelHandlerContext>("__setChannelHandlerContext", org.jboss.netty.channel.ChannelHandlerContext.class);
	private MetaObject<org.jboss.netty.channel.MessageEvent> setMessageEvent = new MetaObject<org.jboss.netty.channel.MessageEvent>("__setMessageEvent", org.jboss.netty.channel.MessageEvent.class);

	@Override
	public void before(Object target, Object[] args) {
		// 대상 class가 non-static이기 때문에 코드상의 argument length는 2이지만 byte code상으로는
		// 3이다.
		if (args.length != 3) {
			return;
		}

		if (!(args[1] instanceof org.jboss.netty.channel.ChannelHandlerContext)) {
			return;
		}

		if (!(args[2] instanceof org.jboss.netty.channel.MessageEvent)) {
			return;
		}

		setChannelHandlerContext.invoke(target, (org.jboss.netty.channel.ChannelHandlerContext) args[1]);
		setMessageEvent.invoke(target, (org.jboss.netty.channel.MessageEvent) args[2]);
	}

	@Override
	public void after(Object target, Object[] args, Object result) {

	}
}
