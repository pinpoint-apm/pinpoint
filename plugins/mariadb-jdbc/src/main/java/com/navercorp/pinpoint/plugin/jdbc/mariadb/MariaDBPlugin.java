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
 * @author dawidmalina
 */
public class MariaDBPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        MariaDBConfig config = new MariaDBConfig(context.getConfig());

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

                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor",
                        MariaDBConstants.MARIADB_SCOPE);
                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor",
                        MariaDBConstants.MARIADB_SCOPE);
                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor",
                        MariaDBConstants.MARIADB_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    target.addScopedInterceptor(
                            "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor",
                            MariaDBConstants.MARIADB_SCOPE);
                }

                if (config.isProfileCommit()) {
                    target.addScopedInterceptor(
                            "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor",
                            MariaDBConstants.MARIADB_SCOPE);
                }

                if (config.isProfileRollback()) {
                    target.addScopedInterceptor(
                            "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor",
                            MariaDBConstants.MARIADB_SCOPE);
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

                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor",
                        va(new MariaDBJdbcUrlParser(), true), MariaDBConstants.MARIADB_SCOPE, ExecutionPolicy.ALWAYS);

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

                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor",
                        va(maxBindValueSize), MariaDBConstants.MARIADB_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.MariaDbServerPreparedStatement", transformer);
        transformTemplate.transform("org.mariadb.jdbc.MariaDbClientPreparedStatement", transformer);

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
                    target.addScopedInterceptor(excludes,
                            "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor",
                            MariaDBConstants.MARIADB_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.AbstractMariaDbPrepareStatement", transformer);

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

                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor",
                        MariaDBConstants.MARIADB_SCOPE);
                target.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor",
                        MariaDBConstants.MARIADB_SCOPE);

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

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor", MariaDBConstants.MARIADB_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.mariadb.jdbc.AbstractCallableProcedureStatement", transformer);
        transformTemplate.transform("org.mariadb.jdbc.AbstractCallableFunctionStatement", transformer);
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

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementExecuteQueryInterceptor", va(maxBindValueSize), MariaDBConstants.MARIADB_SCOPE);
                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor", MariaDBConstants.MARIADB_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementBindVariableInterceptor", MariaDBConstants.MARIADB_SCOPE);
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
