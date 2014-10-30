package com.nhn.pinpoint.profiler.modifier.arcus;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;
import org.slf4j.Logger;


/**
 * @author emeroad
 */
public abstract class AbstractFutureModifier extends AbstractModifier  {

    protected Logger logger;

    public AbstractFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__operation", "__setOperation", "__getOperation", "net.spy.memcached.ops.Operation");

            Interceptor futureSetOperationInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.FutureSetOperationInterceptor");
            aClass.addInterceptor("setOperation", new String[]{"net.spy.memcached.ops.Operation"}, futureSetOperationInterceptor);
            
            SimpleAroundInterceptor futureGetInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.FutureGetInterceptor");

            aClass.addScopeInterceptor("get", new String[]{Long.TYPE.toString(), "java.util.concurrent.TimeUnit"}, futureGetInterceptor, ArcusScope.SCOPE);
            
            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }
}