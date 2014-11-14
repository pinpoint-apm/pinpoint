package com.nhn.pinpoint.profiler.modifier.db.jtds;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.Scope;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.DriverConnectInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class JtdsDriverModifier extends AbstractModifier {

//    oracle.jdbc.driver

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public JtdsDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "net/sourceforge/jtds/jdbc/Driver";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        try {
            InstrumentClass jtdsDriver = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            final Scope scope = byteCodeInstrumentor.getScope(JtdsScope.SCOPE_NAME);
            Interceptor createConnection = new DriverConnectInterceptor(scope);
            String[] params = new String[]{ "java.lang.String", "java.util.Properties" };
            jtdsDriver.addInterceptor("connect", params, createConnection);

            if (logger.isInfoEnabled()) {
                logger.info("{} class is converted.", javassistClassName);
            }

            return jtdsDriver.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }
    }
}
