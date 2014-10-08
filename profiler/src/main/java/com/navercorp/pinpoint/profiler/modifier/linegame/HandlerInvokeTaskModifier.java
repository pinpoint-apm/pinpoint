package com.nhn.pinpoint.profiler.modifier.linegame;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.DedicatedModifier;

/**
 * line game의 baseframework에서 netty pipeline으로 사용하는 HttpCustomServerHandler의 비동기
 * processor인 InvokeTask를 수정하는 클래스
 * 
 * @author netspider
 * 
 */
public class HandlerInvokeTaskModifier extends DedicatedModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HandlerInvokeTaskModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/linecorp/games/common/baseFramework/handlers/HttpCustomServerHandler$InvokeTask";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {} @ {}", javassistClassName, classLoader);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			// constructor parameter trace object
			aClass.addTraceVariable("__channelHandlerContext", "__setChannelHandlerContext", "__getChannelHandlerContext", "org.jboss.netty.channel.ChannelHandlerContext");
			aClass.addTraceVariable("__messageEvent", "__setMessageEvent", "__getMessageEvent", "org.jboss.netty.channel.MessageEvent");

			// non static inner class는 constructor argument의 첫번째가 parent class임.
			Interceptor constInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.linegame.interceptor.InvokeTaskConstructorInterceptor");
			aClass.addConstructorInterceptor(new String[] { "com.linecorp.games.common.baseFramework.handlers.HttpCustomServerHandler", "org.jboss.netty.channel.ChannelHandlerContext", "org.jboss.netty.channel.MessageEvent" }, constInterceptor);

			Interceptor runInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.linegame.interceptor.InvokeTaskRunInterceptor");
			aClass.addInterceptor("run", null, runInterceptor);

			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}