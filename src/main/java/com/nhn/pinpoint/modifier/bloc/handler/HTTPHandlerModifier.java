package com.nhn.pinpoint.modifier.bloc.handler;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.logging.LoggerFactory;
import com.nhn.pinpoint.modifier.AbstractModifier;

/**
 * @author netspider
 */
public class HTTPHandlerModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(HTTPHandlerModifier.class.getName());

    public HTTPHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/nhncorp/lucy/bloc/handler/HTTPHandler$BlocAdapter";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }

        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.modifier.bloc.handler.ExecuteMethodInterceptor");
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
            aClass.addInterceptor("execute", new String[]{"external.org.apache.coyote.Request", "external.org.apache.coyote.Response"}, interceptor);
            return aClass.toBytecode();
        } catch (InstrumentException e) {
            // TODO log
            return null;
        }
    }
}