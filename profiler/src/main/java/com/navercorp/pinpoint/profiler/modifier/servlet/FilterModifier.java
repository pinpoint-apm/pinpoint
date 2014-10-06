package com.nhn.pinpoint.profiler.modifier.servlet;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class FilterModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public FilterModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "javax/servlet/Filter";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			Interceptor doFilterInterceptor = new MethodInterceptor();

//			setTraceContext(doFilterInterceptor);

			InstrumentClass servlet = byteCodeInstrumentor.getClass(javassistClassName);

			servlet.addInterceptor("doFilter", new String[] { "javax.servlet.ServletRequest", "javax.servlet.ServletResponse", "javax.servlet.FilterChain" }, doFilterInterceptor);

			return servlet.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}