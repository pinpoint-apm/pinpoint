package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.redis.filter.JedisPipelineMethodNames;
import com.nhn.pinpoint.profiler.modifier.redis.filter.NameBasedMethodFilter;

/**
 * jedis(redis client) pipeline modifier
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public JedisPipelineModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/Pipeline";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(className);

            // trace host & port
            instrumentClass.addTraceValue(ObjectTraceValue.class);
            final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineConstructorInterceptor");
            try {
                // jedis 1.x
                instrumentClass.addConstructorInterceptor(new String[] { "redis.clients.jedis.Client" }, constructorInterceptor);
            } catch (Exception ignored) {
                // backward compatibility error
            }

            for (Method method : instrumentClass.getDeclaredMethods()) {
                if (method.getMethodName().equals("setClient")) {
                    // jedis 2.x
                    final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineSetClientMethodInterceptor");
                    instrumentClass.addInterceptor("setClient", new String[] { "redis.clients.jedis.Client" }, methodInterceptor);
                }
            }

            // method
            final List<Method> declaredMethods = instrumentClass.getDeclaredMethods(new NameBasedMethodFilter(JedisPipelineMethodNames.get()));
            for (Method method : declaredMethods) {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineMethodInterceptor");
                instrumentClass.addInterceptor(method.getMethodName(), method.getMethodParams(), methodInterceptor);
            }

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("redis.JedisPipelineModifier(Jedis) fail. Target class is " + getTargetClass() + ". Caused " + e.getMessage(), e);
            }
        }

        return null;
    }

}
