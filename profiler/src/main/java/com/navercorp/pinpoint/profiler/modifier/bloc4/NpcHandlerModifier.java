package com.nhn.pinpoint.profiler.modifier.bloc4;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

public class NpcHandlerModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public NpcHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        
        byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        
        try {
            InstrumentClass npcHandler = byteCodeInstrumentor.getClass(javassistClassName);
            Interceptor messageReceivedInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.bloc4.interceptor.MessageReceivedInterceptor");
            npcHandler.addInterceptor("messageReceived", new String[] {"external.org.apache.mina.common.IoFilter$NextFilter", "external.org.apache.mina.common.IoSession", "java.lang.Object"}, messageReceivedInterceptor);

            return npcHandler.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("NpcHandlerModifier fail. Caused:", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/lucy/bloc/npc/handler/NpcHandler";
    }

}
