package com.nhn.pinpoint.modifier.arcus;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.interceptor.bci.Type;
import com.nhn.pinpoint.profiler.logging.Logger;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.modifier.AbstractModifier;

/**
 * @author netspider
 */
public class MemcachedClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(MemcachedClientModifier.class.getName());

    public MemcachedClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/spy/memcached/MemcachedClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__serviceCode", "__setServiceCode", "__getServiceCode", "java.lang.String");

            Interceptor addOpInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.arcus.interceptors.AddOpInterceptor");
            aClass.addInterceptor("addOp", new String[]{"java.lang.String", "net.spy.memcached.ops.Operation"}, addOpInterceptor, Type.before);

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn( e.getMessage(), e);
            }
            return null;
        }
    }
}