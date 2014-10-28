package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.ParameterExtractorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ArcusScope;
import com.nhn.pinpoint.profiler.modifier.arcus.interceptor.IndexParameterExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author harebox
 */
public class FrontCacheMemcachedClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(FrontCacheMemcachedClientModifier.class.getName());

    public FrontCacheMemcachedClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName,
                         ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            String[] args = {"java.lang.String", "java.util.concurrent.Future", Long.TYPE.toString()};
            if (!checkCompatibility(aClass, args)) {
                return null;
            }

            // 모든 public 메소드에 ApiInterceptor를 적용한다.
            final List<MethodInfo> declaredMethods = aClass.getDeclaredMethods(new FrontCacheMemcachedMethodFilter());

            for (MethodInfo method : declaredMethods) {
                SimpleAroundInterceptor apiInterceptor = (SimpleAroundInterceptor) byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.ApiInterceptor");
                if (agent.getProfilerConfig().isMemcachedKeyTrace()) {
                    final int index = ParameterUtils.findFirstString(method, 3);
                    if (index != -1) {
                        ((ParameterExtractorSupport) apiInterceptor).setParameterExtractor(new IndexParameterExtractor(index));
                    }
                }
                aClass.addScopeInterceptor(method.getName(), method.getParameterTypes(), apiInterceptor, ArcusScope.SCOPE);
            }
            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }

    private boolean checkCompatibility(InstrumentClass aClass, String[] args) {
        final boolean putFrontCache = aClass.hasDeclaredMethod("putFrontCache", args);
        if (!putFrontCache) {
            logger.warn("putFrontCache() not found. skip FrontCacheMemcachedClientModifier");
        }
        return putFrontCache;
    }

    public String getTargetClass() {
        return "net/spy/memcached/plugin/FrontCacheMemcachedClient";
    }
}