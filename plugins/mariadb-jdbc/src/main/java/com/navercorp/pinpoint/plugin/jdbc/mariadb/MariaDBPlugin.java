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
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementBindVariableInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor;
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
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer();
        addDriverTransformer();
        addPreparedStatementTransformer();
        addPreparedStatementBindVariableTransformer();
        addCallableStatementTransformer();
        addStatementTransformer();

        // MariaDb 1.3.x's CallableStatements are completely separated from PreparedStatements (similar to MySQL)
        // Separate interceptors must be injected.
        add_1_3_x_CallableStatementTransformer();
    }

    private void addConnectionTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.MariaDbConnection", MariaDbConnectionTransform.class);
    }

    public static class MariaDbConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            MariaDBConfig config = new MariaDBConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, MARIADB_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement")
                    .addScopedInterceptor(statementCreate, MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, MARIADB_SCOPE);

            // preparedStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
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
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, MARIADB_SCOPE);
            }

            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, MARIADB_SCOPE);
            }

            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, MARIADB_SCOPE);
            }

            return target.toBytecode();
        }
    };

    private void addDriverTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.Driver", DriverTransformer.class);
    }

    public static class DriverTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);

            InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                    .addScopedInterceptor(DriverConnectInterceptorV2.class,
                            va(MariaDBConstants.MARIADB, true), MARIADB_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addPreparedStatementTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.MariaDbServerPreparedStatement", PreparedStatementTransform.class);
        transformTemplate.transform("org.mariadb.jdbc.MariaDbClientPreparedStatement", PreparedStatementTransform.class);
        // [1.6.0,1.8.0), [2.0.0,2.4.0)
        transformTemplate.transform("org.mariadb.jdbc.MariaDbPreparedStatementServer", PreparedStatementTransform.class);
        transformTemplate.transform("org.mariadb.jdbc.MariaDbPreparedStatementClient", PreparedStatementTransform.class);
        // [1.8.0,2.0.0), [2.4.0,)
        transformTemplate.transform("org.mariadb.jdbc.ServerSidePreparedStatement", PreparedStatementTransform.class);
        transformTemplate.transform("org.mariadb.jdbc.ClientSidePreparedStatement", PreparedStatementTransform.class);

    }

    public static class PreparedStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {

            MariaDBConfig config = new MariaDBConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "execute")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MARIADB_SCOPE);

            return target.toBytecode();
        }
    };

    private void addPreparedStatementBindVariableTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.AbstractMariaDbPrepareStatement", PreparedStatementBindVariableTransformer.class);
        // Class renamed in 1.5.6 - https://github.com/MariaDB/mariadb-connector-j/commit/16c8313960cf4fbc6b2b83136504d1ba9e662919
        transformTemplate.transform("org.mariadb.jdbc.AbstractPrepareStatement", PreparedStatementBindVariableTransformer.class);
        // 1.6.x
        transformTemplate.transform("org.mariadb.jdbc.BasePrepareStatement", PreparedStatementBindVariableTransformer.class);
    }


    public static class PreparedStatementBindVariableTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            MariaDBConfig config = new MariaDBConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter
                        .excludes("setRowId", "setNClob", "setSQLXML");

                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, MARIADB_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    };

    private void addStatementTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.MariaDbStatement", MariaDbStatementTransform.class);
    }

    public static class MariaDbStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, MARIADB_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
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

    private void addCallableStatementTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.AbstractCallableProcedureStatement", CallableStatementTransformer.class);
        transformTemplate.transform("org.mariadb.jdbc.AbstractCallableFunctionStatement", CallableStatementTransformer.class);
        // 1.6.x
        transformTemplate.transform("org.mariadb.jdbc.CallableProcedureStatement", CallableStatementTransformer.class);
        transformTemplate.transform("org.mariadb.jdbc.CallableFunctionStatement", CallableStatementTransformer.class);
    }

    public static class CallableStatementTransformer implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final Class<? extends Interceptor> registerOutParameterInterceptor = CallableStatementRegisterOutParameterInterceptor.class;
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                    .addScopedInterceptor(registerOutParameterInterceptor, MARIADB_SCOPE);

            return target.toBytecode();
        }
    };

    private void add_1_3_x_CallableStatementTransformer() {
        transformTemplate.transform("org.mariadb.jdbc.MariaDbCallableStatement", CallableStatement1_3_x_Transform.class);
    }

    public static class CallableStatement1_3_x_Transform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) throws InstrumentException {
            MariaDBConfig config = new MariaDBConfig(instrumentor.getProfilerConfig());

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> callableStatementExecuteQuery = CallableStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target,"execute")
                    .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery")
                    .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MARIADB_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate")
                    .addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MARIADB_SCOPE);

            final Class<? extends Interceptor> registerOutParameterInterceptor = CallableStatementRegisterOutParameterInterceptor.class;
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
                    method.addScopedInterceptor(CallableStatementBindVariableInterceptor.class, MARIADB_SCOPE);
                }
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
