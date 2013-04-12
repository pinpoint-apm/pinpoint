package com.profiler.modifier.tomcat;

import java.security.ProtectionDomain;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

import com.profiler.Agent;
import com.profiler.LifeCycleEventListener;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.tomcat.interceptors.StandardServiceStartInterceptor;
import com.profiler.modifier.tomcat.interceptors.StandardServiceStopInterceptor;

import com.profiler.modifier.AbstractModifier;

/**
 * When org.apache.catalina.core.StandardService class is loaded in ClassLoader,
 * this class modifies methods.
 *
 * @author cowboy93, netspider
 */
public class TomcatStandardServiceModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(TomcatStandardServiceModifier.class);


    public TomcatStandardServiceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardService";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass standardService = byteCodeInstrumentor.getClass(javassistClassName);

            LifeCycleEventListener lifeCycleEventListener = new LifeCycleEventListener(agent);
            StandardServiceStartInterceptor start = new StandardServiceStartInterceptor(lifeCycleEventListener);
            standardService.addInterceptor("start", null, start);

            StandardServiceStopInterceptor stop = new StandardServiceStopInterceptor(lifeCycleEventListener);
            standardService.addInterceptor("stop", null, stop);

            return standardService.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }
}
