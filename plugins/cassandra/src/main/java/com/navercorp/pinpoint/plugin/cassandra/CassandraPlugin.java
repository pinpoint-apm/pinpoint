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
package com.navercorp.pinpoint.plugin.cassandra;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

/**
 * @author dawidmalina
 */
public class CassandraPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    // AbstractSession got added in 2.0.9
    private static final String CLASS_SESSION_MANAGER = "com.datastax.driver.core.SessionManager";
    private static final String CLASS_ABSTRACT_SESSION = "com.datastax.driver.core.AbstractSession";

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        CassandraConfig config = new CassandraConfig(context.getConfig());

        if (!config.isPluginEnable()) {
            logger.info("Cassandra plugin is not executed because plugin enable value is false.");
            return;
        }

        addDefaultPreparedStatementTransformer();
        addSessionTransformer(config);
        addClusterTransformer();
    }

    private void addDefaultPreparedStatementTransformer() {
        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                            throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.datastax.driver.core.DefaultPreparedStatement", transformer);
    }

    private void addSessionTransformer(final CassandraConfig config) {

        TransformCallback transformer = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                            throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                if (className.equals(CLASS_SESSION_MANAGER)) {
                    if (instrumentor.exist(loader, CLASS_ABSTRACT_SESSION)) {
                        return null;
                    }
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");

                InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
                close.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraConnectionCloseInterceptor",
                        CassandraConstants.CASSANDRA_SCOPE);

                InstrumentMethod prepare1 = InstrumentUtils.findMethod(target, "prepare", "java.lang.String");
                prepare1.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraPreparedStatementCreateInterceptor",
                        CassandraConstants.CASSANDRA_SCOPE);
                InstrumentMethod prepare2 = InstrumentUtils.findMethod(target, "prepare", "com.datastax.driver.core.RegularStatement");
                prepare2.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraPreparedStatementCreateInterceptor",
                        CassandraConstants.CASSANDRA_SCOPE);

                InstrumentMethod execute1 = InstrumentUtils.findMethod(target, "execute", "java.lang.String");
                execute1.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraStatementExecuteQueryInterceptor",
                        va(config.getMaxSqlBindValueSize()), CassandraConstants.CASSANDRA_SCOPE);
                InstrumentMethod execute2 = InstrumentUtils.findMethod(target, "execute", "java.lang.String", "java.lang.Object[]");
                execute2.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraStatementExecuteQueryInterceptor",
                        va(config.getMaxSqlBindValueSize()), CassandraConstants.CASSANDRA_SCOPE);
                InstrumentMethod execute3 = InstrumentUtils.findMethod(target, "execute", "com.datastax.driver.core.Statement");
                execute3.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraStatementExecuteQueryInterceptor",
                        va(config.getMaxSqlBindValueSize()), CassandraConstants.CASSANDRA_SCOPE);

                return target.toBytecode();
            }
        };

        transformTemplate.transform(CLASS_SESSION_MANAGER, transformer);
        transformTemplate.transform(CLASS_ABSTRACT_SESSION, transformer);

    }

    private void addClusterTransformer() {
        transformTemplate.transform("com.datastax.driver.core.Cluster", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                            throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "connect", "java.lang.String");
                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraDriverConnectInterceptor",
                        va(true), CassandraConstants.CASSANDRA_SCOPE, ExecutionPolicy.ALWAYS);

                InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
                close.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.cassandra.interceptor.CassandraConnectionCloseInterceptor",
                        CassandraConstants.CASSANDRA_SCOPE);

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
