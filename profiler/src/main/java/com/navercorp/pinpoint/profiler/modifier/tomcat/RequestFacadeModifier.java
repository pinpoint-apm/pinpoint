package com.nhn.pinpoint.profiler.modifier.tomcat;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class RequestFacadeModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public RequestFacadeModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "org/apache/catalina/connector/RequestFacade";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

		try {
			InstrumentClass requestFacade = byteCodeInstrumentor.getClass(javassistClassName);
			requestFacade.weaving("com.nhn.pinpoint.profiler.modifier.tomcat.aspect.RequestFacadeAspect");
			return requestFacade.toBytecode();
		} catch (InstrumentException e) {
			logger.warn("modify fail. Cause:" + e.getMessage(), e);
			return null;
		}
	}
}