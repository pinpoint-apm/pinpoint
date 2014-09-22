package com.nhn.pinpoint.profiler.modifier.tomcat;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.LifeCycleEventListener;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cowboy93, netspider
 * @author hyungil.jeong
 */
public class StandardServiceModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public StandardServiceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardService";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        logger.info("Modifying. {}", javassistClassName);
        try {
            InstrumentClass standardService = byteCodeInstrumentor.getClass(javassistClassName);
            LifeCycleEventListener lifeCycleEventListener = new LifeCycleEventListener(agent);
            
            Interceptor standardServiceStartInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                    "com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.StandardServiceStartInterceptor",
                    new Object[] { lifeCycleEventListener }, new Class[] { LifeCycleEventListener.class });
            Interceptor standardServiceStopInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                    "com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.StandardServiceStopInterceptor",
                    new Object[] { lifeCycleEventListener }, new Class[] { LifeCycleEventListener.class });

            boolean isHooked = false;
            // Tomcat 6 - org.apache.catalina.core.StandardService.start(), stop()
            if (isHooked = (standardService.hasDeclaredMethod("start", null) && standardService.hasDeclaredMethod("stop", null))) {
                standardService.addInterceptor("start", null, standardServiceStartInterceptor);
                standardService.addInterceptor("stop", null, standardServiceStopInterceptor);
            }
            // Tomcat 7, 8 - org.apache.catalina.core.StandardService.startInternal(), stopInternal()
            else if (isHooked = (standardService.hasDeclaredMethod("startInternal", null) && standardService.hasDeclaredMethod("stopInternal", null))) {
                standardService.addInterceptor("startInternal", null, standardServiceStartInterceptor);
                standardService.addInterceptor("stopInternal", null, standardServiceStopInterceptor);
            }
            
            if (isHooked) {
                logger.info("{} class is converted.", javassistClassName);
            } else {
                logger.warn("{} class not converted - start() or startInternal() method not found.", javassistClassName);
            }
            return standardService.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("modify fail. Cause:" + e.getMessage(), e);
            }
        }
        return null;
    }
}
