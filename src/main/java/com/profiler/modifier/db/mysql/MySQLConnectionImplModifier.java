package com.profiler.modifier.db.mysql;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.interceptor.bci.InstrumentException;
import com.profiler.modifier.db.interceptor.*;

import com.profiler.modifier.AbstractModifier;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnectionImplModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(MySQLConnectionImplModifier.class.getName());

    public MySQLConnectionImplModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/ConnectionImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }
        checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);


            mysqlConnection.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");

            // 해당 Interceptor를 공통클래스 만들경우 system에 로드해야 된다.
//            Interceptor createConnection  = new ConnectionCreateInterceptor();
//            String[] params = new String[] {
//                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
//            };
//            mysqlConnection.addInterceptor("getInstance", params, createConnection);


            Interceptor closeConnection = new ConnectionCloseInterceptor();
            mysqlConnection.addInterceptor("close", null, closeConnection);

            Interceptor createStatement = new StatementCreateInterceptor();
            mysqlConnection.addInterceptor("createStatement", null, createStatement);


            Interceptor preparedStatement = new PreparedStatementCreateInterceptor();
            mysqlConnection.addInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatement);


            Interceptor transaction = new TransactionInterceptor();
            int interceptorId = mysqlConnection.addInterceptor("setAutoCommit", new String[]{"boolean"}, transaction);
            mysqlConnection.reuseInterceptor("commit", null, interceptorId);
            mysqlConnection.reuseInterceptor("rollback", null, interceptorId);

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
