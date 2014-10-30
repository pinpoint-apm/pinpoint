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

/**
 * RedisCluster(nBase-ARC client) modifier
 * - trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayServerModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public GatewayServerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/gateway/GatewayServer";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, className);
        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(className);

            // trace destinationId
            instrumentClass.addTraceValue(MapTraceValue.class);

            // method
            final List<MethodInfo> declaredMethods = instrumentClass.getDeclaredMethods();
            for (MethodInfo method : declaredMethods) {
                if (method.getName().equals("getResource")) {
                    final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.GatewayServerMethodInterceptor");
                    instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
                }
            }

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("redis.GatewayServerModifier(nBase-ARC) fail. Target class is " + getTargetClass() + ", Caused " + e.getMessage(), e);
            }
        }

        return null;
    }
}