/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.modifier.db.mssql;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.Scopeable;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.ConnectionCloseInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.PreparedStatementCreateInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.StatementCreateInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.TransactionCommitInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.TransactionRollbackInterceptor;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.TransactionSetAutoCommitInterceptor;

/**
 *
 * @author Barney Kim
 */
public abstract class MsSqlConnectionModifier extends AbstractModifier implements Scopeable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * @param byteCodeInstrumentor
     * @param agent
     */
    public MsSqlConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.profiler.modifier.Modifier#modify(java.lang.ClassLoader, java.lang.String, java.security.ProtectionDomain, byte[])
     */
    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", className);
        }
        try {
            InstrumentClass connection = byteCodeInstrumentor.getClass(classLoader, className, classFileBuffer);
            connection.addTraceValue(DatabaseInfoTraceValue.class);

            Interceptor closeConnection = new ConnectionCloseInterceptor();
            connection.addScopeInterceptor("close", null, closeConnection, getScopeName());

            Interceptor statementCreateInterceptor1 = new StatementCreateInterceptor();
            connection.addScopeInterceptor("createStatement", null, statementCreateInterceptor1, getScopeName());

            Interceptor statementCreateInterceptor2 = new StatementCreateInterceptor();
            connection.addScopeInterceptor("createStatement", new String[] { "int", "int" }, statementCreateInterceptor2, getScopeName());

            Interceptor statementCreateInterceptor3 = new StatementCreateInterceptor();
            connection.addScopeInterceptor("createStatement", new String[] { "int", "int", "int" }, statementCreateInterceptor3, getScopeName());

            Interceptor preparedStatementCreateInterceptor1 = new PreparedStatementCreateInterceptor();
            connection.addScopeInterceptor("prepareStatement", new String[] { "java.lang.String" }, preparedStatementCreateInterceptor1, getScopeName());

            Interceptor preparedStatementCreateInterceptor2 = new PreparedStatementCreateInterceptor();
            connection.addScopeInterceptor("prepareStatement", new String[] { "java.lang.String", "int" }, preparedStatementCreateInterceptor2, getScopeName());

            Interceptor preparedStatementCreateInterceptor3 = new PreparedStatementCreateInterceptor();
            connection.addScopeInterceptor("prepareStatement", new String[] { "java.lang.String", "int[]" }, preparedStatementCreateInterceptor3, getScopeName());

            Interceptor preparedStatementCreateInterceptor4 = new PreparedStatementCreateInterceptor();
            connection.addScopeInterceptor("prepareStatement", new String[] { "java.lang.String", "java.lang.String[]" }, preparedStatementCreateInterceptor4, getScopeName());

            Interceptor preparedStatementCreateInterceptor5 = new PreparedStatementCreateInterceptor();
            connection.addScopeInterceptor("prepareStatement", new String[] { "java.lang.String", "int", "int" }, preparedStatementCreateInterceptor5, getScopeName());

            Interceptor preparedStatementCreateInterceptor6 = new PreparedStatementCreateInterceptor();
            connection.addScopeInterceptor("prepareStatement", new String[] { "java.lang.String", "int", "int", "int" }, preparedStatementCreateInterceptor6, getScopeName());

            final ProfilerConfig profilerConfig = agent.getProfilerConfig();
            if (profilerConfig.isJdbcProfileSqlServerSetAutoCommit()) {
                Interceptor setAutocommit = new TransactionSetAutoCommitInterceptor();
                connection.addScopeInterceptor("setAutoCommit", new String[] { "boolean" }, setAutocommit, getScopeName());
            }
            if (profilerConfig.isJdbcProfileSqlServerCommit()) {
                Interceptor commit = new TransactionCommitInterceptor();
                connection.addScopeInterceptor("commit", null, commit, getScopeName());
            }
            if (profilerConfig.isJdbcProfileSqlServerRollback()) {
                Interceptor rollback = new TransactionRollbackInterceptor();
                connection.addScopeInterceptor("rollback", null, rollback, getScopeName());
            }

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", className);
            }

            return connection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }

}
