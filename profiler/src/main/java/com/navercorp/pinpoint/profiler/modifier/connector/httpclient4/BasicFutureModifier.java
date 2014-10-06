package com.nhn.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class BasicFutureModifier extends AbstractModifier {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public BasicFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public String getTargetClass() {
		return "org/apache/http/concurrent/BasicFuture";
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {} @ {}", javassistClassName, classLoader);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			Interceptor futureGetInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureGetInterceptor");
			aClass.addInterceptor("get", null, futureGetInterceptor);
			
			Interceptor futureGetInterceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureGetInterceptor");
			aClass.addInterceptor("get", new String[] { "long", "java.util.concurrent.TimeUnit" }, futureGetInterceptor2);

			Interceptor futureCompletedInterceptor  = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureCompletedInterceptor");
			aClass.addInterceptor("completed", new String[] { "java.lang.Object" }, futureCompletedInterceptor);

			Interceptor futureFailedInterceptor  = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureFailedInterceptor");
			aClass.addInterceptor("failed", new String[] { "java.lang.Exception" }, futureFailedInterceptor);
			
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}
