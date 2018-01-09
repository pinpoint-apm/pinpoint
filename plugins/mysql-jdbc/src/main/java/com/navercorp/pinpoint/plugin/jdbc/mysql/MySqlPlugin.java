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
package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
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
 * @author Jongho Moon
 * @author HyunGil Jeong
 */
public class MySqlPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String MYSQL_SCOPE = MySqlConstants.MYSQL_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;
    private final JdbcUrlParserV2 jdbcUrlParser = new MySqlJdbcUrlParser();

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        MySqlConfig config = new MySqlConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("Mysql plugin is not executed because plugin enable value is false.");
            return;
        }

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer(config);
        addDriverTransformer();
        addStatementTransformer();
        addPreparedStatementTransformer(config);
        addCallableStatementTransformer(config);

        // From MySQL driver 5.1.x, backward compatibility is broken.
        // Driver returns not com.mysql.jdbc.Connection but com.mysql.jdbc.JDBC4Connection which extends com.mysql.jdbc.ConnectionImpl from 5.1.x
        addJDBC4PreparedStatementTransformer(config);
        addJDBC4CallableStatementTransformer(config);
    }

    private void addConnectionTransformer(final MySqlConfig config) {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod constructor = target.getConstructor("java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String");
                if (constructor != null) {
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.MySQLConnectionCreateInterceptor");
                }
                // 6.0.2 ~ 6.0.3
                InstrumentMethod constructor_6_X = target.getConstructor("com.mysql.cj.core.ConnectionString", "java.lang.String", "int", "java.util.Properties");
                if (constructor_6_X == null) {
                    // 6.0.4+
                    constructor_6_X = target.getConstructor("com.mysql.cj.core.conf.url.HostInfo");
                }
                if (constructor_6_X != null) {
                    target.addGetter("com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.OrigHostToConnectToGetter", "origHostToConnectTo");
                    target.addGetter("com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.OrigPortToConnectToGetter", "origPortToConnectTo");
                    target.addGetter("com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.DatabaseGetter", "database");
                    constructor_6_X.addInterceptor("com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.MySQL_6_X_ConnectionCreateInterceptor");
                }

                // close
                InstrumentUtils.findMethod(target, "close")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", MYSQL_SCOPE);

                // createStatement
                final String statementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "createStatement")
                        .addScopedInterceptor(statementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                        .addScopedInterceptor(statementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                        .addScopedInterceptor(statementCreate, MYSQL_SCOPE);

                // preparedStatement
                final String preparedStatementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);

                // preparecall
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", MYSQL_SCOPE);
                }

                if (config.isProfileCommit()) {
                    InstrumentUtils.findMethod(target, "commit")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", MYSQL_SCOPE);
                }

                if (config.isProfileRollback()) {
                    InstrumentUtils.findMethod(target, "rollback")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", MYSQL_SCOPE);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.mysql.jdbc.Connection", transformer);
        transformTemplate.transform("com.mysql.jdbc.ConnectionImpl", transformer);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.ConnectionImpl", transformer);
    }

    private void addDriverTransformer() {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2", va(MySqlConstants.MYSQL, false), MYSQL_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        };
        transformTemplate.transform("com.mysql.jdbc.NonRegisteringDriver", transformCallback);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.NonRegisteringDriver", transformCallback);
    }

    private void addPreparedStatementTransformer(final MySqlConfig config) {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String preparedStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MYSQL_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                    final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }

                return target.toBytecode();
            }
        };
        transformTemplate.transform("com.mysql.jdbc.PreparedStatement", transformCallback);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.PreparedStatement", transformCallback);
    }

    private void addCallableStatementTransformer(final MySqlConfig config) {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String callableStatementExecuteQuery = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target,"execute")
                        .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MYSQL_SCOPE);

                final String registerOutParameterInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor";
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                        .addScopedInterceptor(registerOutParameterInterceptor, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                        .addScopedInterceptor(registerOutParameterInterceptor, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                        .addScopedInterceptor(registerOutParameterInterceptor, MYSQL_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                    final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementBindVariableInterceptor", MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }

                return target.toBytecode();
            }
        };
        transformTemplate.transform("com.mysql.jdbc.CallableStatement", transformCallback);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.CallableStatement", transformCallback);
    }

    private void addJDBC4PreparedStatementTransformer(final MySqlConfig config) {
        transformTemplate.transform("com.mysql.jdbc.JDBC4PreparedStatement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (config.isTraceSqlBindValue()) {
                    final PreparedStatementBindingMethodFilter includes = PreparedStatementBindingMethodFilter.includes("setRowId", "setNClob", "setSQLXML");
                    final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(includes);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private void addJDBC4CallableStatementTransformer(final MySqlConfig config) {
        transformTemplate.transform("com.mysql.jdbc.JDBC4CallableStatement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (config.isTraceSqlBindValue()) {
                    final PreparedStatementBindingMethodFilter includes = PreparedStatementBindingMethodFilter.includes("setRowId", "setNClob", "setSQLXML");
                    final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(includes);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementBindVariableInterceptor", MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private void addStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                final String executeQueryInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                        .addScopedInterceptor(executeQueryInterceptor, MYSQL_SCOPE);

                final String executeUpdateInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor";
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.mysql.jdbc.Statement", transformer);
        transformTemplate.transform("com.mysql.jdbc.StatementImpl", transformer);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.StatementImpl", transformer);

    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
