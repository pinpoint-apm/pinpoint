package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.ScopeDelegateSimpleInterceptor;
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

            Interceptor connectionCloseInterceptor = new ScopeDelegateSimpleInterceptor(new ConnectionCloseInterceptor(), JDBCScope.SCOPE);
            cubridConnection.addInterceptor("close", null, connectionCloseInterceptor);

            Interceptor statementCreateInterceptor = new ScopeDelegateSimpleInterceptor(new StatementCreateInterceptor(), JDBCScope.SCOPE);
            cubridConnection.addInterceptor("createStatement", null, statementCreateInterceptor);

            Interceptor preparedStatementCreateInterceptor = new ScopeDelegateSimpleInterceptor(new PreparedStatementCreateInterceptor(), JDBCScope.SCOPE);
            cubridConnection.addInterceptor("prepareStatement", new String[] { "java.lang.String" }, preparedStatementCreateInterceptor);

            final ProfilerConfig profilerConfig = agent.getProfilerConfig();
            if (profilerConfig.isJdbcProfileCubridSetAutoCommit()) {
                Interceptor setAutoCommit = new ScopeDelegateSimpleInterceptor(new TransactionSetAutoCommitInterceptor(), JDBCScope.SCOPE);
                cubridConnection.addInterceptor("setAutoCommit", new String[] { "boolean" }, setAutoCommit);
            }
            if (profilerConfig.isJdbcProfileCubridCommit()) {
                Interceptor commit = new ScopeDelegateSimpleInterceptor(new TransactionCommitInterceptor(), JDBCScope.SCOPE);
                cubridConnection.addInterceptor("commit", null, commit);
            }
            if (profilerConfig.isJdbcProfileCubridRollback()) {
                Interceptor rollback = new ScopeDelegateSimpleInterceptor(new TransactionRollbackInterceptor(), JDBCScope.SCOPE);
                cubridConnection.addInterceptor("rollback", null, rollback);
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
