package com.nhn.pinpoint.profiler.modifier.netty;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;
import com.nhn.pinpoint.profiler.modifier.netty.interceptor.HandlerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class HandlerModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhn/pinpointtest/HttpCustomServerHandler$InvokeTask";
		// return "com/nhn/pinpointtest/HttpCustomServerHandler$InvokeTask";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {} @ {}", javassistClassName, classLoader);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			Interceptor runInterceptor = new HandlerInterceptor();
			aClass.addInterceptor("run", null, runInterceptor);

			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}