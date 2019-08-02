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
import com.navercorp.pinpoint.plugin.jdbc.postgresql.interceptor.PostgreSQLConnectionCreateInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Brad Hong
 * @author HyunGil Jeong
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
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        context.addJdbcUrlParser(jdbcUrlParser);

        addDriverTransformer();
        addConnectionTransformers(config);
        addStatementTransformers(config);
        addPreparedStatementTransformers(config);

        // pre 9.4.1207
        addLegacyConnectionTransformers(config);
        addLegacyStatementTransformers(config);
    }

    private void addDriverTransformer() {
        transformTemplate.transform("org.postgresql.Driver", DriverTransform.class);
    }

    public static class DriverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentUtils.findMethod(target, "connect", "java.lang.String", "java.util.Properties")
                    .addScopedInterceptor(DriverConnectInterceptorV2.class,
                            va(PostgreSqlConstants.POSTGRESQL, false), POSTGRESQL_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    private void addConnectionTransformers(final PostgreSqlConfig config) {
        transformTemplate.transform("org.postgresql.jdbc.PgConnection", PgConnectionTransform.class);
    }

    public static class PgConnectionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            InstrumentUtils.findConstructor(target, "org.postgresql.util.HostSpec[]", "java.lang.String", "java.lang.String", "java.util.Properties", "java.lang.String")
                    .addInterceptor(PostgreSQLConnectionCreateInterceptor.class);

            target.addField(DatabaseInfoAccessor.class);

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, POSTGRESQL_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement")
                    .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);

            // prepareStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int[]")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);

            PostgreSqlConfig config = new PostgreSqlConfig(instrumentor.getProfilerConfig());
            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethod(target, "setAutoCommit", "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, POSTGRESQL_SCOPE);
            }

            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, POSTGRESQL_SCOPE);
            }

            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, POSTGRESQL_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addStatementTransformers(final PostgreSqlConfig config) {

        transformTemplate.transform("org.postgresql.jdbc.PgStatement", PgStatementTransform.class);
    }

    public static class PgStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, POSTGRESQL_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);

            PostgreSqlConfig config = new PostgreSqlConfig(instrumentor.getProfilerConfig());
            // 9.4.1207 has setX methods in PgStatement
            final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
            final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
            if (!declaredMethods.isEmpty()) {
                target.addField(ParsingResultAccessor.class);
                target.addField(BindValueAccessor.class);

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, POSTGRESQL_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }
            }

            return target.toBytecode();
        }
    };

    private void addPreparedStatementTransformers(final PostgreSqlConfig config) {

        transformTemplate.transform("org.postgresql.jdbc.PgPreparedStatement", PgPreparedStatementTransform.class);
    }

    public static class PgPreparedStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);

            PostgreSqlConfig config = new PostgreSqlConfig(instrumentor.getProfilerConfig());
            // 9.4.1207 has setX methods in PgStatement
            final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
            final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
            if (!declaredMethods.isEmpty()) {
                target.addField(ParsingResultAccessor.class);
                target.addField(BindValueAccessor.class);

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, POSTGRESQL_SCOPE, ExecutionPolicy.BOUNDARY);
                    }
                }
            }

            return target.toBytecode();
        }
    };

    private void addLegacyConnectionTransformers(final PostgreSqlConfig config) {
        transformTemplate.transform("org.postgresql.jdbc2.AbstractJdbc2Connection", AbstractJdbc2ConnectionTransform.class);

        transformTemplate.transform("org.postgresql.jdbc3.AbstractJdbc3Connection", AbstractJdbc3ConnectionTransform.class);


        transformTemplate.transform("org.postgresql.jdbc3.Jdbc3Connection", ConcreteConnectionTransform.class);
        transformTemplate.transform("org.postgresql.jdbc3g.Jdbc3gConnection", ConcreteConnectionTransform.class);
        transformTemplate.transform("org.postgresql.jdbc4.Jdbc4Connection", ConcreteConnectionTransform.class);
        transformTemplate.transform("org.postgresql.jdbc42.Jdbc42Connection", ConcreteConnectionTransform.class);
    }

    public static class AbstractJdbc2ConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, POSTGRESQL_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement")
                    .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);

            // prepareStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);

            PostgreSqlConfig config = new PostgreSqlConfig(instrumentor.getProfilerConfig());
            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethod(target, "setAutoCommit", "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, POSTGRESQL_SCOPE);
            }

            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, POSTGRESQL_SCOPE);
            }

            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, POSTGRESQL_SCOPE);
            }

            return target.toBytecode();
        }
    }

    public static class AbstractJdbc3ConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);

            // prepareStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int[]")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);

            return target.toBytecode();
        }
    }

    public static class ConcreteConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            InstrumentUtils.findConstructor(target, "org.postgresql.util.HostSpec[]", "java.lang.String", "java.lang.String", "java.util.Properties", "java.lang.String")
                    .addInterceptor(PostgreSQLConnectionCreateInterceptor.class);

            // createStatement
            final Class<? extends Interceptor> statementCreate = StatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, POSTGRESQL_SCOPE);

            // prepareStatement
            final Class<? extends Interceptor> preparedStatementCreate = PreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement", "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, POSTGRESQL_SCOPE);

            return target.toBytecode();
        }
    };

    private void addLegacyStatementTransformers(final PostgreSqlConfig config) {
        transformTemplate.transform("org.postgresql.jdbc2.AbstractJdbc2Statement", AbstractJdbc2StatementTransform.class);
        transformTemplate.transform("org.postgresql.jdbc3.AbstractJdbc3Statement", AbstractJdbc3StatementTransform.class);
    }

    public static class AbstractJdbc2StatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            PostgreSqlConfig config = new PostgreSqlConfig(instrumentor.getProfilerConfig());
            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "execute")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), POSTGRESQL_SCOPE);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, POSTGRESQL_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);

            if (config.isTraceSqlBindValue()) {
                final PreparedStatementBindingMethodFilter excludes = PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML");
                final List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(excludes);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, POSTGRESQL_SCOPE, ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }
    }

    public static class AbstractJdbc3StatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);
            InstrumentUtils.findMethod(target, "execute", "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, POSTGRESQL_SCOPE);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
