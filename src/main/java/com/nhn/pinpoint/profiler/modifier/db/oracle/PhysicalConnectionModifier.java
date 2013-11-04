package com.nhn.pinpoint.profiler.modifier.db.oracle;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateSimpleInterceptor;
import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.Type;
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


            oracleConnection.addTraceVariable("__databaseInfo", "__setDatabaseInfo", "__getDatabaseInfo", "java.lang.Object");

            // 해당 Interceptor를 공통클래스 만들경우 system에 로드해야 된다.
//            Interceptor createConnection  = new ConnectionCreateInterceptor();
//            String[] params = new String[] {
//                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
//            };
//            mysqlConnection.addInterceptor("getInstance", params, createConnection);


            Interceptor closeConnection = new ScopeDelegateSimpleInterceptor(new ConnectionCloseInterceptor(), JDBCScope.SCOPE);
            oracleConnection.addInterceptor("close", null, closeConnection);

            Interceptor createStatement = new ScopeDelegateSimpleInterceptor(new StatementCreateInterceptor(), JDBCScope.SCOPE);
            oracleConnection.addInterceptor("createStatement", null, createStatement);

            Interceptor preparedStatement = new ScopeDelegateSimpleInterceptor(new PreparedStatementCreateInterceptor(), JDBCScope.SCOPE);
            oracleConnection.addInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatement);

            final ProfilerConfig profilerConfig = agent.getProfilerConfig();
            if (profilerConfig.isJdbcProfileOracleSetAutoCommit()) {
                Interceptor setAutocommit = new ScopeDelegateSimpleInterceptor(new TransactionSetAutoCommitInterceptor(), JDBCScope.SCOPE);
                oracleConnection.addInterceptor("setAutoCommit", new String[]{"boolean"}, setAutocommit);
            }
            if (profilerConfig.isJdbcProfileOracleCommit()) {
                Interceptor commit = new ScopeDelegateSimpleInterceptor(new TransactionCommitInterceptor(), JDBCScope.SCOPE);
                oracleConnection.addInterceptor("commit", null, commit);
            }
            if (profilerConfig.isJdbcProfileOracleRollback()) {
                Interceptor rollback = new ScopeDelegateSimpleInterceptor(new TransactionRollbackInterceptor(), JDBCScope.SCOPE);
                oracleConnection.addInterceptor("rollback", null, rollback);
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
