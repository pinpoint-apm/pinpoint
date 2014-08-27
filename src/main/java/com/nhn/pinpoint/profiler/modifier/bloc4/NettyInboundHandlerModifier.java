package com.nhn.pinpoint.profiler.modifier.bloc4;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author netspider
 */
public class NettyInboundHandlerModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public NettyInboundHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhncorp/lucy/bloc/http/NettyInboundHandler";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			aClass.addGetter("__getUriEncoding", "uriEncoding", "java.nio.charset.Charset");
			
			Interceptor read0nterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.bloc4.interceptor.ChannelRead0Interceptor");
			aClass.addInterceptor("channelRead0", new String[] { "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.FullHttpRequest" }, read0nterceptor);

			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("NettyInboundHandlerModifier fail. Caused:", e.getMessage(), e);
			return null;
		}
	}
}