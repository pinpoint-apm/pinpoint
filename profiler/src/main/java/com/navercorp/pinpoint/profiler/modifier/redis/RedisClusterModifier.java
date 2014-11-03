package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.DeclaredMethodsForce;
import com.nhn.pinpoint.profiler.interceptor.bci.DeclaredMethodsForce.FailMethod;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.interceptor.bci.NotFoundInstrumentException;
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

        byteCodeInstrumentor.checkLibrary(classLoader, className);
        try {
            final InstrumentClass instrumentClass = byteCodeInstrumentor.getClass(className);

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

    protected void beforeAddInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        // nothing
    }

    protected void addMethodInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        final DeclaredMethodsForce declaredMethods = instrumentClass.getDeclaredMethodsForce(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        for (Method method : declaredMethods.getDeclaredMethods()) {
            try {
                final Interceptor methodInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterMethodInterceptor");
                instrumentClass.addInterceptor(method.getMethodName(), method.getMethodParams(), methodInterceptor);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to add method interceptor('not found ...' is jedis compatibility error). caused={}", e.getMessage(), e);
                }
            }
        }

        for (FailMethod method : declaredMethods.getFailDeclaredMethods()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to add method interceptor('not found ...' is jedis compatibility error). caused={}", method.getCaused().getMessage(), method.getCaused());
            }
        }
    }

    protected void addConstructorInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws InstrumentException, NotFoundInstrumentException {
        final Interceptor constructorInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.redis.interceptor.RedisClusterConstructorInterceptor");
        instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String" }, constructorInterceptor);
        instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int" }, constructorInterceptor);
        instrumentClass.addConstructorInterceptor(new String[] { "java.lang.String", "int", "int" }, constructorInterceptor);
    }
}