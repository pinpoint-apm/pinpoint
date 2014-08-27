package com.nhn.pinpoint.profiler.modifier.connector.nimm;

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
 * @author netspider
 * 
 */
public class NimmInvokerModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public NimmInvokerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "com/nhncorp/lucy/nimm/connector/bloc/NimmInvoker";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			// trace variable
			aClass.addTraceVariable("_nimmAddress", "__setNimmAddress", "__getNimmAddress", "java.lang.String");

			// TODO nimm socket도 수집해야하나??
			// aClass.addTraceVariable("_nimmSocket", "__setNimmSocket", "__getNimmSocket", "com.nhncorp.lucy.nimm.connector.NimmSocket");
			
			// constructor
			Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.nimm.interceptor.NimmInvokerConstructorInterceptor");
			aClass.addConstructorInterceptor(new String[] { "com.nhncorp.lucy.nimm.connector.address.NimmAddress", "com.nhncorp.lucy.nimm.connector.NimmSocket", "long" }, constructorInterceptor);

			// invoke method
			Interceptor invokeInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.connector.nimm.interceptor.InvokeMethodInterceptor");
			aClass.addInterceptor("invoke", new String[] { "long", "java.lang.String", "java.lang.String", "java.lang.Object[]" }, invokeInterceptor);

			return aClass.toBytecode();
		} catch (Throwable e) {
			logger.warn("NimmInvoker modifier error. Caused:{}", e.getMessage(), e);
			return null;
		}
	}
}