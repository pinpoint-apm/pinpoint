package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;

import com.profiler.interceptor.bci.Type;
import com.profiler.logging.Logger;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.arcus.interceptors.BaseOperationConstructInterceptor;

/**
 * @author netspider
 */
public class BaseOperationModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(BaseOperationModifier.class.getName());

    public BaseOperationModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/protocol/BaseOperationImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__serviceCode", "__setServiceCode", "__getServiceCode", "java.lang.String");

            aClass.addTraceVariable("__asyncTrace", "__setAsyncTrace", "__getAsyncTrace", "java.lang.Object");

            aClass.addConstructorInterceptor(null, new BaseOperationConstructInterceptor());

            Interceptor transitionStateInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.arcus.interceptors.BaseOperationTransitionStateInterceptor");
            aClass.addInterceptor("transitionState", new String[]{"net.spy.memcached.ops.OperationState"}, transitionStateInterceptor, Type.before);

            Interceptor cancelInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.arcus.interceptors.BaseOperationCancelInterceptor");
            aClass.addInterceptor("cancel", null, cancelInterceptor, Type.after);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn( e.getMessage(), e);
            }
            return null;
        }
    }
}