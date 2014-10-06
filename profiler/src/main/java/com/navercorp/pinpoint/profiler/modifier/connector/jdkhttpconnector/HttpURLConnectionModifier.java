package com.nhn.pinpoint.profiler.modifier.connector.jdkhttpconnector;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.connector.jdkhttpconnector.interceptor.ConnectMethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO classloader문제 있음.
 * @author netspider
 * 
 */
public class HttpURLConnectionModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HttpURLConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "sun/net/www/protocol/http/HttpURLConnection";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
            ConnectMethodInterceptor connectMethodInterceptor = new ConnectMethodInterceptor();
            aClass.addInterceptorCallByContextClassLoader("connect", null, connectMethodInterceptor);

			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("HttpURLConnectionModifier fail. Caused:", e.getMessage(), e);
			return null;
		}
	}
}