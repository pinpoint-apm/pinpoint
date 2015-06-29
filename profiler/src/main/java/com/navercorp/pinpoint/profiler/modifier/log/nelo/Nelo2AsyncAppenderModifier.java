package com.navercorp.pinpoint.profiler.modifier.log.nelo;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

public class Nelo2AsyncAppenderModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Nelo2AsyncAppenderModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    
    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        
        try {
            InstrumentClass nelo2AsyncAppender = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            
            if (!nelo2AsyncAppender.hasDeclaredMethod("append", new String[]{"org.apache.log4j.spi.LoggingEvent"})) {
                nelo2AsyncAppender.addDelegatorMethod("append", new String[]{"org.apache.log4j.spi.LoggingEvent"});
            }
            
            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.log.nelo.interceptor.AppenderInterceptor");
            nelo2AsyncAppender.addInterceptor("append", new String[]{"org.apache.log4j.spi.LoggingEvent"}, interceptor);
            
            return nelo2AsyncAppender.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher("com/nhncorp/nelo2/log4j/Nelo2AsyncAppender");
    }
}
