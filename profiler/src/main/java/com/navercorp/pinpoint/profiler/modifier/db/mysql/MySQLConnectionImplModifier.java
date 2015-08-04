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

package com.navercorp.pinpoint.profiler.modifier.db.mysql;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.db.interceptor.*;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.interceptor.MySQLConnectionCreateInterceptor;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class MySQLConnectionImplModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MySQLConnectionImplModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher("com/mysql/jdbc/ConnectionImpl");
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        try {
            InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);


            mysqlConnection.addTraceValue(DatabaseInfoTraceValue.class);

            
            // If you want to make this common intercepter class, it has to be loaded to system.
//            TargetMethod createConnection  = new ConnectionCreateInterceptor();
//            String[] params = new String[] {
//                "java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String"
//            };
//            mysqlConnection.addInterceptor("getInstance", params, createConnection);
            Interceptor connectionUrlBindInterceptor = new MySQLConnectionCreateInterceptor();
            mysqlConnection.addConstructorInterceptor(new String[]{"java.lang.String", "int",
                    "java.util.Properties", "java.lang.String", "java.lang.String" }, connectionUrlBindInterceptor);


            Interceptor closeConnection = new ConnectionCloseInterceptor();
            mysqlConnection.addGroupInterceptor("close", null, closeConnection, MYSQLScope.SCOPE_NAME);


            Interceptor statementCreateInterceptor1 = new StatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("createStatement", null, statementCreateInterceptor1, MYSQLScope.SCOPE_NAME);

            Interceptor statementCreateInterceptor2 = new StatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("createStatement", new String[]{"int", "int"}, statementCreateInterceptor2, MYSQLScope.SCOPE_NAME);

            Interceptor statementCreateInterceptor3 = new StatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("createStatement", new String[]{"int", "int", "int"}, statementCreateInterceptor3, MYSQLScope.SCOPE_NAME);


            Interceptor preparedStatementCreateInterceptor1 = new PreparedStatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String"}, preparedStatementCreateInterceptor1, MYSQLScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor2 = new PreparedStatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int"}, preparedStatementCreateInterceptor2, MYSQLScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor3 = new PreparedStatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int[]"}, preparedStatementCreateInterceptor3, MYSQLScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor4 = new PreparedStatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "java.lang.String[]"}, preparedStatementCreateInterceptor4, MYSQLScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor5 = new PreparedStatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int"}, preparedStatementCreateInterceptor5, MYSQLScope.SCOPE_NAME);

            Interceptor preparedStatementCreateInterceptor6 = new PreparedStatementCreateInterceptor();
            mysqlConnection.addGroupInterceptor("prepareStatement", new String[]{"java.lang.String", "int", "int", "int"}, preparedStatementCreateInterceptor6, MYSQLScope.SCOPE_NAME);

            final ProfilerConfig profilerConfig = this.getProfilerConfig();
            if (profilerConfig.isJdbcProfileMySqlSetAutoCommit()) {
                Interceptor setAutocommit = new TransactionSetAutoCommitInterceptor();
                mysqlConnection.addGroupInterceptor("setAutoCommit", new String[]{"boolean"}, setAutocommit, MYSQLScope.SCOPE_NAME);
            }
            if (profilerConfig.isJdbcProfileMySqlCommit()) {
                Interceptor commit = new TransactionCommitInterceptor();
                mysqlConnection.addGroupInterceptor("commit", null, commit, MYSQLScope.SCOPE_NAME);
            }
            if (profilerConfig.isJdbcProfileMySqlRollback()) {
                Interceptor rollback = new TransactionRollbackInterceptor();
                mysqlConnection.addGroupInterceptor("rollback", null, rollback, MYSQLScope.SCOPE_NAME);
            }
            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return mysqlConnection.toBytecode();
        } catch (InstrumentException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return null;
        }
    }


}
