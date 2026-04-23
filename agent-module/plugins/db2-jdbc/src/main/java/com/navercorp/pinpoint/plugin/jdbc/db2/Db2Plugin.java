/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.db2;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParameters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParametersBuilder;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcAutoCommitConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
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
import java.util.Objects;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class Db2Plugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    private static final String DB2_SCOPE = Db2Constants.DB2_SCOPE;

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JdbcAutoCommitConfig config = Db2Config.of(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        context.addJdbcUrlParser(new Db2JdbcUrlParser());

        addDriverTransformer();
        addConnectionTransformer(config);
        addStatementTransformer();
        addPreparedStatementTransformer(config);
        addCallableStatementTransformer();
        addWasWrapperTransformer(config);
    }

    private void addDriverTransformer() {
        transformTemplate.transform(Db2Constants.JCC_DRIVER, Db2DriverTransform.class);
    }

    public static class Db2DriverTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentUtils.findMethodOrIgnore(target, "connect", "java.lang.String", "java.util.Properties")
                    .addScopedInterceptor(DriverConnectInterceptorV2.class,
                            va(Db2Constants.DB2), DB2_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addConnectionTransformer(JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform(Db2Constants.JCC_CONNECTION, Db2ConnectionTransform.class, parameters);
    }

    public static class Db2ConnectionTransform implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public Db2ConnectionTransform(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentUtils.findMethodOrIgnore(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, DB2_SCOPE);

            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "createStatement")
                    .addScopedInterceptor(statementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, DB2_SCOPE);

            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int[]")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareStatement", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);

            InstrumentUtils.findMethodOrIgnore(target, "prepareCall", "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareCall", "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "prepareCall", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DB2_SCOPE);

            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethodOrIgnore(target, "setAutoCommit", "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, DB2_SCOPE);
            }
            if (config.isProfileCommit()) {
                InstrumentUtils.findMethodOrIgnore(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, DB2_SCOPE);
            }
            if (config.isProfileRollback()) {
                InstrumentUtils.findMethodOrIgnore(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, DB2_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addStatementTransformer() {
        // considerSubclass=false: PreparedStatement/CallableStatement impls declare DB2PreparedStatement /
        // DB2CallableStatement directly, so excluding subclasses keeps this matcher Statement-only.
        final Matcher matcher = Matchers.newPackageBasedMatcher(Db2Constants.JCC_PACKAGE,
                new InterfaceInternalNameMatcherOperand(Db2Constants.JCC_STATEMENT_INTERFACE, false));
        transformTemplate.transform(matcher, Db2StatementTransform.class);
    }

    public static class Db2StatementTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, DB2_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String", "int[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "executeUpdate", "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);

            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String", "int[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "execute", "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DB2_SCOPE);

            return target.toBytecode();
        }
    }

    private void addPreparedStatementTransformer(JdbcAutoCommitConfig config) {
        // considerSubclass=false: CallableStatement impl declares only DB2CallableStatement,
        // so excluding subclasses prevents double instrumentation from the CallableStatement transformer.
        final Matcher matcher = Matchers.newPackageBasedMatcher(Db2Constants.JCC_PACKAGE,
                new InterfaceInternalNameMatcherOperand(Db2Constants.JCC_PREPARED_STATEMENT_INTERFACE, false));
        transformTemplate.transform(matcher, new Db2PreparedStatementTransform(config));
    }

    public static class Db2PreparedStatementTransform implements TransformCallback {

        private final JdbcAutoCommitConfig config;

        public Db2PreparedStatementTransform(JdbcAutoCommitConfig config) {
            this.config = Objects.requireNonNull(config, "config");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> psInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            final InstrumentMethod execute = target.getDeclaredMethod("execute");
            if (execute != null) {
                execute.addScopedInterceptor(psInterceptor, va(maxBindValueSize), DB2_SCOPE);
            }
            final InstrumentMethod executeQuery = target.getDeclaredMethod("executeQuery");
            if (executeQuery != null) {
                executeQuery.addScopedInterceptor(psInterceptor, va(maxBindValueSize), DB2_SCOPE);
            }
            final InstrumentMethod executeUpdate = target.getDeclaredMethod("executeUpdate");
            if (executeUpdate != null) {
                executeUpdate.addScopedInterceptor(psInterceptor, va(maxBindValueSize), DB2_SCOPE);
            }

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter filter = new PreparedStatementBindingMethodFilter();
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(filter);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, DB2_SCOPE);
                }
            }

            return target.toBytecode();
        }
    }

    private void addCallableStatementTransformer() {
        transformTemplate.transform(Db2Constants.JCC_CALLABLE_STATEMENT, Db2CallableStatementTransform.class);
    }

    public static class Db2CallableStatementTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final Class<? extends Interceptor> callableStatementInterceptor = CallableStatementRegisterOutParameterInterceptor.class;
            InstrumentUtils.findMethodOrIgnore(target, "registerOutParameter", "int", "int")
                    .addScopedInterceptor(callableStatementInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "registerOutParameter", "int", "int", "int")
                    .addScopedInterceptor(callableStatementInterceptor, DB2_SCOPE);
            InstrumentUtils.findMethodOrIgnore(target, "registerOutParameter", "int", "int", "java.lang.String")
                    .addScopedInterceptor(callableStatementInterceptor, DB2_SCOPE);

            return target.toBytecode();
        }
    }

    private void addWasWrapperTransformer(JdbcAutoCommitConfig config) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addJdbcConfig(config)
                .build();
        transformTemplate.transform(Db2Constants.WAS_PREPARED_STATEMENT, Db2PreparedStatementTransform.class, parameters);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = Objects.requireNonNull(transformTemplate, "transformTemplate");
    }
}
