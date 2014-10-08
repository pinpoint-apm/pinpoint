package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.DedicatedModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.FrontCacheGetFutureConstructInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author harebox
 */
public class FrontCacheGetFutureModifier extends AbstractModifier {

    protected Logger logger;

    public FrontCacheGetFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__cacheName", "__setCacheName", "__getCacheName", "java.lang.String");
            aClass.addTraceVariable("__cacheKey", "__setCacheKey", "__getCacheKey", "java.lang.String");

            Interceptor frontCacheGetFutureConstructInterceptor = new FrontCacheGetFutureConstructInterceptor();
            aClass.addConstructorInterceptor(new String[]{"net.sf.ehcache.Element"}, frontCacheGetFutureConstructInterceptor);

            SimpleAroundInterceptor frontCacheGetFutureGetInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
            aClass.addScopeInterceptor("get", new String[]{Long.TYPE.toString(), "java.util.concurrent.TimeUnit"}, frontCacheGetFutureGetInterceptor, ArcusScope.SCOPE);
            aClass.addScopeInterceptor("get", new String[]{}, frontCacheGetFutureGetInterceptor, ArcusScope.SCOPE);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public String getTargetClass() {
        return "net/spy/memcached/plugin/FrontCacheGetFuture";
    }
}
