package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.redis.filter.NameBasedMethodFilter;
import com.nhn.pinpoint.profiler.modifier.redis.filter.RedisClusterMethodNames;

/**
 * RedisCluster(nBase-ARC client) modifier
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RedisClusterModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/RedisCluster";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(className);

            // trace destinationId, endPoint
            instrumentClass.addTraceValue(MapTraceValue.class);

            final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterConstructorInterceptor");
            instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String" }, constructorInterceptor);
            instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int" }, constructorInterceptor);
            instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int", "int" }, constructorInterceptor);

            // method
            final List<MethodInfo> declaredMethods = instrumentClass.getDeclaredMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
            for (MethodInfo method : declaredMethods) {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterMethodInterceptor");
                instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
            }

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("redis.RedisClusterModifier(nBase-ARC) fail. Target class is " + getTargetClass() + ", Caused " + e.getMessage(), e);
            }
        }

        return null;
    }
}