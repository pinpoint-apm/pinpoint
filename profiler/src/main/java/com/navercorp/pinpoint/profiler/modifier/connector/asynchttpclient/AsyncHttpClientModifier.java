package com.nhn.pinpoint.profiler.modifier.connector.asynchttpclient;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

/**
 * 
 * https://github.com/AsyncHttpClient/async-http-client modifier
 * 
 * @author netspider
 * 
 */
public class AsyncHttpClientModifier extends AbstractModifier {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public AsyncHttpClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/ning/http/client/AsyncHttpClient";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			Interceptor executeRequestInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.asynchttpclient.interceptor.ExecuteRequestInterceptor");
			aClass.addInterceptor("executeRequest", new String[] { "com.ning.http.client.Request", "com.ning.http.client.AsyncHandler" }, executeRequestInterceptor);

			return aClass.toBytecode();
		} catch (Throwable e) {
			logger.warn("httpClient4 modifier error. Caused:{}", e.getMessage(), e);
			return null;
		}
	}

}
