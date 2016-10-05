/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;

/**
 * Plugin for <a href="https://github.com/xerial/sqlite-jdbc">SQLite JDBC Driver</a>
 *
 * @author barney
 *
 */
public class SqlitePlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String CLASS_CONN = "org.sqlite.Conn";
    private static final String CLASS_STMT = "org.sqlite.Stmt";
    private static final String CLASS_PREP_STMT = "org.sqlite.PrepStmt";

    private static final String CLASS_CORE_CONNECTION = "org.sqlite.core.CoreConnection";

    private static final String CLASS_JDBC3_CONNECTION = "org.sqlite.jdbc4.JDBC3Connection";
    private static final String CLASS_JDBC3_STATEMENT = "org.sqlite.jdbc3.JDBC3Statement";
    private static final String CLASS_JDBC3_PREPARED_STATEMENT = "org.sqlite.jdbc3.JDBC3PreparedStatement";

    private static final String CLASS_JDBC4_CONNECTION = "org.sqlite.jdbc4.JDBC4Connection";
    private static final String CLASS_JDBC4_STATEMENT = "org.sqlite.jdbc4.JDBC4Statement";
    private static final String CLASS_JDBC4_PREPARED_STATEMENT = "org.sqlite.jdbc4.JDBC4PreparedStatement";

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        SqlitePluginConfig config = new SqlitePluginConfig(context.getConfig());

        addConnectionTransformer(config);
        addDriverTransformer();
        addStatementTransformer();
        addPreparedStatementTransformer(config);
    }

    private void addDriverTransformer() {
        transformTemplate.transform("org.sqlite.JDBC", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor",
                        va(new SqliteJdbcUrlParser()), SqlitePluginConstants.SQLITE_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }

        });

    }

    private void addConnectionTransformer(final SqlitePluginConfig config) {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                if (className.equals(CLASS_CORE_CONNECTION)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                    return target.toBytecode();
                } else if (className.equals(CLASS_CONN)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                } else if (className.equals(CLASS_JDBC3_CONNECTION)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.jdbc.sqlite.interceptor.SqliteJdbc3StatementCreateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.jdbc.sqlite.interceptor.SqliteJdbc3PreparedStatementCreateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                } else if (className.equals(CLASS_JDBC4_CONNECTION)) {
                    if (!target.hasDeclaredMethod("setAutoCommit", "boolean") && target.hasMethod("setAutoCommit", "boolean")) {
                      target.addDelegatorMethod("setAutoCommit", "boolean");
                    }
                    if (!target.hasDeclaredMethod("commit") && target.hasMethod("commit")) {
                        target.addDelegatorMethod("commit");
                    }
                    if (!target.hasDeclaredMethod("rollback") && target.hasMethod("rollback")) {
                        target.addDelegatorMethod("rollback");
                    }
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.jdbc.sqlite.interceptor.SqliteJdbc4StatementCreateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.jdbc.sqlite.interceptor.SqliteJdbc4PreparedStatementCreateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                }

                if (config.isProfileSetAutoCommit()) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                }

                if (config.isProfileCommit()) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                }

                if (config.isProfileRollback()) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_JDBC3_CONNECTION, transformCallback);
        transformTemplate.transform(CLASS_JDBC4_CONNECTION, transformCallback);
        transformTemplate.transform(CLASS_CORE_CONNECTION, transformCallback);
        transformTemplate.transform(CLASS_CONN, transformCallback);
    }

    private void addStatementTransformer() {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                if (!className.equals(CLASS_JDBC4_STATEMENT)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                }

                if (className.equals(CLASS_STMT)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.jdbc.sqlite.interceptor.SqliteStmtExecuteUpdateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                } else if (className.equals(CLASS_JDBC3_STATEMENT)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_JDBC3_STATEMENT, transformCallback);
        transformTemplate.transform(CLASS_JDBC4_STATEMENT, transformCallback);
        transformTemplate.transform(CLASS_STMT, transformCallback);
    }

    private void addPreparedStatementTransformer(final SqlitePluginConfig config) {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                if (!className.equals(CLASS_JDBC4_PREPARED_STATEMENT)) {
                    target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor",
                        va(maxBindValueSize), SqlitePluginConstants.SQLITE_SCOPE);
                }
                if (config.isTraceSqlBindValue()) {
                    final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter
                            .excludes("setRowId", "setNString", "setNCharacterStream", "setNClob", "setClob", "setBlob", "setSQLXML", "setAsciiStream", "setBinaryStream", "setCharacterStream", "setNCharacterStream");
                    target.addScopedInterceptor(excludes,
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor",
                        SqlitePluginConstants.SQLITE_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_JDBC4_PREPARED_STATEMENT, transformCallback);
        transformTemplate.transform(CLASS_JDBC3_PREPARED_STATEMENT, transformCallback);
        transformTemplate.transform(CLASS_PREP_STMT, transformCallback);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
