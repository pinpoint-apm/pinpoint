package com.profiler.modifier.servlet;

import java.security.ProtectionDomain;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.Agent;
import com.nhn.pinpoint.common.ServiceType;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class SpringFrameworkServletModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(SpringFrameworkServletModifier.class);

	public SpringFrameworkServletModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "org/springframework/web/servlet/FrameworkServlet";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			Interceptor doGetInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
            setServiceType(doGetInterceptor, ServiceType.SPRING_MVC);

            Interceptor doPostInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.method.interceptors.MethodInterceptor");
            setServiceType(doPostInterceptor, ServiceType.SPRING_MVC);



			InstrumentClass servlet = byteCodeInstrumentor.getClass(javassistClassName);
			servlet.addInterceptor("doGet", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doGetInterceptor);

			servlet.addInterceptor("doPost", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doPostInterceptor);

			return servlet.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}