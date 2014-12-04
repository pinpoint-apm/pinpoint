package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
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
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);

            beforeAddInterceptor(classLoader, protectedDomain, instrumentClass);

            // add constructor
            addConstructorInterceptor(classLoader, protectedDomain, instrumentClass);

            // method
            addMethodInterceptor(classLoader, protectedDomain, instrumentClass);

            return instrumentClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to modifier. caused={}", e.getMessage(), e);
            }
        }

        return null;
    }

    protected void beforeAddInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        // nothing
    }

    protected void addMethodInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException {
        final List<MethodInfo> declaredMethods = instrumentClass.getDeclaredMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        for (MethodInfo method : declaredMethods) {
            try {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterMethodInterceptor");
                instrumentClass.addInterceptor(method.getName(), method.getParameterTypes(), methodInterceptor);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to add method interceptor('not found ...' is jedis compatibility error). caused={}", e.getMessage(), e);
                }
            }
        }

    }

    protected void addConstructorInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException, NotFoundInstrumentException {
        final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterConstructorInterceptor");
        instrumentClass.addConstructorInterceptor(new String[]{"java.lang.String"}, constructorInterceptor);
        instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int" }, constructorInterceptor);
        instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int", "int" }, constructorInterceptor);
    }
}