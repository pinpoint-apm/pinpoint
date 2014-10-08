package com.nhn.pinpoint.profiler.modifier.bloc.handler;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.DedicatedModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author netspider
 */
public class HTTPHandlerModifier extends DedicatedModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HTTPHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/nhncorp/lucy/bloc/handler/HTTPHandler$BlocAdapter";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.bloc.handler.interceptor.ExecuteMethodInterceptor");
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
            aClass.addInterceptor("execute", new String[]{"external.org.apache.coyote.Request", "external.org.apache.coyote.Response"}, interceptor);
            return aClass.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("Bloc.HTTPHandlerModifier fail. Caused:", e.getMessage(), e);
            return null;
        }
    }
}