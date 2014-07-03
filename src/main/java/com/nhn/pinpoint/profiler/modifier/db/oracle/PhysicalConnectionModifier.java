package com.nhn.pinpoint.profiler.modifier.db.oracle;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class PhysicalConnectionModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PhysicalConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        // T4C , T2C (OCI: T2C를 상속해서 만듬) 의 최상위 구현체가 있으나,
        // 해당 클래스는 PhysicalConnection 를 base로 하므로 PhysicalConnection 만 해도 될듯하다.
        return "oracle/jdbc/driver/PhysicalConnection";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }
        this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
        try {
            InstrumentClass oracleConnection = byteCodeInstrumentor.getClass(javassistClassName);


            oracleConnection.addTraceValue(DatabaseInfoTraceValue.class);

            // 해당 Interceptor를 공통클래스 만들경우 system에 로드해야 된다.
//            Interceptor createConnection  = new ConnectionCreateInterceptor();
//            String[] params = new String[] {
//                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
//            };
//            mysqlConnection.addInterceptor("getInstance", params, createConnection);


            Interceptor closeConnection = new ConnectionCloseInterceptor();
            oracleConnection.addScopeInterceptor("close", null, closeConnection, JDBCScope.SCOPE);


            Interceptor statementCreateInterceptor1 = new StatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("createStatement", null, statementCreateInterceptor1, JDBCScope.SCOPE);

            Interceptor statementCreateInterceptor2 = new StatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("createStatement", new String[]{"int", "int"}, statementCreateInterceptor2, JDBCScope.SCOPE);

            Interceptor statementCreateInterceptor3 = new StatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("createStatement", new String[]{"int", "int", "int"}, statementCreateInterceptor3, JDBCScope.SCOPE);


            Interceptor preparedStatementCreateInterceptor1 = new PreparedStatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatementCreateInterceptor1, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor2 = new PreparedStatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int"}, preparedStatementCreateInterceptor2, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor3 = new PreparedStatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int[]"}, preparedStatementCreateInterceptor3, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor4 = new PreparedStatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "java.lang.String[]"}, preparedStatementCreateInterceptor4, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor5 = new PreparedStatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int"}, preparedStatementCreateInterceptor5, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor6 = new PreparedStatementCreateInterceptor();
            oracleConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int", "int"}, preparedStatementCreateInterceptor6, JDBCScope.SCOPE);

            final ProfilerConfig profilerConfig = agent.getProfilerConfig();
            if (profilerConfig.isJdbcProfileOracleSetAutoCommit()) {
                Interceptor setAutocommit = new TransactionSetAutoCommitInterceptor();
                oracleConnection.addScopeInterceptor("setAutoCommit", new String[]{"boolean"}, setAutocommit, JDBCScope.SCOPE);
            }
            if (profilerConfig.isJdbcProfileOracleCommit()) {
                Interceptor commit = new TransactionCommitInterceptor();
                oracleConnection.addScopeInterceptor("commit", null, commit, JDBCScope.SCOPE);
            }
            if (profilerConfig.isJdbcProfileOracleRollback()) {
                Interceptor rollback = new TransactionRollbackInterceptor();
                oracleConnection.addScopeInterceptor("rollback", null, rollback, JDBCScope.SCOPE);
            }

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return oracleConnection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }


}
