/*
 * Copyright 2022 NAVER Corp.
 *
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

package com.navercorp.pinpoint.plugin.spring.r2dbc;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.ConnectionCreateStatementInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.ConnectionFactoryCreateInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.DefaultDatabaseClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.DefaultDatabaseClientInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.DefaultFetchSpecInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.DefaultGenericExecuteSpecInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.DefaultGenericExecuteSpecResultFunctionApplyInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.DefaultGenericExecuteSpecStatementFunctionGetLambdaInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.SetDatabaseInfoConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.StatementBindInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.StatementBindNullInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.StatementExecuteInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2.H2ConnectionConfigurationConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2.H2ConnectionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2.H2ConnectionFactoryConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.h2.SessionClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.jasync.ConfigurationConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.jasync.JasyncClientConnectionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.jasync.JasyncConnectionFactoryConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.jasync.MySQLConnectionFactoryConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mariadb.MariadbConnectionConfigurationConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mariadb.MariadbConnectionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mariadb.MariadbConnectionFactoryTransformConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlConnectionConfigurationConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlConnectionConfigurationToConnectionOptionsInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlConnectionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlConnectionFactoryConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlStatementBindInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlStatementBindNullInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mssql.MssqlStatementExecuteInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql.MySqlConnectionConfigurationInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql.MySqlConnectionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql.MySqlConnectionFactoryFromInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql.QueryFlowLoginInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.mysql.ReactorNettyClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.oracle.OracleConnectionFactoryImplConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.oracle.OracleConnectionFactoryImplLambdaCreateInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.oracle.OracleConnectionImplConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.postgresql.PostgresqlConnectionConfigurationInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.postgresql.PostgresqlConnectionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor.postgresql.PostgresqlConnectionFactoryConstructorInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class SpringDataR2dbcPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(context.getConfig());
        if (Boolean.FALSE == config.isEnabled()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (config.getPostgresqlConfig().isPluginEnable()) {
            // Postgresql
            transformTemplate.transform("io.r2dbc.postgresql.PostgresqlConnectionConfiguration", PostgresqlConnectionConfigurationTransform.class);
            transformTemplate.transform("io.r2dbc.postgresql.PostgresqlConnectionFactory", PostgresqlConnectionFactoryTransform.class);
            transformTemplate.transform("io.r2dbc.postgresql.PostgresqlConnection", PostgresqlConnectionTransform.class);
            transformTemplate.transform("io.r2dbc.postgresql.PostgresqlStatement", PostgresqlStatementTransform.class);
        }
        if (config.getH2Config().isPluginEnable()) {
            // H2
            transformTemplate.transform("io.r2dbc.h2.H2ConnectionConfiguration", H2ConnectionConfigurationTransform.class);
            transformTemplate.transform("io.r2dbc.h2.H2ConnectionFactory", H2ConnectionFactoryTransform.class);
            transformTemplate.transform("io.r2dbc.h2.client.SessionClient", SessionClientTransform.class);
            transformTemplate.transform("io.r2dbc.h2.H2Connection", H2ConnectionTransform.class);
            transformTemplate.transform("io.r2dbc.h2.H2Statement", H2StatementTransform.class);
        }
        if (config.getMysqlConfig().isPluginEnable()) {
            // MySQL
            transformTemplate.transform("dev.miku.r2dbc.mysql.MySqlConnectionConfiguration", MySqlConnectionConfigurationTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.MySqlConnectionFactory", MySqlConnectionFactoryTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.MySqlConnection", MySqlConnectionTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.PrepareSimpleStatement", MySqlStatementTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.TextSimpleStatement", MySqlStatementTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.PrepareParametrizedStatement", MySqlStatementTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.TextParametrizedStatement", MySqlStatementTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.SimpleStatementSupport", MySqlStatementTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.ParametrizedStatementSupport", MySqlStatementTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.client.ReactorNettyClient", ReactorNettyClientTransform.class);
            transformTemplate.transform("dev.miku.r2dbc.mysql.QueryFlow", QueryFlowTransform.class);
            // MySQL - Jasync
            transformTemplate.transform("com.github.jasync.sql.db.Configuration", JasyncConfigurationTransform.class);
            transformTemplate.transform("com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory", JasyncMySQLConnectionFactoryTransform.class);
            transformTemplate.transform("com.github.jasync.r2dbc.mysql.JasyncConnectionFactory", JasyncConnectionFactoryTransform.class);
            transformTemplate.transform("com.github.jasync.r2dbc.mysql.JasyncClientConnection", JasyncClientConnectionTransform.class);
            transformTemplate.transform("com.github.jasync.r2dbc.mysql.JasyncStatement", JasyncStatementTransform.class);
        }
        if (config.getMariadbConfig().isPluginEnable()) {
            // Mariadb
            transformTemplate.transform("org.mariadb.r2dbc.MariadbConnectionConfiguration", MariadbConnectionConfigurationTransform.class);
            transformTemplate.transform("org.mariadb.r2dbc.MariadbConnectionFactory", MariadbConnectionFactoryTransform.class);
            transformTemplate.transform("org.mariadb.r2dbc.MariadbConnection", MariadbConnectionTransform.class);
            transformTemplate.transform("org.mariadb.r2dbc.MariadbClientParameterizedQueryStatement", MariadbStatementTransform.class);
            transformTemplate.transform("org.mariadb.r2dbc.MariadbServerParameterizedQueryStatement", MariadbStatementTransform.class);
            transformTemplate.transform("org.mariadb.r2dbc.MariadbCommonStatement", MariadbStatementTransform.class);
        }
        if (config.getOracleConfig().isPluginEnable()) {
            // Oracle
            transformTemplate.transform("oracle.r2dbc.impl.OracleConnectionFactoryImpl", OracleConnectionFactoryImplTransform.class);
            transformTemplate.transform("oracle.r2dbc.impl.OracleReactiveJdbcAdapter", OracleReactiveJdbcAdapterTransform.class);
            final Matcher oracleConnectionFactoryImplLambdaMatcher = Matchers.newLambdaExpressionMatcher("oracle.r2dbc.impl.OracleConnectionFactoryImpl", "java.util.function.Function");
            transformTemplate.transform(oracleConnectionFactoryImplLambdaMatcher, OracleConnectionFactoryImplLambdaTransform.class);
            transformTemplate.transform("oracle.r2dbc.impl.OracleConnectionImpl", OracleConnectionImplTransform.class);
            transformTemplate.transform("oracle.r2dbc.impl.OracleStatementImpl", OracleStatementImplTransform.class);
        }
        if (config.getMssqlConfig().isPluginEnable()) {
            // Mssql
            transformTemplate.transform("io.r2dbc.mssql.MssqlConnectionConfiguration", MssqlConnectionConfigurationTransform.class);
            transformTemplate.transform("io.r2dbc.mssql.MssqlConnectionFactory", MssqlConnectionFactoryTransform.class);
            transformTemplate.transform("io.r2dbc.mssql.ConnectionOptions", ConnectionOptionsTransform.class);
            transformTemplate.transform("io.r2dbc.mssql.MssqlConnection", MssqlConnectionTransform.class);
            transformTemplate.transform("io.r2dbc.mssql.ParametrizedMssqlStatement", MssqlStatementTransform.class);
            transformTemplate.transform("io.r2dbc.mssql.SimpleMssqlStatement", MssqlStatementTransform.class);
        }
        // Spring data r2dbc
        transformTemplate.transform("org.springframework.r2dbc.core.DefaultDatabaseClient", DefaultDatabaseClientTransform.class);
        transformTemplate.transform("org.springframework.r2dbc.core.DefaultDatabaseClient$DefaultGenericExecuteSpec", DefaultGenericExecuteSpecTransform.class);
        transformTemplate.transform("org.springframework.r2dbc.core.DefaultFetchSpec", DefaultFetchSpecTransform.class);

        // ConnectionFactory
        transformTemplate.transform("io.r2dbc.pool.ConnectionPoolConfiguration", ConnectionPoolConfigurationTransform.class);
        transformTemplate.transform("io.r2dbc.pool.ConnectionPool", ConnectionPoolTransform.class);
        transformTemplate.transform("org.springframework.boot.r2dbc.OptionsCapableConnectionFactory", OptionsCapableConnectionFactoryTransform.class);
        transformTemplate.transform("org.springframework.r2dbc.connection.DelegatingConnectionFactory", DelegatingConnectionFactoryTransform.class);
        transformTemplate.transform("org.springframework.r2dbc.connection.SingleConnectionFactory", SingleConnectionFactoryTransform.class);
        transformTemplate.transform("org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy", TransactionAwareConnectionFactoryProxyTransform.class);

        // statementFunction, resultFunction Lambda
        final Matcher defaultGenericExecuteSpecLambdaMatcher = Matchers.newLambdaExpressionMatcher("org.springframework.r2dbc.core.DefaultDatabaseClient$DefaultGenericExecuteSpec", "java.util.function.Function");
        transformTemplate.transform(defaultGenericExecuteSpecLambdaMatcher, DefaultGenericExecuteSpecStatementFunctionTransform.class);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class PostgresqlConnectionConfigurationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            // 0.8.x
            InstrumentMethod constructorMethod = target.getConstructor("java.lang.String", "boolean", "boolean", "java.time.Duration", "java.lang.String", "java.util.List", "java.util.function.ToIntFunction", "boolean", "java.lang.String", "java.time.Duration", "reactor.netty.resources.LoopResources", "java.util.Map", "java.lang.CharSequence", "int", "boolean", "int", "java.lang.String", "java.lang.String", "io.r2dbc.postgresql.client.SSLConfig", "java.time.Duration", "boolean", "boolean", "java.lang.String");
            if (constructorMethod == null) {
                // 0.9.x
                constructorMethod = target.getConstructor("java.lang.String", "boolean", "boolean", "java.time.Duration", "java.lang.String", "io.r2dbc.postgresql.util.LogLevel", "java.util.List", "java.util.function.ToIntFunction", "boolean", "java.lang.String", "java.time.Duration", "reactor.netty.resources.LoopResources", "io.r2dbc.postgresql.util.LogLevel", "java.util.Map", "java.lang.CharSequence", "int", "boolean", "int", "java.lang.String", "java.lang.String", "io.r2dbc.postgresql.client.SSLConfig", "java.time.Duration", "boolean", "boolean", "java.lang.String");
            }
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(PostgresqlConnectionConfigurationInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class PostgresqlConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(PostgresqlConnectionFactoryConstructorInterceptor.class);
                }
            }
            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class PostgresqlConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("io.r2dbc.postgresql.client.Client", "io.r2dbc.postgresql.codec.Codecs", "io.r2dbc.postgresql.client.PortalNameSupplier", "io.r2dbc.postgresql.StatementCache", "io.r2dbc.spi.IsolationLevel", "io.r2dbc.postgresql.PostgresqlConnectionConfiguration");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(PostgresqlConnectionConstructorInterceptor.class);
            }

            final InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class PostgresqlStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig postgresqlConfig = config.getPostgresqlConfig();
            if (postgresqlConfig.isTraceSqlBindValue()) {
                final InstrumentMethod bindNullMethod = target.getDeclaredMethod("bindNull", "int", "java.lang.Class");
                if (bindNullMethod != null) {
                    bindNullMethod.addInterceptor(StatementBindNullInterceptor.class);
                }
                final InstrumentMethod addIndexMethod = target.getDeclaredMethod("bind", "int", "java.lang.Object");
                if (addIndexMethod != null) {
                    addIndexMethod.addInterceptor(StatementBindInterceptor.class);
                }
            }

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(StatementExecuteInterceptor.class, va(postgresqlConfig.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class H2ConnectionConfigurationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(H2ConnectionConfigurationConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class H2ConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(H2ConnectionFactoryConstructorInterceptor.class);
                }
            }

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SessionClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("org.h2.engine.ConnectionInfo", "boolean");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(SessionClientConstructorInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class H2ConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("io.r2dbc.h2.client.Client", "io.r2dbc.h2.codecs.Codecs");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(H2ConnectionConstructorInterceptor.class);
            }

            InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class H2StatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig h2Config = config.getH2Config();

            if (h2Config.isTraceSqlBindValue()) {
                final InstrumentMethod bindNullMethod = target.getDeclaredMethod("bindNull", "int", "java.lang.Class");
                if (bindNullMethod != null) {
                    bindNullMethod.addInterceptor(StatementBindNullInterceptor.class);
                }
                final InstrumentMethod addIndexMethod = target.getDeclaredMethod("addIndex", "int", "java.lang.Object");
                if (addIndexMethod != null) {
                    addIndexMethod.addInterceptor(StatementBindInterceptor.class);
                }
            }

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(StatementExecuteInterceptor.class, va(h2Config.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class MySqlConnectionConfigurationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MySqlConnectionConfigurationInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class MySqlConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod fromMethod = target.getDeclaredMethod("from", "dev.miku.r2dbc.mysql.MySqlConnectionConfiguration");
            if (fromMethod != null) {
                fromMethod.addInterceptor(MySqlConnectionFactoryFromInterceptor.class);
            }

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MySqlConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("dev.miku.r2dbc.mysql.client.Client", "dev.miku.r2dbc.mysql.ConnectionContext", "dev.miku.r2dbc.mysql.codec.Codecs", "io.r2dbc.spi.IsolationLevel", "java.lang.String", "java.util.function.Predicate");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(MySqlConnectionConstructorInterceptor.class);
            }

            final InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MySqlStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig mysqlConfig = config.getMysqlConfig();
            if (mysqlConfig.isTraceSqlBindValue()) {
                InstrumentMethod bindNullMethod = target.getDeclaredMethod("bindNull", "int", "java.lang.Class");
                if (bindNullMethod != null) {
                    bindNullMethod.addInterceptor(StatementBindNullInterceptor.class);
                }
                InstrumentMethod addIndexMethod = target.getDeclaredMethod("bind", "int", "java.lang.Object");
                if (addIndexMethod != null) {
                    addIndexMethod.addInterceptor(StatementBindInterceptor.class);
                }
            }

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(StatementExecuteInterceptor.class, va(mysqlConfig.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }


    public static class ReactorNettyClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("reactor.netty.Connection", "dev.miku.r2dbc.mysql.MySqlSslConfiguration", "dev.miku.r2dbc.mysql.ConnectionContext");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(ReactorNettyClientConstructorInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class QueryFlowTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod loginMethod = target.getDeclaredMethod("login", "dev.miku.r2dbc.mysql.client.Client", "dev.miku.r2dbc.mysql.constant.SslMode", "java.lang.String", "java.lang.String", "java.lang.CharSequence", "dev.miku.r2dbc.mysql.ConnectionContext");
            if (loginMethod != null) {
                loginMethod.addInterceptor(QueryFlowLoginInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    // jasync-sql
    public static class JasyncConfigurationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(ConfigurationConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class JasyncMySQLConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MySQLConnectionFactoryConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class JasyncConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(JasyncConnectionFactoryConstructorInterceptor.class);
                }
            }

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }


    public static class JasyncClientConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("com.github.jasync.sql.db.Connection", "com.github.jasync.sql.db.mysql.pool.MySQLConnectionFactory");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(JasyncClientConnectionConstructorInterceptor.class);
            }
            final InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class JasyncStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig mysqlConfig = config.getMysqlConfig();
            if (mysqlConfig.isTraceSqlBindValue()) {
                InstrumentMethod bindNullMethod = target.getDeclaredMethod("bindNull", "int", "java.lang.Class");
                if (bindNullMethod != null) {
                    bindNullMethod.addInterceptor(StatementBindNullInterceptor.class);
                }
                InstrumentMethod addIndexMethod = target.getDeclaredMethod("bind", "int", "java.lang.Object");
                if (addIndexMethod != null) {
                    addIndexMethod.addInterceptor(StatementBindInterceptor.class);
                }
            }

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(StatementExecuteInterceptor.class, va(mysqlConfig.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class MariadbConnectionConfigurationTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MariadbConnectionConfigurationConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class MariadbConnectionFactoryTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MariadbConnectionFactoryTransformConstructorInterceptor.class);
                }
            }

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MariadbConnectionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MariadbConnectionConstructorInterceptor.class);
                }
            }
            final InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MariadbStatementTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig mariadbConfig = config.getMariadbConfig();
            if (mariadbConfig.isTraceSqlBindValue()) {
                final InstrumentMethod bindNullMethod = target.getDeclaredMethod("bindNull", "int", "java.lang.Class");
                if (bindNullMethod != null) {
                    bindNullMethod.addInterceptor(StatementBindNullInterceptor.class);
                }
                final InstrumentMethod addIndexMethod = target.getDeclaredMethod("bind", "int", "java.lang.Object");
                if (addIndexMethod != null) {
                    addIndexMethod.addInterceptor(StatementBindInterceptor.class);
                }
            }

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(StatementExecuteInterceptor.class, va(mariadbConfig.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class OracleConnectionFactoryImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(OracleConnectionFactoryImplConstructorInterceptor.class);
                }
            }

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class OracleConnectionFactoryImplLambdaTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final InstrumentMethod lambdaMethod = target.getConstructor("oracle.r2dbc.impl.OracleConnectionFactoryImpl", "oracle.r2dbc.impl.ReactiveJdbcAdapter");
            if (lambdaMethod != null) {
                lambdaMethod.addInterceptor(OracleConnectionFactoryImplLambdaCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class OracleReactiveJdbcAdapterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            return target.toBytecode();
        }
    }

    public static class OracleConnectionImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(OracleConnectionImplConstructorInterceptor.class);
                }
            }
            final InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class OracleStatementImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig oracleConfig = config.getOracleConfig();
            if (oracleConfig.isTraceSqlBindValue()) {
                final InstrumentMethod addIndexMethod = target.getDeclaredMethod("bindObject", "int", "java.lang.Object");
                if (addIndexMethod != null) {
                    addIndexMethod.addInterceptor(StatementBindInterceptor.class);
                }
            }

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(StatementExecuteInterceptor.class, va(oracleConfig.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class MssqlConnectionConfigurationTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MssqlConnectionConfigurationConstructorInterceptor.class);
                }
            }

            final InstrumentMethod toConnectionOptionsMethod = target.getDeclaredMethod("toConnectionOptions");
            if (toConnectionOptionsMethod != null) {
                toConnectionOptionsMethod.addInterceptor(MssqlConnectionConfigurationToConnectionOptionsInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MssqlConnectionFactoryTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("io.r2dbc.mssql.MssqlConnectionConfiguration");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(MssqlConnectionFactoryConstructorInterceptor.class);
            }

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ConnectionOptionsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            return target.toBytecode();
        }
    }

    public static class MssqlConnectionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    constructorMethod.addInterceptor(MssqlConnectionConstructorInterceptor.class);
                }
            }
            final InstrumentMethod createStatementMethod = target.getDeclaredMethod("createStatement", "java.lang.String");
            if (createStatementMethod != null) {
                createStatementMethod.addInterceptor(ConnectionCreateStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MssqlStatementTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindNameValueAccessor.class);

            final SpringDataR2dbcConfiguration config = new SpringDataR2dbcConfiguration(instrumentor.getProfilerConfig());
            final JdbcConfig mssqlConfig = config.getMssqlConfig();
            if (mssqlConfig.isTraceSqlBindValue()) {
                InstrumentMethod bindMethod = target.getDeclaredMethod("bind", "java.lang.String", "java.lang.Object");
                if (bindMethod != null) {
                    bindMethod.addInterceptor(MssqlStatementBindInterceptor.class);
                }
                InstrumentMethod bindNullMethod = target.getDeclaredMethod("bindNull", "java.lang.String", "java.lang.Class");
                if (bindNullMethod != null) {
                    bindNullMethod.addInterceptor(MssqlStatementBindNullInterceptor.class);
                }
            }
            // public Publisher<? extends Result> execute()
            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addInterceptor(MssqlStatementExecuteInterceptor.class, va(mssqlConfig.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class DefaultDatabaseClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("org.springframework.r2dbc.core.binding.BindMarkersFactory", "io.r2dbc.spi.ConnectionFactory", "org.springframework.r2dbc.core.ExecuteFunction", "boolean");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(DefaultDatabaseClientConstructorInterceptor.class);
            }

            final InstrumentMethod sqlMethod = target.getDeclaredMethod("sql", "java.util.function.Supplier");
            if (sqlMethod != null) {
                sqlMethod.addInterceptor(DefaultDatabaseClientInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DefaultGenericExecuteSpecTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(AsyncContextAccessor.class);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("bind", "bindNull", "filter"))) {
                method.addInterceptor(DefaultGenericExecuteSpecInterceptor.class);
            }
            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute", "java.util.function.Supplier", "java.util.function.BiFunction");
            if (executeMethod != null) {
                executeMethod.addInterceptor(DefaultGenericExecuteSpecInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DefaultGenericExecuteSpecStatementFunctionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final InstrumentMethod resultFunctionGetLambdaMethod = target.getConstructor("org.springframework.r2dbc.core.DefaultDatabaseClient$DefaultGenericExecuteSpec", "java.util.function.Function", "java.lang.String");
            if (resultFunctionGetLambdaMethod != null) {
                // resultFunction
                target.addField(DatabaseInfoAccessor.class);
                target.addField(AsyncContextAccessor.class);

                resultFunctionGetLambdaMethod.addInterceptor(DefaultGenericExecuteSpecStatementFunctionGetLambdaInterceptor.class);
                final InstrumentMethod applyMethod = target.getDeclaredMethod("apply", "java.lang.Object");
                if (applyMethod != null) {
                    applyMethod.addInterceptor(DefaultGenericExecuteSpecResultFunctionApplyInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class DefaultFetchSpecTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(AsyncContextAccessor.class);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("one", "first", "all", "rowsUpdated"))) {
                method.addScopedInterceptor(DefaultFetchSpecInterceptor.class, "DefaultFetchSpec", ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    public static class ConnectionPoolConfigurationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                if (ArrayUtils.hasLength(constructorMethod.getParameterTypes())) {
                    // ConnectionPoolConfiguration(int acquireRetry, @Nullable Duration backgroundEvictionInterval, ConnectionFactory connectionFactory, ...)
                    constructorMethod.addInterceptor(SetDatabaseInfoConstructorInterceptor.class, va(2));
                }
            }

            return target.toBytecode();
        }
    }

    public static class ConnectionPoolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("io.r2dbc.pool.ConnectionPoolConfiguration");
            if (constructorMethod != null) {
                // ConnectionPool(ConnectionPoolConfiguration configuration)
                constructorMethod.addInterceptor(SetDatabaseInfoConstructorInterceptor.class, va(0));
            }
            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class OptionsCapableConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("io.r2dbc.spi.ConnectionFactoryOptions", "io.r2dbc.spi.ConnectionFactory");
            if (constructorMethod != null) {
                // OptionsCapableConnectionFactory(ConnectionFactoryOptions options, ConnectionFactory delegate)
                constructorMethod.addInterceptor(SetDatabaseInfoConstructorInterceptor.class, va(1));
            }
            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DelegatingConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("io.r2dbc.spi.ConnectionFactory");
            if (constructorMethod != null) {
                // DelegatingConnectionFactory(ConnectionFactory targetConnectionFactory)
                constructorMethod.addInterceptor(SetDatabaseInfoConstructorInterceptor.class, va(0));
            }
            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }


    public static class SingleConnectionFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class TransactionAwareConnectionFactoryProxyTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod createMethod = target.getDeclaredMethod("create");
            if (createMethod != null) {
                createMethod.addInterceptor(ConnectionFactoryCreateInterceptor.class);
            }

            return target.toBytecode();
        }
    }
}