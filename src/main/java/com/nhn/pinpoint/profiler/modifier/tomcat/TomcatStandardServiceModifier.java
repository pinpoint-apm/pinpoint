package com.nhn.pinpoint.profiler.modifier.tomcat;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.LifeCycleEventListener;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.StandardServiceStartInterceptor;
import com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.StandardServiceStopInterceptor;

import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 *
 * @author cowboy93, netspider
 */
public class TomcatStandardServiceModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public TomcatStandardServiceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardService";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
			InstrumentClass standardService = byteCodeInstrumentor.getClass(javassistClassName);

			LifeCycleEventListener lifeCycleEventListener = new LifeCycleEventListener(agent);
			
			// Tomcat 6
			if (standardService.hasDeclaredMethod("start", null) && standardService.hasDeclaredMethod("stop", null)) {
				standardService.addInterceptor("start", null, new StandardServiceStartInterceptor(lifeCycleEventListener));
				standardService.addInterceptor("stop", null, new StandardServiceStopInterceptor(lifeCycleEventListener));
			}
			// Tomcat 7
			else if (standardService.hasDeclaredMethod("startInternal", null) && standardService.hasDeclaredMethod("stopInternal", null)) {
				standardService.addInterceptor("startInternal", null, new StandardServiceStartInterceptor(lifeCycleEventListener));
				standardService.addInterceptor("stopInternal", null, new StandardServiceStopInterceptor(lifeCycleEventListener));
			}
			
            return standardService.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }
}
