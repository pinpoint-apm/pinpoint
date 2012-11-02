package com.profiler.modifier.db.mysql;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.db.interceptor.*;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MySQLNonRegisteringDriverModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MySQLConnectionImplModifier.class.getName());

    public MySQLNonRegisteringDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/NonRegisteringDriver";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }
        checkLibrary(classLoader, javassistClassName);
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
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
            }
            return null;
        }
    }
}
