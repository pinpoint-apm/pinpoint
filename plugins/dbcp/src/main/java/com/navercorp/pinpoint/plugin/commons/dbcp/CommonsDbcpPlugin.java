/**
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

import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Jongho Moon
 */
public class CommonsDbcpPlugin implements ProfilerPlugin {
    public static final ServiceType DBCP_SERVICE_TYPE = ServiceType.of(6050, "DBCP", NORMAL_SCHEMA);
    public static final String DBCP_GROUP = "DBCP_GROUP";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addBasicDataSourceTransformer(context);
        
        boolean profileClose = context.getConfig().readBoolean("profiler.jdbc.dbcp.connectionclose", false);
        
        if (profileClose) {
            addPoolGuardConnectionWrapperTransformer(context);
        }
    }

    private void addPoolGuardConnectionWrapperTransformer(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext pluginContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = pluginContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.commons.dbcp.interceptor.DataSourceCloseInterceptor");
                return target.toBytecode();
            }
        });
    }

    private void addBasicDataSourceTransformer(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("org.apache.commons.dbcp.BasicDataSource", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext pluginContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = pluginContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addInterceptor("com.navercorp.pinpoint.plugin.commons.dbcp.interceptor.DataSourceGetConnectionInterceptor");
                return target.toBytecode();
            }
        });
    }
}
