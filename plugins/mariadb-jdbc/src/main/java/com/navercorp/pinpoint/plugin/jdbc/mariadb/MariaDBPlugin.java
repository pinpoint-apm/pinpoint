/*
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
package com.navercorp.pinpoint.plugin.jdbc.mariadb;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author dawidmalina
 */
public class MariaDBPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String MARIADB_SCOPE = MariaDBConstants.MARIADB_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final JdbcUrlParserV2 jdbcUrlParser = new MariaDBJdbcUrlParser();

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        MariaDBConfig config = new MariaDBConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("MariaDB plugin is not executed because plugin enable value is false.");
            return;
        }

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer(config);
        addDriverTransformer();
        addPreparedStatementTransformer(config);
        addPreparedStatementBindVariableTransformer(config);
        addCallableStatementTransformer();
        addStatementTransformer();

        // MariaDb 1.3.x's CallableStatements are completely separated from PreparedStatements (similar to MySQL)
        // Separate interceptors must be injected.
        add_1_3_x_CallableStatementTransformer(config);
    }

    private void addConnectionTransformer(final MariaDBConfig config) {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                // close
                InstrumentUtils.findMethod(target, "close")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", MARIADB_SCOPE);

                // createStatement
                final String statementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "createStatement")
                        .addScopedInterceptor(statementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                        .addScopedInterceptor(statementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                        .addScopedInterceptor(statementCreate, MARIADB_SCOPE);

                // preparedStatement
                final String preparedStatementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);

                // preparecall
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MARIADB_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", MARIADB_SCOPE);
                }

                if (config.isProfileCommit()) {
                    InstrumentUtils.findMethod(target, "commit")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", MARIADB_SCOPE);
                }

                if (config.isProfileRollback()) {
                    InstrumentUtils.findMethod(target, "rollback")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", MARIADB_SCOPE);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.MariaDbConnection", transformer);
    }

    private void addDriverTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.Driver", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2",
                                va(MariaDBConstants.MARIADB, true), MARIADB_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }

    private void addPreparedStatementTransformer(final MariaDBConfig config) {

        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String preparedStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MARIADB_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.MariaDbServerPreparedStatement", transformer);
        transformTemplate.transform("org.mariadb.jdbc.MariaDbClientPreparedStatement", transformer);
        // 1.6.x
        transformTemplate.transform("org.mariadb.jdbc.MariaDbPreparedStatementServer", transformer);
        transformTemplate.transform("org.mariadb.jdbc.MariaDbPreparedStatementClient", transformer);

    }

    private void addPreparedStatementBindVariableTransformer(final MariaDBConfig config) {

        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                if (config.isTraceSqlBindValue()) {
                    final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter
                            .excludes("setRowId", "setNClob", "setSQLXML");

                    final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", MARIADB_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.AbstractMariaDbPrepareStatement", transformer);
        // Class renamed in 1.5.6 - https://github.com/MariaDB/mariadb-connector-j/commit/16c8313960cf4fbc6b2b83136504d1ba9e662919
        transformTemplate.transform("org.mariadb.jdbc.AbstractPrepareStatement", transformer);
        // 1.6.x
        transformTemplate.transform("org.mariadb.jdbc.BasePrepareStatement", transformer);
    }

    private void addStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                final String executeQueryInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                        .addScopedInterceptor(executeQueryInterceptor, MARIADB_SCOPE);

                final String executeUpdateInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor";
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, MARIADB_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.MariaDbStatement", transformer);

    }

    private void addCallableStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                        byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                final String registerOutParameterInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor";
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                        .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                        .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                        .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.AbstractCallableProcedureStatement", transformer);
        transformTemplate.transform("org.mariadb.jdbc.AbstractCallableFunctionStatement", transformer);
        // 1.6.x
        transformTemplate.transform("org.mariadb.jdbc.CallableProcedureStatement", transformer);
        transformTemplate.transform("org.mariadb.jdbc.CallableFunctionStatement", transformer);
    }

    private void add_1_3_x_CallableStatementTransformer(final MariaDBConfig config) {
        transformTemplate.transform("org.mariadb.jdbc.MariaDbCallableStatement", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                        byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String callableStatementExecuteQuery = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target,"execute")
                        .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MARIADB_SCOPE);

                final String registerOutParameterInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor";
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                        .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                        .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                        .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);


                if (config.isTraceSqlBindValue()) {
                    final MethodFilter filter = new PreparedStatementBindingMethodFilter();
                    final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(filter);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementBindVariableInterceptor", MARIADB_SCOPE);
                    }
                }

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
