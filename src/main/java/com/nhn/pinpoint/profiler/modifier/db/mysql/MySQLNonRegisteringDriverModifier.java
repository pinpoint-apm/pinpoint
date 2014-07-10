package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.modifier.db.interceptor.DriverConnectInterceptor;
import com.nhn.pinpoint.profiler.util.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class MySQLNonRegisteringDriverModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLNonRegisteringDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/NonRegisteringDriver";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);

            final Scope scope = byteCodeInstrumentor.getScope(MYSQLScope.SCOPE_NAME);
            Interceptor createConnection = new DriverConnectInterceptor(false, scope);
            String[] params = new String[]{
                    "java.lang.String", "java.util.Properties"
            };
//            Driver에서는 scopeInterceptor를 걸면안된다. trace thread가 아닌곳에서 connection이 생성될수 있다.
            mysqlConnection.addInterceptor("connect", params, createConnection);

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return mysqlConnection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }
}
