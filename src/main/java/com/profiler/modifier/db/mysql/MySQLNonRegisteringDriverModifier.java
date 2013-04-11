package com.profiler.modifier.db.mysql;

import com.profiler.Agent;
import com.profiler.DefaultAgent;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.db.interceptor.*;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;

/**
 *
 */
public class MySQLNonRegisteringDriverModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(MySQLConnectionImplModifier.class.getName());

    public MySQLNonRegisteringDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/NonRegisteringDriver";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);


            Interceptor createConnection = new DriverConnectInterceptor();
            String[] params = new String[]{
                    "java.lang.String", "java.util.Properties"
            };
            mysqlConnection.addInterceptor("connect", params, createConnection);

            printClassConvertComplete(javassistClassName);

            return mysqlConnection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }
    }
}
