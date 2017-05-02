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
package com.navercorp.pinpoint.plugin.jdbc.postgresql;

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
 * @author Brad Hong
 *
 */
public class PostgreSqlPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String POSTGRESQL_SCOPE = PostgreSqlConstants.POSTGRESQL_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final JdbcUrlParserV2 jdbcUrlParser = new PostgreSqlJdbcUrlParser();

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        PostgreSqlConfig config = new PostgreSqlConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("PostgreSql plugin is not executed because plugin enable value is false.");
            return;
        }

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer(config);
        addDriverTransformer();
        addPreparedStatementTransformer(config);
    }

    private void addConnectionTransformer(final PostgreSqlConfig config) {
        transformTemplate.transform("org.postgresql.jdbc4.Jdbc4Connection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentUtils.findConstructor(target, "org.postgresql.util.HostSpec[]", "java.lang.String", "java.lang.String", "java.util.Properties", "java.lang.String")
                        .addInterceptor("com.navercorp.pinpoint.plugin.jdbc.postgresql.interceptor.PostgreSQLConnectionCreateInterceptor");

                InstrumentUtils.findMethod(target,"prepareStatement", "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor("com.navercorp.pinpoint.plugin.jdbc.postgresql.interceptor.PostgreSqlPreparedStatementCreateInterceptor3", POSTGRESQL_SCOPE);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.postgresql.jdbc3.Jdbc3Connection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                // createStatement
                final String statementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "createStatement")
                        .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                        .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                        .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.postgresql.jdbc2.AbstractJdbc2Connection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                // close
                InstrumentUtils.findMethod(target, "close")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", POSTGRESQL_SCOPE);

                InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String")
                        .addScopedInterceptor("com.navercorp.pinpoint.plugin.jdbc.postgresql.interceptor.PostgreSqlPreparedStatementCreateInterceptor1", POSTGRESQL_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", POSTGRESQL_SCOPE);
                }

                if (config.isProfileCommit()) {
                    InstrumentUtils.findMethod(target, "commit")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", POSTGRESQL_SCOPE);
                }

                if (config.isProfileRollback()) {
                    InstrumentUtils.findMethod(target, "rollback")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", POSTGRESQL_SCOPE);
                }

                return target.toBytecode();
            }
        });

        transformTemplate.transform("org.postgresql.jdbc3.AbstractJdbc3Connection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                // preparedStatement
                final String preparedStatementCreate = "com.navercorp.pinpoint.plugin.jdbc.postgresql.interceptor.PostgreSqlPreparedStatementCreateInterceptor2";
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                        .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                        .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                        .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);

                return target.toBytecode();
            }
        });
    }

    private void addDriverTransformer() {
        transformTemplate.transform("org.postgresql.Driver", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2",
                                va(PostgreSqlConstants.POSTGRESQL, false), POSTGRESQL_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }

    private void addPreparedStatementTransformer(final PostgreSqlConfig config) {
        transformTemplate.transform("org.postgresql.jdbc2.AbstractJdbc2Statement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String preparedStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.postgresql.jdbc3.AbstractJdbc3Statement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", POSTGRESQL_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                final String executeQueryInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                        .addScopedInterceptor(executeQueryInterceptor, POSTGRESQL_SCOPE);

                final String executeUpdateInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor";
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
