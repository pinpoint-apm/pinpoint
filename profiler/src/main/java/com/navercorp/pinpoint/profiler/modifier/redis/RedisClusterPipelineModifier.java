package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.redis.filter.NameBasedMethodFilter;
import com.nhn.pinpoint.profiler.modifier.redis.filter.RedisClusterPipelineMethodNames;

/**
 * RedisCluster(nBase-ARC client) pipeline modifier
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RedisClusterPipelineModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/pipeline/RedisClusterPipeline";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, className);
        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(className);

            // trace destinationId, endPoint
            instrumentClass.addTraceValue(MapTraceValue.class);
            
            final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterPipelineConstructorInterceptor");
            try {
                instrumentClass.addConstructorInterceptor(new String[] { "com.nhncorp.redis.cluster.gateway.GatewayServer" }, constructorInterceptor);
            } catch (Exception ignored) {
                // backward compatibility error
            }

            // method
            final List<Method> declaredMethods = instrumentClass.getDeclaredMethods(new NameBasedMethodFilter(RedisClusterPipelineMethodNames.get()));
            for (Method method : declaredMethods) {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterPipelineMethodInterceptor");
                instrumentClass.addInterceptor(method.getMethodName(), method.getMethodParams(), methodInterceptor);
            }

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("redis.RedisClusterPipelineModifier(nBase-ARC) fail. Target class is " + getTargetClass() + ", Caused " + e.getMessage(), e);
            }
        }

        return null;
    }
}