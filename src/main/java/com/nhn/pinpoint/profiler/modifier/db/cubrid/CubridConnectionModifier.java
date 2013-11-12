package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
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

			cubridConnection.addTraceVariable("__databaseInfo", "__setDatabaseInfo", "__getDatabaseInfo", "java.lang.Object");

            Interceptor connectionCloseInterceptor = new ConnectionCloseInterceptor();
            cubridConnection.addScopeInterceptor("close", null, connectionCloseInterceptor, JDBCScope.SCOPE);

            Interceptor statementCreateInterceptor = new StatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("createStatement", null, statementCreateInterceptor, JDBCScope.SCOPE);

            Interceptor preparedStatementCreateInterceptor = new PreparedStatementCreateInterceptor();
            cubridConnection.addScopeInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatementCreateInterceptor, JDBCScope.SCOPE);

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
