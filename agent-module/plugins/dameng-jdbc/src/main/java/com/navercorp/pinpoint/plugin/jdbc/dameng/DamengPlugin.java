/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.dameng;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
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

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author yjqg6666
 */
public class DamengPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String DAMENG_SCOPE = DamengConstants.DAMENG_SCOPE;

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private final JdbcUrlParserV2 jdbcUrlParser = new DamengJdbcUrlParser();

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        DamengConfig config = new DamengConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        context.addJdbcUrlParser(jdbcUrlParser);

        addDriverTransformer();
        addConnectionTransformer();
        addPreparedStatementTransformer();
        addCallableStatementTransformer();
        addStatementTransformer();
    }

    private void addDriverTransformer() {
        transformTemplate.transform("dm.jdbc.driver.DmDriver", DriverTransformer.class);
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
                            va(DamengConstants.DAMENG, true), DAMENG_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addConnectionTransformer() {
        transformTemplate.transform("dm.jdbc.driver.DmdbConnection", DamengConnectionTransform.class);
    }

    public static class DamengConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            DamengConfig config = new DamengConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, DAMENG_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement")
                    .addScopedInterceptor(statementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, DAMENG_SCOPE);

            // preparedStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);

            // preparecall
            InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, DAMENG_SCOPE);

            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, DAMENG_SCOPE);
            }

            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, DAMENG_SCOPE);
            }

            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, DAMENG_SCOPE);
            }

            return target.toBytecode();
        }
    };

    private void addPreparedStatementTransformer() {
        transformTemplate.transform("dm.jdbc.driver.DmdbPreparedStatement", PreparedStatementTransform.class);
    }

    public static class PreparedStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {

            DamengConfig config = new DamengConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            interceptExecute(target, config);
            interceptBindVariable(target, config);

            return target.toBytecode();
        }

        private void interceptExecute(InstrumentClass target, DamengConfig config) throws InstrumentException {
            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "execute")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), DAMENG_SCOPE);

        }

        private void interceptBindVariable(InstrumentClass target, DamengConfig config) throws InstrumentException {
            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter
                        .excludes("setRowId", "setNClob", "setSQLXML");

                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, DAMENG_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }
        }

    };

    private void addStatementTransformer() {
        transformTemplate.transform("dm.jdbc.driver.DmdbStatement", DamengStatementTransform.class);
    }

    public static class DamengStatementTransform implements TransformCallback {

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
                    .addScopedInterceptor(executeQueryInterceptor, DAMENG_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);

            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(executeUpdateInterceptor, DAMENG_SCOPE);

            return target.toBytecode();
        }
    };

    private void addCallableStatementTransformer() {
        transformTemplate.transform("dm.jdbc.driver.DmdbCallableStatement", CallableStatementTransformer.class);
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
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);

            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "java.sql.SQLType")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "java.sql.SQLType", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "java.sql.SQLType", "java.lang.String")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);

            InstrumentUtils.findMethod(target, "registerOutParameter",  "java.lang.String", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter",  "java.lang.String", "int", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter",  "java.lang.String", "int", "java.lang.String")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);

            InstrumentUtils.findMethod(target, "registerOutParameter", "java.lang.String", "java.sql.SQLType")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "java.lang.String", "java.sql.SQLType", "int")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "java.lang.String", "java.sql.SQLType", "java.lang.String")
                    .addScopedInterceptor(registerOutParameterInterceptor, DAMENG_SCOPE);

            return target.toBytecode();
        }
    };

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
