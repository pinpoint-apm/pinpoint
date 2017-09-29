/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hikaricp;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
public class HikariCpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private HikariCpConfig config;

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        config = new HikariCpConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("Disable hikaricp option. 'profiler.jdbc.hikaricp=false'");
            return;
        }

        addBasicDataSourceTransformer();
        if (config.isProfileClose()) {
            addPoolGuardConnectionWrapperTransformer();
        }
        addHikariPoolTransformer();
    }

    private void addBasicDataSourceTransformer() {
        transformTemplate.transform("com.zaxxer.hikari.HikariDataSource", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                // getConnection method
                InstrumentMethod getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection");
                getConnectionMethod.addScopedInterceptor(HikariCpConstants.INTERCEPTOR_GET_CONNECTION, HikariCpConstants.SCOPE);

                getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection", new String[]{"java.lang.String", "java.lang.String"});
                getConnectionMethod.addScopedInterceptor(HikariCpConstants.INTERCEPTOR_GET_CONNECTION, HikariCpConstants.SCOPE);

                return target.toBytecode();
            }

        });
    }

    private void addPoolGuardConnectionWrapperTransformer() {
        // 2.4.2 ~
        transformTemplate.transform("com.zaxxer.hikari.pool.ProxyConnection", new ConnectionTransformCallback());

        // 1.1.1 ~ 2.4.1
        transformTemplate.transform("com.zaxxer.hikari.proxy.ConnectionProxy", new ConnectionTransformCallback());
    }

    private void addHikariPoolTransformer() {
        // 1.3.7 ~ 2.6.x (without 2.3.x)
        transformTemplate.transform("com.zaxxer.hikari.pool.HikariPool", new HikariPoolTransformCallback());

        // 2.3.x
        transformTemplate.transform("com.zaxxer.hikari.pool.BaseHikariPool", new HikariPoolTransformCallback());
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private static class ConnectionTransformCallback implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // close method
            InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
            closeMethod.addScopedInterceptor(HikariCpConstants.INTERCEPTOR_CLOSE_CONNECTION, HikariCpConstants.SCOPE);

            return target.toBytecode();
        }

    }

    private static class HikariPoolTransformCallback implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (isAvailableDataSourceMonitor(target)) {
                // ~ 2.4.0
                InstrumentMethod constructor = target.getConstructor("com.zaxxer.hikari.HikariConfig", "java.lang.String", "java.lang.String");
                if (constructor != null) {
                    addDataSourceMonitorInterceptor(target, constructor);
                    return target.toBytecode();
                }

                // 2.4.1 ~
                constructor = target.getConstructor("com.zaxxer.hikari.HikariConfig");
                if (constructor != null) {
                    addDataSourceMonitorInterceptor(target, constructor);
                    return target.toBytecode();
                }
            }
            return target.toBytecode();
        }

        private boolean isAvailableDataSourceMonitor(InstrumentClass target) {
            InstrumentMethod getActiveConnectionsMethod = target.getDeclaredMethod("getActiveConnections");
            if (getActiveConnectionsMethod == null || !int.class.getName().equals(getActiveConnectionsMethod.getReturnType())) {
                return false;
            }

            InstrumentMethod getTotalConnectionsMethod = target.getDeclaredMethod("getTotalConnections");
            if (getTotalConnectionsMethod == null || !int.class.getName().equals(getTotalConnectionsMethod.getReturnType())) {
                return false;
            }

            return true;
        }

        private void addDataSourceMonitorInterceptor(InstrumentClass target, InstrumentMethod constructor) throws InstrumentException {
            target.addField(HikariCpConstants.ACCESSOR_DATASOURCE_MONITOR);

            // constructor
            constructor.addScopedInterceptor(HikariCpConstants.INTERCEPTOR_CONSTRUCTOR, HikariCpConstants.SCOPE);

            // shutdown method
            InstrumentMethod shutdownMethod = InstrumentUtils.findMethod(target, "shutdown");
            shutdownMethod.addScopedInterceptor(HikariCpConstants.INTERCEPTOR_CLOSE, HikariCpConstants.SCOPE);
        }

    }

}
