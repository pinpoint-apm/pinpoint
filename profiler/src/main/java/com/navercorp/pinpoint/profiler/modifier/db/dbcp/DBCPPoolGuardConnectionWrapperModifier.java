package com.nhn.pinpoint.profiler.modifier.db.dbcp;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.DataSourceCloseInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class DBCPPoolGuardConnectionWrapperModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DBCPPoolGuardConnectionWrapperModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/commons/dbcp/PoolingDataSource$PoolGuardConnectionWrapper";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        return changeMethod(classLoader, javassistClassName, classFileBuffer);
    }

    private byte[] changeMethod(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {

        try {
            InstrumentClass wrapper = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            Interceptor close = new DataSourceCloseInterceptor();
            wrapper.addScopeInterceptor("close", null, close, DBCPScope.SCOPE_NAME);

            return wrapper.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

}
