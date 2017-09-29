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
package com.navercorp.pinpoint.plugin.jdbc.cubrid;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 *
 */
public class CubridPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String CUBRID_SCOPE = CubridConstants.CUBRID_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;
    private final JdbcUrlParserV2 jdbcUrlParser = new CubridJdbcUrlParser();

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        CubridConfig config = new CubridConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("Cubrid plugin is not executed because plugin enable value is false.");
            return;
        }
        context.addJdbcUrlParser(jdbcUrlParser);

        addCUBRIDConnectionTransformer(config);
        addCUBRIDDriverTransformer();
        addCUBRIDPreparedStatementTransformer(config);
        addCUBRIDCallableStatementTransformer();
        addCUBRIDStatementTransformer();
    }

    
    private void addCUBRIDConnectionTransformer(final CubridConfig config) {
        transformTemplate.transform("cubrid.jdbc.driver.CUBRIDConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                // close
                InstrumentUtils.findMethod(target, "close")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", CUBRID_SCOPE);

                // createStatement
                final String statementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "createStatement")
                        .addScopedInterceptor(statementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                        .addScopedInterceptor(statementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                        .addScopedInterceptor(statementCreate, CUBRID_SCOPE);

                // preparedStatement
                final String preparedStatementCreate = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor";
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                // preparecall
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int", "int")
                        .addScopedInterceptor(preparedStatementCreate, CUBRID_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", CUBRID_SCOPE);
                }

                if (config.isProfileCommit()) {
                    InstrumentUtils.findMethod(target, "commit")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", CUBRID_SCOPE);
                }
                if (config.isProfileRollback()) {
                    InstrumentUtils.findMethod(target, "rollback")
                            .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", CUBRID_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addCUBRIDDriverTransformer() {
        transformTemplate.transform("cubrid.jdbc.driver.CUBRIDDriver", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                        .addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2", va(CubridConstants.CUBRID), CUBRID_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }
    
    private void addCUBRIDPreparedStatementTransformer(final CubridConfig config) {
        transformTemplate.transform("cubrid.jdbc.driver.CUBRIDPreparedStatement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                int maxBindValueSize = config.getMaxSqlBindValueSize();

                final String preparedStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "execute")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "executeQuery")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate")
                        .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), CUBRID_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    MethodFilter filter = new PreparedStatementBindingMethodFilter();
                    List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(filter);
                    for (InstrumentMethod method : declaredMethods) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", CUBRID_SCOPE);
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private void addCUBRIDCallableStatementTransformer() {
        transformTemplate.transform("cubrid.jdbc.driver.CUBRIDCallableStatement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                final String callableStatementInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor";
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                        .addScopedInterceptor(callableStatementInterceptor, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                        .addScopedInterceptor(callableStatementInterceptor, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                        .addScopedInterceptor(callableStatementInterceptor, CUBRID_SCOPE);

                return target.toBytecode();
            }
        });
    }
    
    private void addCUBRIDStatementTransformer() {
        transformTemplate.transform("cubrid.jdbc.driver.CUBRIDStatement", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                final String executeQueryInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor";
                InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                        .addScopedInterceptor(executeQueryInterceptor, CUBRID_SCOPE);

                final String executeUpdateInterceptor = "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor";
                InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                        .addScopedInterceptor(executeUpdateInterceptor, CUBRID_SCOPE);
                InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                        .addScopedInterceptor(executeUpdateInterceptor, CUBRID_SCOPE);

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
