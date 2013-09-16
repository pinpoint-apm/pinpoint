package com.nhn.pinpoint.profiler.modifier.arcus;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.modifier.MultipleModifier;
import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Type;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */
public class FutureModifier extends AbstractModifier implements MultipleModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return null;
    }
    
    public String[] getTargetClasses() {
    	return new String[] {
    		"net/spy/memcached/internal/OperationFuture",
    		"net/spy/memcached/internal/GetFuture",
    		"net/spy/memcached/internal/CollectionFuture",
    		"net/spy/memcached/internal/ImmediateFuture"
    	};
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__operation", "__setOperation", "__getOperation", "net.spy.memcached.ops.Operation");

            Interceptor futureSetOperationInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.FutureSetOperationInterceptor");
            aClass.addInterceptor("setOperation", new String[]{"net.spy.memcached.ops.Operation"}, futureSetOperationInterceptor, Type.before);
            
            Interceptor futureGetInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.arcus.interceptor.FutureGetInterceptor");
            aClass.addInterceptor("get", new String[]{Long.TYPE.toString(), "java.util.concurrent.TimeUnit"}, futureGetInterceptor, Type.around);
            
            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return null;
        }
    }
}