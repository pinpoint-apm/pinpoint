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
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
public class HikariCpPlugin implements ProfilerPlugin, TransformTemplateAware {

    public static final ServiceType HIKARICP_SERVICE_TYPE = ServiceTypeFactory.of(6060, "HIKARICP");
    public static final String HIKARICP_SCOPE = "HIKARICP_SCOPE";
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
    }

    private void addPoolGuardConnectionWrapperTransformer() {
        // 2.4.2 ~
        transformTemplate.transform("com.zaxxer.hikari.pool.ProxyConnection", new ConnectionTransformCallback());

        // 1.1.1 ~ 2.4.1
        transformTemplate.transform("com.zaxxer.hikari.proxy.ConnectionProxy", new ConnectionTransformCallback());
    }

    private void addBasicDataSourceTransformer() {
        transformTemplate.transform("com.zaxxer.hikari.HikariDataSource", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.hikaricp.interceptor.DataSourceGetConnectionInterceptor");
                return target.toBytecode();
            }

        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    static class ConnectionTransformCallback implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addInterceptor("com.navercorp.pinpoint.plugin.hikaricp.interceptor.DataSourceCloseInterceptor");
            return target.toBytecode();
        }

    }
}
