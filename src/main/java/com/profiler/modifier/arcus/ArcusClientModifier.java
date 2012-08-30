package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class ArcusClientModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(ArcusClientModifier.class.getName());

	public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/http/impl/client/AbstractHttpClient";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.connector.interceptors.ExecuteMethodInterceptor");
		if (interceptor == null) {
			return null;
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

		InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
		aClass.addInterceptor("execute", new String[] { "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext" }, interceptor);

		return aClass.toBytecode();
	}
}