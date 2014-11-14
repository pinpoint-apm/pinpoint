package com.nhn.pinpoint.profiler.modifier.servlet;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.ServiceTypeSupport;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class SpringFrameworkServletModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

		try {
			Interceptor doGetInterceptor = new MethodInterceptor();
            setServiceType(doGetInterceptor, ServiceType.SPRING_MVC);

            Interceptor doPostInterceptor = new MethodInterceptor();
            setServiceType(doPostInterceptor, ServiceType.SPRING_MVC);



			InstrumentClass servlet = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
			servlet.addInterceptor("doGet", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doGetInterceptor);

			servlet.addInterceptor("doPost", new String[] { "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse" }, doPostInterceptor);

			return servlet.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}

    private void setServiceType(Interceptor interceptor, ServiceType serviceType) {
        if (interceptor instanceof ServiceTypeSupport) {
            ((ServiceTypeSupport)interceptor).setServiceType(serviceType);
        }
    }
}