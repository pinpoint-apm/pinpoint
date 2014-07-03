package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

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

/**
 * @author emeroad
 */
public class CubridConnectionModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public CubridConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDConnection";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass cubridConnection = byteCodeInstrumentor.getClass(javassistClassName);

            cubridConnection.addTraceValue(DatabaseInfoTraceValue.class);

            Interceptor connectionCloseInterceptor = new ConnectionCloseInterceptor();
            cubridConnection.addScopeInterceptor("close", null, connectionCloseInterceptor, JDBCScope.SCOPE);


            Interceptor statementCreateInterceptor1 = new StatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("createStatement", null, statementCreateInterceptor1, JDBCScope.SCOPE);

            Interceptor statementCreateInterceptor2 = new StatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("createStatement", new String[]{"int", "int"}, statementCreateInterceptor2, JDBCScope.SCOPE);

            Interceptor statementCreateInterceptor3 = new StatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("createStatement", new String[]{"int", "int", "int"}, statementCreateInterceptor3, JDBCScope.SCOPE);


            Interceptor preparedStatementCreateInterceptor1 = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatementCreateInterceptor1, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor2 = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int"}, preparedStatementCreateInterceptor2, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor3 = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int[]"}, preparedStatementCreateInterceptor3, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor4 = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "java.lang.String[]"}, preparedStatementCreateInterceptor4, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor5 = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int"}, preparedStatementCreateInterceptor5, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor6 = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int", "int"}, preparedStatementCreateInterceptor6, JDBCScope.SCOPE);

            final ProfilerConfig profilerConfig = agent.getProfilerConfig();
            if (profilerConfig.isJdbcProfileCubridSetAutoCommit()) {
                Interceptor setAutoCommit = new TransactionSetAutoCommitInterceptor();
                cubridConnection.addScopeInterceptor("setAutoCommit", new String[]{"boolean"}, setAutoCommit, JDBCScope.SCOPE);
            }
            if (profilerConfig.isJdbcProfileCubridCommit()) {
                Interceptor commit = new TransactionCommitInterceptor();
                cubridConnection.addScopeInterceptor("commit", null, commit, JDBCScope.SCOPE);
            }
            if (profilerConfig.isJdbcProfileCubridRollback()) {
                Interceptor rollback = new TransactionRollbackInterceptor();
                cubridConnection.addScopeInterceptor("rollback", null, rollback, JDBCScope.SCOPE);
            }

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return cubridConnection.toBytecode();
		} catch (InstrumentException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
			}
			return null;
		}
	}
}
