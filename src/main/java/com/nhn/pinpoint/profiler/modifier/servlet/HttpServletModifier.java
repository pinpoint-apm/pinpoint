package com.nhn.pinpoint.profiler.modifier.servlet;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class HttpServletModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public HttpServletModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "javax/servlet/http/HttpServlet";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. " + javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			Interceptor doGetInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.method.method.interceptor.MethodInterceptor");
			Interceptor doPostInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.method.method.interceptor.MethodInterceptor");


			InstrumentClass servlet = byteCodeInstrumentor.getClass(javassistClassName);

			servlet.addInterceptor("doGet", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doGetInterceptor);
			servlet.addInterceptor("doPost", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doPostInterceptor);

			return servlet.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}