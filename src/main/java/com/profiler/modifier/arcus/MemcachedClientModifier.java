package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

/**
 * @author netspider
 */
public class MemcachedClientModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MemcachedClientModifier.class.getName());

	public MemcachedClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "net/spy/memcached/MemcachedClient";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			aClass.addTraceVariable("__serviceCode", "__setServiceCode", "__getServiceCode", "java.lang.String");

			Interceptor addOpInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.arcus.interceptors.AddOpInterceptor");
			aClass.addInterceptor("addOp", new String[] { "java.lang.String", "net.spy.memcached.ops.Operation" }, addOpInterceptor);

			return aClass.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(Level.WARNING, e.getMessage(), e);
			}
			return null;
		}
	}
}