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
package com.navercorp.pinpoint.plugin.cassandra4;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.SettableByIndexSetInterceptor;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.DefaultPreparedStatementInterceptor;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.DefaultSessionCloseInterceptor;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.DefaultSessionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.DefaultSessionExecuteInterceptor;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.DefaultSessionInitInterceptor;
import com.navercorp.pinpoint.plugin.cassandra4.interceptor.DefaultSimpleStatementConstructorInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class CassandraPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        CassandraConfig config = new CassandraConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // 4.x
        transformTemplate.transform("com.datastax.oss.driver.internal.core.session.DefaultSession", DefaultSessionTransformer.class);
        if (config.isTraceSqlBindValue()) {
            // Statement
            transformTemplate.transform("com.datastax.oss.driver.internal.core.cql.DefaultSimpleStatement", DefaultSimpleStatementTransformer.class);
            transformTemplate.transform("com.datastax.oss.driver.internal.core.cql.DefaultPreparedStatement", DefaultPreparedStatementTransformer.class);
            transformTemplate.transform("com.datastax.oss.driver.internal.core.cql.DefaultBoundStatement", StatementTransformer.class);
            transformTemplate.transform("com.datastax.oss.driver.api.core.data.SettableByIndex", SettableByIndexTransformer.class);
            transformTemplate.transform("com.datastax.oss.driver.internal.core.cql.DefaultBatchStatement", StatementTransformer.class);
            transformTemplate.transform("com.datastax.dse.driver.internal.core.graph.BytecodeGraphStatement", StatementTransformer.class);
            transformTemplate.transform("com.datastax.dse.driver.internal.core.graph.DefaultBatchGraphStatement", StatementTransformer.class);
            transformTemplate.transform("com.datastax.dse.driver.internal.core.graph.DefaultFluentGraphStatement", StatementTransformer.class);
            transformTemplate.transform("com.datastax.dse.driver.internal.core.graph.DefaultScriptGraphStatement", StatementTransformer.class);
        }
    }

    public static class DefaultSessionTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(BindValueAccessor.class);
            target.addField(HostListAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("com.datastax.oss.driver.internal.core.context.InternalDriverContext", "java.util.Set");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(DefaultSessionConstructorInterceptor.class);
            }

            final InstrumentMethod initMethod = target.getDeclaredMethod("init", "com.datastax.oss.driver.api.core.CqlIdentifier");
            if (initMethod != null) {
                initMethod.addInterceptor(DefaultSessionInitInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("close"))) {
                if (method != null) {
                    method.addScopedInterceptor(DefaultSessionCloseInterceptor.class, CassandraConstants.CASSANDRA_SCOPE);
                }
            }

            final CassandraConfig config = new CassandraConfig(instrumentor.getProfilerConfig());
            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute", "com.datastax.oss.driver.api.core.session.Request", "com.datastax.oss.driver.api.core.type.reflect.GenericType");
            if (executeMethod != null) {
                executeMethod.addInterceptor(DefaultSessionExecuteInterceptor.class, va(config.getMaxSqlBindValueSize()));
            }

            return target.toBytecode();
        }
    }

    public static class DefaultSimpleStatementTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(BindValueAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(DefaultSimpleStatementConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class DefaultPreparedStatementTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(BindValueAccessor.class);

            final InstrumentMethod bindMethod = target.getDeclaredMethod("bind", "java.lang.Object[]");
            if (bindMethod != null) {
                bindMethod.addInterceptor(DefaultPreparedStatementInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SettableByIndexTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("set", "setToNull", "setBoolean", "setByte", "setDouble", "setFloat", "setInt", "setLong", "setShort", "setInstant", "setLocalDate", "setLocalTime", "setByteBuffer", "setString", "setBigInteger", "setBigDecimal", "setUuid", "setInetAddress", "setCqlDuration", "setToken", "setList", "setSet", "setMap", "setUdtValue", "setTupleValue"))) {
                method.addScopedInterceptor(SettableByIndexSetInterceptor.class, CassandraConstants.CASSANDRA_SCOPE);
            }

            return target.toBytecode();
        }
    }

    public static class StatementTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(DatabaseInfoAccessor.class);
            target.addField(BindValueAccessor.class);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
