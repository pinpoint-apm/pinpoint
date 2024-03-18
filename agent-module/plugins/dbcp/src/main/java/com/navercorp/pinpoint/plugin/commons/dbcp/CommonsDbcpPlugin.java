/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.commons.dbcp;

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
import com.navercorp.pinpoint.plugin.commons.dbcp.interceptor.DataSourceCloseConnectionInterceptor;
import com.navercorp.pinpoint.plugin.commons.dbcp.interceptor.DataSourceCloseInterceptor;
import com.navercorp.pinpoint.plugin.commons.dbcp.interceptor.DataSourceConstructorInterceptor;
import com.navercorp.pinpoint.plugin.commons.dbcp.interceptor.DataSourceGetConnectionInterceptor;

import java.security.ProtectionDomain;

/**
 * @author Jongho Moon
 */
public class CommonsDbcpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        CommonsDbcpConfig config = new CommonsDbcpConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled {}", this.getClass().getSimpleName(), "'profiler.jdbc.dbcp=false'");
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        addBasicDataSourceTransformer();
        if (config.isProfileClose()) {
            addPoolGuardConnectionWrapperTransformer();
        }
    }

    private void addPoolGuardConnectionWrapperTransformer() {
        transformTemplate.transform("org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper", PoolGuardConnectionTransformCallback.class);
    }

    public static class PoolGuardConnectionTransformCallback implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // closeMethod
            InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
            closeMethod.addScopedInterceptor(DataSourceCloseConnectionInterceptor.class, CommonsDbcpConstants.SCOPE);

            return target.toBytecode();
        }
    }

    private void addBasicDataSourceTransformer() {
        transformTemplate.transform("org.apache.commons.dbcp.BasicDataSource", DatasourceTransformCallback.class);
    }

    public static class DatasourceTransformCallback implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (isAvailableDataSourceMonitor(target)) {
                target.addField(DataSourceMonitorAccessor.class);

                // default constructor
                InstrumentMethod defaultConstructor = InstrumentUtils.findConstructor(target);
                defaultConstructor.addScopedInterceptor(DataSourceConstructorInterceptor.class, CommonsDbcpConstants.SCOPE);

                // closeMethod
                InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
                closeMethod.addScopedInterceptor(DataSourceCloseInterceptor.class, CommonsDbcpConstants.SCOPE);
            }

            // getConnectionMethod
            InstrumentMethod getConnectionMethod1 = InstrumentUtils.findMethod(target, "getConnection");
            getConnectionMethod1.addScopedInterceptor(DataSourceGetConnectionInterceptor.class, CommonsDbcpConstants.SCOPE);

            InstrumentMethod getConnectionMethod2 = InstrumentUtils.findMethod(target, "getConnection", "java.lang.String", "java.lang.String");
            getConnectionMethod2.addScopedInterceptor(DataSourceGetConnectionInterceptor.class, CommonsDbcpConstants.SCOPE);

            return target.toBytecode();
        }

        private boolean isAvailableDataSourceMonitor(InstrumentClass target) {
            boolean hasMethod = target.hasMethod("getUrl");
            if (!hasMethod) {
                return false;
            }

            hasMethod = target.hasMethod("getNumActive");
            if (!hasMethod) {
                return false;
            }

            hasMethod = target.hasMethod("getMaxActive");
            return hasMethod;
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
