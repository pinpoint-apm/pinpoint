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
package com.navercorp.pinpoint.plugin.jdbc.clickhouse;

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
import com.navercorp.pinpoint.plugin.jdbc.clickhouse.interceptor.ClickHouseConnectionCreateInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 */
public class ClickHousePlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String CLICK_HOUSE_SCOPE = ClickHouseConstants.CLICK_HOUSE_SCOPE;

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private TransformTemplate transformTemplate;
    private final JdbcUrlParserV2 jdbcUrlParser = new ClickHouseJdbcUrlParser();

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ClickHouseConfig config = new ClickHouseConfig(context.getConfig());
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

    }

    private void addConnectionTransformer(final ClickHouseConfig config) {

        // before 0.3.2-patch11
        transformTemplate.transform("ru.yandex.clickhouse.ClickHouseConnectionImpl", ConnectionTransform.class);

        // after 0.3.2
        transformTemplate.transform("com.clickhouse.jdbc.internal.ClickHouseConnectionImpl", ConnectionTransform.class);

    }

    public static class ConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod constructor1 = target.getConstructor("java.lang.String");
            if (constructor1 != null) {
                constructor1.addInterceptor(ClickHouseConnectionCreateInterceptor.class);
            }
            InstrumentMethod constructor2 = target.getConstructor("java.lang.String", "java.util.Properties");
            if (constructor2 != null) {
                constructor2.addInterceptor(ClickHouseConnectionCreateInterceptor.class);
            }
            InstrumentMethod constructor3 = target.getConstructor("com.clickhouse.jdbc.internal.ClickHouseJdbcUrlParser.ConnectionInfo");
            if (constructor3 != null) {
                constructor3.addInterceptor(ClickHouseConnectionCreateInterceptor.class);
            }

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, CLICK_HOUSE_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, CLICK_HOUSE_SCOPE);
            // preparedStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, CLICK_HOUSE_SCOPE);

            // prepareCall not implemented in ClickHouse

            ClickHouseConfig config = new ClickHouseConfig(instrumentor.getProfilerConfig());
            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethod(target, "setAutoCommit", "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, CLICK_HOUSE_SCOPE);
            }
            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, CLICK_HOUSE_SCOPE);
            }

            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, CLICK_HOUSE_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addDriverTransformer() {

        // before 0.3.2-patch11
        transformTemplate.transform("ru.yandex.clickhouse.ClickHouseDriver", DriverTransform.class);
        // after 0.3.2
        transformTemplate.transform("com.clickhouse.jdbc.ClickHouseDriver", DriverTransform.class);

    }

    public static class DriverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentUtils.findMethod(target, "connect", "java.lang.String", "java.util.Properties")
                    .addScopedInterceptor(DriverConnectInterceptorV2.class, va(ClickHouseConstants.CLICK_HOUSE, true), CLICK_HOUSE_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addPreparedStatementTransformer(final ClickHouseConfig config) {

        // before 0.3.2-patch11
        transformTemplate.transform("ru.yandex.clickhouse.ClickHousePreparedStatementImpl", PreparedStatementTransform.class);

        // after 0.3.2
        // added after 0.3.2-test1
        transformTemplate.transform("com.clickhouse.jdbc.internal.SqlBasedPreparedStatement", PreparedStatementTransform.class);

        // added after 0.3.2-test3
        transformTemplate.transform("com.clickhouse.jdbc.internal.InputBasedPreparedStatement", PreparedStatementTransform.class);
        transformTemplate.transform("com.clickhouse.jdbc.internal.TableBasedPreparedStatement", PreparedStatementTransform.class);

    }

    public static class PreparedStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            ClickHouseConfig config = new ClickHouseConfig(instrumentor.getProfilerConfig());
            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), CLICK_HOUSE_SCOPE);
            }
            InstrumentMethod executeQueryMethod = target.getDeclaredMethod("executeQuery");
            if (executeQueryMethod != null) {
                executeQueryMethod.addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), CLICK_HOUSE_SCOPE);
            }
            InstrumentMethod executeUpdateMethod = target.getDeclaredMethod("executeUpdate");
            if (executeUpdateMethod != null) {
                executeUpdateMethod.addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), CLICK_HOUSE_SCOPE);
            }

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, CLICK_HOUSE_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }


    private void addStatementTransformer() {

        // before 0.3.2-patch11
        transformTemplate.transform("ru.yandex.clickhouse.ClickHouseStatementImpl", StatementTransformer.class);

        // after 0.3.2
        transformTemplate.transform("com.clickhouse.jdbc.internal.ClickHouseStatementImpl", StatementTransformer.class);

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
            InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, CLICK_HOUSE_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, CLICK_HOUSE_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, CLICK_HOUSE_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, CLICK_HOUSE_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, CLICK_HOUSE_SCOPE);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
