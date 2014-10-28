package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.Method;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

/**
 * Gateway(nBase-ARC client) modifier
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public GatewayModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/gateway/Gateway";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(className);

            // trace destinationId
            instrumentClass.addTraceValue(MapTraceValue.class);
            final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.GatewayConstructorInterceptor");
            instrumentClass.addConstructorInterceptor(new String[] { "com.nhncorp.redis.cluster.gateway.GatewayConfig" }, constructorInterceptor);

            // method
            final List<Method> declaredMethods = instrumentClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.getName().equals("getServer")) {
                    final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.GatewayMethodInterceptor");
                    instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
                }
            }

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("redis.GatewayModifier(nBase-ARC) fail. Target class is " + getTargetClass() + ", Caused " + e.getMessage(), e);
            }
        }

        return null;
    }
}