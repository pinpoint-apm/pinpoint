/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.druid;

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
import com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseConnectionInterceptor;
import com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceCloseInterceptor;
import com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceConstructorInterceptor;
import com.navercorp.pinpoint.plugin.druid.interceptor.DataSourceGetConnectionInterceptor;

import java.security.ProtectionDomain;

/**
 * The type Druid plugin.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/21
 */
public class DruidPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        final DruidConfig config = new DruidConfig(context.getConfig());
        if (!config.isPluginEnable()) {
            logger.info("{} disabled '{}'", this.getClass().getSimpleName(), "profiler.jdbc.druid=false");
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        addDruidDataSourceTransformer();

        if (config.isProfileClose()) {

            addDruidPooledConnectionTransformer();
        }
    }


    private void addDruidPooledConnectionTransformer() {

        transformTemplate.transform("com.alibaba.druid.pool.DruidPooledConnection", DruidPooledConnectionTransform.class);
    }

    public static class DruidPooledConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // closeMethod
            InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");

            closeMethod.addScopedInterceptor(DataSourceCloseConnectionInterceptor.class, DruidConstants.SCOPE);

            return target.toBytecode();
        }
    }

    private void addDruidDataSourceTransformer() {

        transformTemplate.transform("com.alibaba.druid.pool.DruidDataSource", DruidDataSourceTransform.class);
    }

    public static class DruidDataSourceTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (isAvailableDataSourceMonitor(target)) {
                target.addField(DataSourceMonitorAccessor.class);

                // closeMethod
                InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
                closeMethod.addScopedInterceptor(DataSourceCloseInterceptor.class, DruidConstants.SCOPE);

                // constructor
                InstrumentMethod defaultConstructor = InstrumentUtils.findConstructor(target);
                defaultConstructor.addScopedInterceptor(DataSourceConstructorInterceptor.class, DruidConstants.SCOPE);
            }

            // getConnectionMethod
            InstrumentMethod getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection");
            getConnectionMethod.addScopedInterceptor(DataSourceGetConnectionInterceptor.class, DruidConstants.SCOPE);
            getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection", new String[]{"java.lang.String", "java.lang.String"});
            getConnectionMethod.addScopedInterceptor(DataSourceGetConnectionInterceptor.class, DruidConstants.SCOPE);

            return target.toBytecode();
        }


        private boolean isAvailableDataSourceMonitor(InstrumentClass target) {

            return target.hasMethod("getUrl") && target.hasMethod("getMaxActive") && target.hasMethod("getActiveCount");
        }
    }
}