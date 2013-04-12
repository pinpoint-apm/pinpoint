package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import com.profiler.logging.Logger;

import com.profiler.Agent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.logging.LoggerFactory;
import com.profiler.modifier.AbstractModifier;

/**
 * @author netspider
 */
public class ArcusClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(ArcusClientModifier.class.getName());

    public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/ArcusClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            Interceptor setCacheManagerInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.arcus.interceptors.SetCacheManagerInterceptor");
            aClass.addInterceptor("setCacheManager", new String[]{"net.spy.memcached.CacheManager"}, setCacheManagerInterceptor);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn( e.getMessage(), e);
            }
            return null;
        }
    }
}