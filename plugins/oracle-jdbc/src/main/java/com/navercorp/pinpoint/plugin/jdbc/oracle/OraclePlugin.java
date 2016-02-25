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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 */
public class OraclePlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String CLASS_STATEMENT_WRAPPER = "oracle.jdbc.driver.OracleStatementWrapper";
    private static final String CLASS_STATEMENT = "oracle.jdbc.driver.OracleStatement";
    private static final String CLASS_PREPARED_STATEMENT_WRAPPER = "oracle.jdbc.driver.OraclePreparedStatementWrapper";
    private static final String CLASS_PREPARED_STATEMENT = "oracle.jdbc.driver.OraclePreparedStatement";
    private static final String CLASS_CALLABLE_STATEMENT_WRAPPER = "oracle.jdbc.driver.OracleCallableStatementWrapper";
    private static final String CLASS_CALLABLE_STATEMENT = "oracle.jdbc.driver.OracleCallableStatement";

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        OracleConfig config = new OracleConfig(context.getConfig());

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


                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", OracleConstants.ORACLE_SCOPE);
                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor", OracleConstants.ORACLE_SCOPE);
                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor", OracleConstants.ORACLE_SCOPE);

                if (config.isProfileSetAutoCommit()) {
                    target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", OracleConstants.ORACLE_SCOPE);
                }

                if (config.isProfileCommit()) {
                    target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", OracleConstants.ORACLE_SCOPE);
                }

                if (config.isProfileRollback()) {
                    target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", OracleConstants.ORACLE_SCOPE);
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

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor", va(new OracleJdbcUrlParser()), OracleConstants.ORACLE_SCOPE, ExecutionPolicy.ALWAYS);

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

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor", va(maxBindValueSize), OracleConstants.ORACLE_SCOPE);

                if (config.isTraceSqlBindValue()) {
                    target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", OracleConstants.ORACLE_SCOPE);
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

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.CallableStatementRegisterOutParameterInterceptor", OracleConstants.ORACLE_SCOPE);

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

                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor", OracleConstants.ORACLE_SCOPE);
                target.addScopedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor", OracleConstants.ORACLE_SCOPE);

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
