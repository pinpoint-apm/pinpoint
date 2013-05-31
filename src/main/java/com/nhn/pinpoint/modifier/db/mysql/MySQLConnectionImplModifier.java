package com.nhn.pinpoint.modifier.db.mysql;

import com.nhn.pinpoint.Agent;
import com.nhn.pinpoint.interceptor.Interceptor;
import com.nhn.pinpoint.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.interceptor.bci.Type;
import com.nhn.pinpoint.modifier.db.interceptor.*;

import com.nhn.pinpoint.modifier.AbstractModifier;

import java.security.ProtectionDomain;
import com.nhn.pinpoint.logging.Logger;
import com.nhn.pinpoint.logging.LoggerFactory;

public class MySQLConnectionImplModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLConnectionImplModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "com/mysql/jdbc/ConnectionImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
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
            mysqlConnection.addInterceptor("close", null, closeConnection, Type.before);

            Interceptor createStatement = new StatementCreateInterceptor();
            mysqlConnection.addInterceptor("createStatement", null, createStatement, Type.after);


            Interceptor preparedStatement = new PreparedStatementCreateInterceptor();
            mysqlConnection.addInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatement);


            Interceptor setAutocommit = new TransactionInterceptor();
            mysqlConnection.addInterceptor("setAutoCommit", new String[]{"boolean"}, setAutocommit);
            Interceptor commit = new TransactionInterceptor();
            mysqlConnection.addInterceptor("commit", null, commit);
            Interceptor rollback = new TransactionInterceptor();
            mysqlConnection.addInterceptor("rollback", null, rollback);

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
