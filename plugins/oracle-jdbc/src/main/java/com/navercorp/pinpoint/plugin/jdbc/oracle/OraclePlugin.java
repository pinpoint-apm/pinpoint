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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 */
public class OraclePlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String ORACLE_SCOPE = OracleConstants.ORACLE_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private static final String CLASS_STATEMENT_WRAPPER = "oracle.jdbc.driver.OracleStatementWrapper";
    private static final String CLASS_STATEMENT = "oracle.jdbc.driver.OracleStatement";
    private static final String CLASS_PREPARED_STATEMENT_WRAPPER = "oracle.jdbc.driver.OraclePreparedStatementWrapper";
    private static final String CLASS_PREPARED_STATEMENT = "oracle.jdbc.driver.OraclePreparedStatement";
    private static final String CLASS_CALLABLE_STATEMENT_WRAPPER = "oracle.jdbc.driver.OracleCallableStatementWrapper";
    private static final String CLASS_CALLABLE_STATEMENT = "oracle.jdbc.driver.OracleCallableStatement";

    private TransformTemplate transformTemplate;

    private final JdbcUrlParserV2 jdbcUrlParser = new OracleJdbcUrlParser();

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        OracleConfig config = new OracleConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("Oracle plugin is not executed because plugin enable value is false.");
            return;
        }

        context.addJdbcUrlParser(jdbcUrlParser);

        addConnectionTransformer(config);
        addDriverTransformer();
        addPreparedStatementTransformer(config);
        addCallableStatementTransformer();
        addStatementTransformer();
    }

    private void addConnectionTransformer(final OracleConfig config) {
        transformTemplate.transform("oracle.jdbc.driver.PhysicalConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");


                // close
                InstrumentUtils.findMethod(target, "close")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", ORACLE_SCOPE);

                // createStatement
                final String statementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "createStatement")
                        .addScopedInterceptor(statementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                        .addScopedInterceptor(statementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                        .addScopedInterceptor(statementCreate, ORACLE_SCOPE);

                // preparedStatement
                final String preparedStatementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                // preparecall
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, ORACLE_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", ORACLE_SCOPE);
                }

                if (config.isProfileCommit()) {
                    InstrumentUtils.findMethod(target, "commit")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", ORACLE_SCOPE);
                }

                if (config.isProfileRollback()) {
                    InstrumentUtils.findMethod(target, "rollback")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", ORACLE_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addDriverTransformer() {
        transformTemplate.transform("oracle.jdbc.driver.OracleDriver", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2", va(OracleConstants.ORACLE), ORACLE_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }

    private void addPreparedStatementTransformer(final OracleConfig config) {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                if (className.equals(CLASS_PREPARED_STATEMENT)) {
                    if (instrumentor.exist(loader, CLASS_PREPARED_STATEMENT_WRAPPER)) {
                        return null;
                    }
                }

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String preparedStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), ORACLE_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    MethodFilter filter = new PreparedStatementBindingMethodFilter();
                    List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(filter);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", ORACLE_SCOPE);
                    }
                }

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_PREPARED_STATEMENT, transformer);
        transformTemplate.transform(CLASS_PREPARED_STATEMENT_WRAPPER, transformer);
    }

    private void addCallableStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                if (className.equals(CLASS_CALLABLE_STATEMENT)) {
                    if (instrumentor.exist(loader, CLASS_CALLABLE_STATEMENT_WRAPPER)) {
                        return null;
                    }
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                final String callableStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor";
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                        .addScopedInterceptor(callableStatementInterceptor, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                        .addScopedInterceptor(callableStatementInterceptor, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                        .addScopedInterceptor(callableStatementInterceptor, ORACLE_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_CALLABLE_STATEMENT, transformer);
        transformTemplate.transform(CLASS_CALLABLE_STATEMENT_WRAPPER, transformer);
    }

    private void addStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                if (className.equals(CLASS_STATEMENT)) {
                    if (instrumentor.exist(loader, CLASS_STATEMENT_WRAPPER)) {
                        return null;
                    }
                }

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                final String executeQueryInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                        .addScopedInterceptor(executeQueryInterceptor, ORACLE_SCOPE);

                final String executeUpdateInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor";
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, ORACLE_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, ORACLE_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_STATEMENT, transformer);
        transformTemplate.transform(CLASS_STATEMENT_WRAPPER, transformer);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
