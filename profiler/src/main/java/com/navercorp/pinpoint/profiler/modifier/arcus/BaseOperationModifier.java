package com.nhn.pinpoint.profiler.modifier.arcus;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author netspider
 */
public class BaseOperationModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BaseOperationModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/protocol/BaseOperationImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__serviceCode", "__setServiceCode", "__getServiceCode", "java.lang.String");

            /* Do not intercept
            aClass.addTraceVariable("__asyncTrace", "__setAsyncTrace", "__getAsyncTrace", "java.lang.Object");

            aClass.addConstructorInterceptor(null, new BaseOperationConstructInterceptor());

            Interceptor transitionStateInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.BaseOperationTransitionStateInterceptor");
            aClass.addInterceptor("transitionState", new String[]{"net.spy.memcached.ops.OperationState"}, transitionStateInterceptor, Type.before);

            Interceptor cancelInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.BaseOperationCancelInterceptor");
            aClass.addInterceptor("cancel", null, cancelInterceptor, Type.after);
            */

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }
}