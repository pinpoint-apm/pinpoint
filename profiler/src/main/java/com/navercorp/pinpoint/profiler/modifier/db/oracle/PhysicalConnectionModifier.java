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

package com.navercorp.pinpoint.profiler.modifier.db.oracle;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.*;

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

    public Matcher getMatcher() {
        // There is a common super class of T4C, T2C (OCI subclasses T2C), which is based on PhysicalConnection.
        // So modifying PhysicalConnection will be enough.
        return Matchers.newClassNameMatcher("oracle/jdbc/driver/PhysicalConnection");
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        try {
            InstrumentableClass oracleConnection = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);


            oracleConnection.addTraceValue(DatabaseInfoTraceValue.class);

            // If we make this as common interceptor, it has to be loaded to system.
//            TargetMethod createConnection  = new ConnectionCreateInterceptor();
//            String[] params = new String[] {
//                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
//            };
//            mysqlConnection.addInterceptor("getInstance", params, createConnection);


            Interceptor closeConnection = new ConnectionCloseInterceptor();
            oracleConnection.addGroupInterceptor("close", null, closeConnection, OracleScope.SCOPE_NAME);


            Interceptor statementCreateInterceptor1 = new StatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("createStatement", null, statementCreateInterceptor1, OracleScope.SCOPE_NAME);

            Interceptor statementCreateInterceptor2 = new StatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("createStatement", new String[]{"int", "int"}, statementCreateInterceptor2, OracleScope.SCOPE_NAME);

            Interceptor statementCreateInterceptor3 = new StatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("createStatement", new String[]{"int", "int", "int"}, statementCreateInterceptor3, OracleScope.SCOPE_NAME);


            Interceptor preparedStatementCreateInterceptor1 = new PreparedStatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatementCreateInterceptor1, OracleScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor2 = new PreparedStatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int"}, preparedStatementCreateInterceptor2, OracleScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor3 = new PreparedStatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int[]"}, preparedStatementCreateInterceptor3, OracleScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor4 = new PreparedStatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "java.lang.String[]"}, preparedStatementCreateInterceptor4, OracleScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor5 = new PreparedStatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int"}, preparedStatementCreateInterceptor5, OracleScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor6 = new PreparedStatementCreateInterceptor();
            oracleConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int", "int"}, preparedStatementCreateInterceptor6, OracleScope.SCOPE_NAME);

            final ProfilerConfig profilerConfig = this.getProfilerConfig();
            if (profilerConfig.isJdbcProfileOracleSetAutoCommit()) {
                Interceptor setAutocommit = new TransactionSetAutoCommitInterceptor();
                oracleConnection.addGroupInterceptor("setAutoCommit", new String[]{"boolean"}, setAutocommit, OracleScope.SCOPE_NAME);
            }
            if (profilerConfig.isJdbcProfileOracleCommit()) {
                Interceptor commit = new TransactionCommitInterceptor();
                oracleConnection.addGroupInterceptor("commit", null, commit, OracleScope.SCOPE_NAME);
            }
            if (profilerConfig.isJdbcProfileOracleRollback()) {
                Interceptor rollback = new TransactionRollbackInterceptor();
                oracleConnection.addGroupInterceptor("rollback", null, rollback, OracleScope.SCOPE_NAME);
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
