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
package com.navercorp.pinpoint.plugin.jdbc.informix;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptorV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.InformixStatementCreateInterceptor;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.InformixPreparedStatementCreateInterceptor;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.InformixDatabaseNameGetter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.InformixConnectionInfoGetter;
import com.navercorp.pinpoint.plugin.jdbc.informix.interceptor.getter.Informix_4_50_ConnectionInfoGetter;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Guillermo Adrian Molina
 *
 */
public class InformixPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String INFORMIX_SCOPE = InformixConstants.INFORMIX_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;
    private final JdbcUrlParserV2 jdbcUrlParser = new InformixJdbcUrlParser();

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        InformixConfig config = new InformixConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        context.addJdbcUrlParser(jdbcUrlParser);

        addDriverTransformer();
        addConnectionTransformer(config);
        addStatementTransformer();
        addPreparedStatementTransformer(config);
        addCallableStatementTransformer();
    }

    private void addDriverTransformer() {
        transformTemplate.transform("com.informix.jdbc.IfxDriver", InformixDriverTransform.class);
    }

    public static class InformixDriverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentUtils.findMethod(target, "connect",  "java.lang.String", "java.util.Properties")
                    .addScopedInterceptor(DriverConnectInterceptorV2.class, va(InformixConstants.INFORMIX), INFORMIX_SCOPE, ExecutionPolicy.ALWAYS);

            return target.toBytecode();
        }
    }

    
    private void addConnectionTransformer(final InformixConfig config) {
        transformTemplate.transform("com.informix.jdbc.IfxSqliConnect", InformixConnectionTransformer.class);
    }

    public static class InformixConnectionTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InformixConfig config = new InformixConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);

            target.addGetter(InformixDatabaseNameGetter.class, "dbName");
            if (target.hasField("connInfo", "com.informix.util.AdvancedUppercaseProperties")) {
                // 4.50.x+
                target.addGetter(Informix_4_50_ConnectionInfoGetter.class, "connInfo");
            }
            else {
                // 4.10.x or previous
                target.addGetter(InformixConnectionInfoGetter.class, "connInfo");
            }

            // close
            InstrumentUtils.findMethod(target, "close")
                    .addScopedInterceptor(ConnectionCloseInterceptor.class, INFORMIX_SCOPE);

            // createStatement
            final Class<? extends Interceptor> statementCreate = InformixStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "createStatement")
                    .addScopedInterceptor(statementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int")
                    .addScopedInterceptor(statementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "createStatement", "int", "int", "int")
                    .addScopedInterceptor(statementCreate, INFORMIX_SCOPE);

            // preparedStatement
            final Class<? extends Interceptor> preparedStatementCreate = InformixPreparedStatementCreateInterceptor.class;
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int[]")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "java.lang.String[]")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareStatement",  "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            // preparecall
            InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "prepareCall",  "java.lang.String", "int", "int", "int")
                    .addScopedInterceptor(preparedStatementCreate, INFORMIX_SCOPE);

            if (config.isProfileSetAutoCommit()) {
                InstrumentUtils.findMethod(target, "setAutoCommit",  "boolean")
                        .addScopedInterceptor(TransactionSetAutoCommitInterceptor.class, INFORMIX_SCOPE);
            }

            if (config.isProfileCommit()) {
                InstrumentUtils.findMethod(target, "commit")
                        .addScopedInterceptor(TransactionCommitInterceptor.class, INFORMIX_SCOPE);
            }
            if (config.isProfileRollback()) {
                InstrumentUtils.findMethod(target, "rollback")
                        .addScopedInterceptor(TransactionRollbackInterceptor.class, INFORMIX_SCOPE);
            }

            return target.toBytecode();
        }
    }
    
    private void addStatementTransformer() {
        transformTemplate.transform("com.informix.jdbc.IfxStatement", InformixStatementTransform.class);
    }

    public static class InformixStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);

            final Class<? extends Interceptor> executeQueryInterceptor = StatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "executeQuery", "java.lang.String")
                    .addScopedInterceptor(executeQueryInterceptor, INFORMIX_SCOPE);

            final Class<? extends Interceptor> executeUpdateInterceptor = StatementExecuteUpdateInterceptor.class;
            InstrumentUtils.findMethod(target, "executeUpdate", "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate",  "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "execute",  "java.lang.String")
                    .addScopedInterceptor(executeUpdateInterceptor, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "execute",  "java.lang.String", "int")
                    .addScopedInterceptor(executeUpdateInterceptor, INFORMIX_SCOPE);

            return target.toBytecode();
        }
    }
    
    private void addPreparedStatementTransformer(final InformixConfig config) {
        transformTemplate.transform("com.informix.jdbc.IfxPreparedStatement", InformixPreparedStatement.class);
    }

    public static class InformixPreparedStatement implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InformixConfig config = new InformixConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            int maxBindValueSize = config.getMaxSqlBindValueSize();

            final Class<? extends Interceptor> preparedStatementInterceptor = PreparedStatementExecuteQueryInterceptor.class;
            InstrumentUtils.findMethod(target, "execute")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "executeQuery")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "executeUpdate")
                    .addScopedInterceptor(preparedStatementInterceptor, va(maxBindValueSize), INFORMIX_SCOPE);

            if (config.isTraceSqlBindValue()) {
                MethodFilter filter = new PreparedStatementBindingMethodFilter();
                List<InstrumentMethod> declaredMethods = target.getDeclaredMethods(filter);
                for (InstrumentMethod method : declaredMethods) {
                    method.addScopedInterceptor(PreparedStatementBindVariableInterceptor.class, INFORMIX_SCOPE);
                }
            }

            return target.toBytecode();
        }
    }

    private void addCallableStatementTransformer() {
        transformTemplate.transform("com.informix.jdbc.IfxCallableStatement", InformixCallableStatementTransform.class);
    }

    public static class InformixCallableStatementTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(DatabaseInfoAccessor.class);
            target.addField(ParsingResultAccessor.class);
            target.addField(BindValueAccessor.class);

            final Class<? extends Interceptor> callableStatementInterceptor = CallableStatementRegisterOutParameterInterceptor.class;
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int")
                    .addScopedInterceptor(callableStatementInterceptor, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "int")
                    .addScopedInterceptor(callableStatementInterceptor, INFORMIX_SCOPE);
            InstrumentUtils.findMethod(target, "registerOutParameter", "int", "int", "java.lang.String")
                    .addScopedInterceptor(callableStatementInterceptor, INFORMIX_SCOPE);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
