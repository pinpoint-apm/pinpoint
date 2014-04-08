package com.nhn.pinpoint.profiler.modifier.db.dbcp;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.DataSourceGetConnectionInterceptor;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DBCPBasicDataSourceModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DBCPBasicDataSourceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/commons/dbcp/BasicDataSource";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);

        try {
            InstrumentClass basicDataSource = byteCodeInstrumentor.getClass(javassistClassName);
            Interceptor getConnection0 = new DataSourceGetConnectionInterceptor();
            basicDataSource.addInterceptor("getConnection", null, getConnection0);

            Interceptor getConnection1 = new DataSourceGetConnectionInterceptor();
            basicDataSource.addInterceptor("getConnection", new String[] {"java.lang.String", "java.lang.String"}, getConnection1);

            return basicDataSource.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }


}
