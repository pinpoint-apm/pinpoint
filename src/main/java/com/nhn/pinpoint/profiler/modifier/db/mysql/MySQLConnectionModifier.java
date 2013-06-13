package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.Type;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.ConnectionCloseInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.PreparedStatementCreateInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementCreateInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.TransactionInterceptor;

import java.security.ProtectionDomain;

/**
 *
 */
public class MySQLConnectionModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        // mysql의 과거버전의 경우 Connection class에 직접 구현이 되어있다.
        return "com/mysql/jdbc/Connection";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. " + javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);
            if (mysqlConnection.isInterface()) {
                // 최신버전의 mysql dirver를 사용했을 경우의 호환성 작업.
                return null;
            }


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

