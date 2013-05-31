package com.nhn.pinpoint.modifier.connector.jdkhttpconnector;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.modifier.AbstractModifier;
import com.nhn.pinpoint.modifier.connector.httpclient4.HttpClient4Modifier;
import com.nhn.pinpoint.modifier.connector.jdkhttpconnector.interceptor.ConnectMethodInterceptor;

/**
 * TODO classloader문제 있음.
 * @author netspider
 * 
 */
public class HttpURLConnectionModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(HttpClient4Modifier.class.getName());

	public HttpURLConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "sun/net/www/protocol/http/HttpURLConnection";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
            ConnectMethodInterceptor connectMethodInterceptor = new ConnectMethodInterceptor();
            aClass.addInterceptorCallByContextClassLoader("connect", null, connectMethodInterceptor);

			return aClass.toBytecode();
		} catch (InstrumentException e) {
			e.printStackTrace();
			return null;
		}
	}
}