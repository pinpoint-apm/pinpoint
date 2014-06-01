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
public class ClosableHttpAsyncClientModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ClosableHttpAsyncClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public String getTargetClass() {
		return "org/apache/http/impl/nio/client/CloseableHttpAsyncClient";
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {} @ {}", javassistClassName, classLoader);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
 
			/**
			 * 아래 두 메소드는 오버로드 되었으나 호출 관계가 없어 scope 없어도 됨.
			 */
			Interceptor executeInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,
					protectedDomain,
					"com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.AsyncClientExecuteInterceptor");
			
			String[] executeParams = new String[] { 
					"org.apache.http.HttpHost", 
					"org.apache.http.HttpRequest", 
					"org.apache.http.protocol.HttpContext", 
					"org.apache.http.concurrent.FutureCallback"
					};
			aClass.addInterceptor("execute", executeParams, executeInterceptor);
			
			/**
			 * 
			 */
			Interceptor internalExecuteInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,
					protectedDomain,
					"com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor.AsyncInternalClientExecuteInterceptor");
			
			String[] internalExecuteParams = new String[] { 
					"org.apache.http.nio.protocol.HttpAsyncRequestProducer", 
					"org.apache.http.nio.protocol.HttpAsyncResponseConsumer", 
					"org.apache.http.concurrent.FutureCallback"
					};
			aClass.addInterceptor("execute", internalExecuteParams, internalExecuteInterceptor);
			
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}
