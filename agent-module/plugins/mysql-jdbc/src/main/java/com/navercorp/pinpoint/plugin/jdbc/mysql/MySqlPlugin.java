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
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParameters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParametersBuilder;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcAutoCommitConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
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
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.MySQLConnectionCreateInterceptor;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.MySQL_6_X_ConnectionCreateInterceptor;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.DatabaseGetter;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.OrigHostToConnectToGetter;
import com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.getter.OrigPortToConnectToGetter;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 */
public class MySqlPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String MYSQL_SCOPE = MySqlConstants.MYSQL_SCOPE;

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private TransformTemplate transformTemplate;
    private final JdbcUrlParserV2 jdbcUrlParser = new MySqlJdbcUrlParser();

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JdbcAutoCommitConfig config = MySqlConfig.of(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

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

    private void addConnectionTransformer(final JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform("com.mysql.jdbc.Connection", ConnectionTransform.class, parameters);
        transformTemplate.transform("com.mysql.jdbc.ConnectionImpl", ConnectionTransform.class, parameters);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.ConnectionImpl", ConnectionTransform.class, parameters);
    }

    public static class ConnectionTransform implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public ConnectionTransform(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructor = target.getConstructor("java.lang.String", "int", "java.util.Properties", "java.lang.String", "java.lang.String");
            if (constructor != null) {
                constructor.addInterceptor(MySQLConnectionCreateInterceptor.class);
            }
            // 6.0.2 ~ 6.0.3
            InstrumentMethod constructor_6_X = target.getConstructor("com.mysql.cj.core.ConnectionString", "java.lang.String", "int", "java.util.Properties");
            if (constructor_6_X == null) {
                // 6.0.4+
                constructor_6_X = target.getConstructor("com.mysql.cj.core.conf.url.HostInfo");
            }
            if (constructor_6_X == null) {
                // 8.x+
                constructor_6_X = target.getConstructor("com.mysql.cj.conf.HostInfo");
            }
            if (constructor_6_X != null) {
                target.addGetter(OrigHostToConnectToGetter.class, "origHostToConnectTo");
                target.addGetter(OrigPortToConnectToGetter.class, "origPortToConnectTo");
                target.addGetter(DatabaseGetter.class, "database");
                constructor_6_X.addInterceptor(MySQL_6_X_ConnectionCreateInterceptor.class);
            }

            // close
            InstrumentUtils.findMethodOrIgnore(target, "close").addScopedInterceptor(ConnectionCloseInterceptor.class, MYSQL_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "createStatement").addScopedInterceptor(statementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "createStatement", "int", "int").addScopedInterceptor(statementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "createStatement", "int", "int", "int").addScopedInterceptor(statementCreate, MYSQL_SCOPE);

            // preparedStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int[]").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "java.lang.String[]").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int", "int").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int", "int", "int").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);

            // preparecall
            InstrumentUtils.findMethodOrIgnore(target, "prepareCall", "java.lang.String").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareCall", "java.lang.String", "int", "int").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareCall", "java.lang.String", "int", "int", "int").addScopedInterceptor(preparedStatementCreate, MYSQL_SCOPE);

            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethodOrIgnore(target, "setAutoCommit", "boolean").addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, MYSQL_SCOPE);
            }
            if (config.isProfileCommit()) {
                InstrumentUtils.findMethodOrIgnore(target, "commit").addScopedInterceptor(TransactionCommitInterceptor.class, MYSQL_SCOPE);
            }
            if (config.isProfileRollback()) {
                InstrumentUtils.findMethodOrIgnore(target, "rollback").addScopedInterceptor(TransactionRollbackInterceptor.class, MYSQL_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addDriverTransformer() {
        transformTemplate.transform("com.mysql.jdbc.NonRegisteringDriver", DriverTransform.class);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.NonRegisteringDriver", DriverTransform.class);
    }

    public static class DriverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentUtils.findMethodOrIgnore(target, "connect", "java.lang.String", "java.util.Properties").addScopedInterceptor(DriverConnectInterceptorV2.class, va(MySqlConstants.MYSQL, false), MYSQL_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addPreparedStatementTransformer(final JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform("com.mysql.jdbc.PreparedStatement", PreparedStatementTransform.class, parameters);
        transformTemplate.transform("com.mysql.jdbc.ServerPreparedStatement", PreparedStatementTransform.class, parameters);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.PreparedStatement", PreparedStatementTransform.class, parameters);
        // 8.0.11+
        transformTemplate.transform("com.mysql.cj.jdbc.ClientPreparedStatement", PreparedStatementTransform.class, parameters);
        transformTemplate.transform("com.mysql.cj.jdbc.ServerPreparedStatement", PreparedStatementTransform.class, parameters);
    }

    public static class PreparedStatementTransform implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public PreparedStatementTransform(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MYSQL_SCOPE);
            }
            InstrumentMethod executeQueryMethod = target.getDeclaredMethod("executeQuery");
            if (executeQueryMethod != null) {
                executeQueryMethod.addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MYSQL_SCOPE);
            }
            InstrumentMethod executeUpdateMethod = target.getDeclaredMethod("executeUpdate");
            if (executeUpdateMethod != null) {
                executeUpdateMethod.addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), MYSQL_SCOPE);
            }

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }

    private void addCallableStatementTransformer(final JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform("com.mysql.jdbc.CallableStatement", CallableStatementTransform.class, parameters);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.CallableStatement", CallableStatementTransform.class, parameters);
    }

    public static class CallableStatementTransform implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public CallableStatementTransform(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> callableStatementExecuteQuery = CallableStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "execute").addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeQuery").addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate").addScopedInterceptor(callableStatementExecuteQuery, va(maxBindValueSize), MYSQL_SCOPE);

            final Class<? extends Interceptor> registerOutParameterInterceptor = CallableStatementRegisterOutParameterInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "registerOutParameter", "int", "int").addScopedInterceptor(registerOutParameterInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "registerOutParameter", "int", "int", "int").addScopedInterceptor(registerOutParameterInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "registerOutParameter", "int", "int", "java.lang.String").addScopedInterceptor(registerOutParameterInterceptor, MYSQL_SCOPE);

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(CallableStatementBindVariableInterceptor.class, MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }

    private void addJDBC4PreparedStatementTransformer(final JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform("com.mysql.jdbc.JDBC4PreparedStatement", JDBC4PreparedStatementTransform.class, parameters);
    }

    public static class JDBC4PreparedStatementTransform implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public JDBC4PreparedStatementTransform(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter includes = PreparedStatementBindingMethodFilter.includes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(includes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }

    private void addJDBC4CallableStatementTransformer(final JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform("com.mysql.jdbc.JDBC4CallableStatement", JDBC4CallableStatement.class, parameters);
    }

    public static class JDBC4CallableStatement implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public JDBC4CallableStatement(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter includes = PreparedStatementBindingMethodFilter.includes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(includes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(CallableStatementBindVariableInterceptor.class, MYSQL_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }

    private void addStatementTransformer() {
        transformTemplate.transform("com.mysql.jdbc.Statement", StatementTransformer.class);
        transformTemplate.transform("com.mysql.jdbc.StatementImpl", StatementTransformer.class);
        // 6.x+
        transformTemplate.transform("com.mysql.cj.jdbc.StatementImpl", StatementTransformer.class);
    }

    public static class StatementTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "executeQuery", "java.lang.String").addScopedInterceptor(executeQueryInterceptor, MYSQL_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String", "int").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String", "int[]").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String", "java.lang.String[]").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String", "int").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String", "int[]").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String", "String[]").addScopedInterceptor(executeUpdateInterceptor, MYSQL_SCOPE);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
