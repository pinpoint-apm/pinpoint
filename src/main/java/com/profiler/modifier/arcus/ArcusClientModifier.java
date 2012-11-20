package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.arcus.interceptors.ConstructInterceptor;

/**
 * @author netspider
 */
public class ArcusClientModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(ArcusClientModifier.class.getName());

    public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "net/spy/memcached/protocol/BaseOperationImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__asyncTrace", "__setAsyncTrace", "__getAsyncTrace", "java.lang.Object");
            aClass.addConstructorInterceptor(null, new ConstructInterceptor());

            Interceptor transitionStateInterceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.arcus.interceptors.BaseOperationTransitionStateInterceptor");
            aClass.addInterceptor("transitionState", new String[]{"net.spy.memcached.ops.OperationState"}, transitionStateInterceptor);
            Interceptor cancelInterceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.arcus.interceptors.BaseOperationCancelInterceptor");
            aClass.addInterceptor("cancel", null, cancelInterceptor);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return null;
        }
    }
}