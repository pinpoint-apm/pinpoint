package com.profiler.modifier.servlet;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import com.profiler.logging.Logger;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class FilterModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(FilterModifier.class.getName());

	public FilterModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "javax/servlet/Filter";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			Interceptor doFilterInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");

//			setTraceContext(doFilterInterceptor);

			InstrumentClass servlet = byteCodeInstrumentor.getClass(javassistClassName);

			servlet.addInterceptor("doFilter", new String[] { "javax.servlet.ServletRequest", "javax.servlet.ServletResponse", "javax.servlet.FilterChain" }, doFilterInterceptor);

			return servlet.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}