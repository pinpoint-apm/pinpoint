package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;

/**
 * jedis(redis client) pipeline modifier
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineModifier extends JedisPipelineBaseModifier {

    public JedisPipelineModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/Pipeline";
    }

    protected void beforeAddInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        // trace endPoint
        instrumentClass.addTraceValue(MapTraceValue.class);

        addConstructorInterceptor(classLoader, protectedDomain, instrumentClass);
        addSetClientMethodInterceptor(classLoader, protectedDomain, instrumentClass);
    }

    private void addSetClientMethodInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        for (MethodInfo method : instrumentClass.getDeclaredMethods()) {
            if (method.getName().equals("setClient")) {
                // jedis 2.x
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineSetClientMethodInterceptor");
                instrumentClass.addInterceptor("setClient", new String[] { "redis.clients.jedis.Client" }, methodInterceptor);
            }
        }
    }

    private void addConstructorInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.JedisPipelineConstructorInterceptor");
        try {
            // jedis 1.x
            instrumentClass.addConstructorInterceptor(new String[] { "redis.clients.jedis.Client" }, constructorInterceptor);
        } catch (Exception e) {
            // backward compatibility error
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to add constructor interceptor(only jedis 1.x). caused={}", e.getMessage(), e);
            }
        }
    }
}