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

    private DruidConfig config;

    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        config = new DruidConfig(context.getConfig());

        if (!config.isPluginEnable()) {

            logger.info("Disable druid option. 'profiler.jdbc.druid=false'");
            return;
        }

        addDruidDataSourceTransformer();

        if (config.isProfileClose()) {

            addDruidPooledConnectionTransformer();
        }
    }

    private boolean isAvailableDataSourceMonitor(InstrumentClass target) {

        return target.hasMethod("getUrl") && target.hasMethod("getMaxActive") && target.hasMethod("getActiveCount");
    }

    private void addDruidPooledConnectionTransformer() {

        transformTemplate.transform("com.alibaba.druid.pool.DruidPooledConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                // closeMethod
                InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");

                closeMethod.addScopedInterceptor(DruidConstants.INTERCEPTOR_CLOSE_CONNECTION, DruidConstants.SCOPE);

                return target.toBytecode();
            }
        });
    }

    private void addDruidDataSourceTransformer() {

        transformTemplate.transform("com.alibaba.druid.pool.DruidDataSource", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (isAvailableDataSourceMonitor(target)) {
                    target.addField(DruidConstants.ACCESSOR_DATASOURCE_MONITOR);

                    // closeMethod
                    InstrumentMethod closeMethod = InstrumentUtils.findMethod(target, "close");
                    closeMethod.addScopedInterceptor(DruidConstants.INTERCEPTOR_CLOSE, DruidConstants.SCOPE);

                    // constructor
                    InstrumentMethod defaultConstructor = InstrumentUtils.findConstructor(target);
                    defaultConstructor.addScopedInterceptor(DruidConstants.INTERCEPTOR_CONSTRUCTOR, DruidConstants.SCOPE);
                }

                // getConnectionMethod
                InstrumentMethod getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection");
                getConnectionMethod.addScopedInterceptor(DruidConstants.INTERCEPTOR_GET_CONNECTION, DruidConstants.SCOPE);
                getConnectionMethod = InstrumentUtils.findMethod(target, "getConnection", new String[]{"java.lang.String", "java.lang.String"});
                getConnectionMethod.addScopedInterceptor(DruidConstants.INTERCEPTOR_GET_CONNECTION, DruidConstants.SCOPE);

                return target.toBytecode();
            }
        });
    }
}