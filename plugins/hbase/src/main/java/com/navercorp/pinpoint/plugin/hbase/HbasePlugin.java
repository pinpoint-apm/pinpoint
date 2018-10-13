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
package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginMethodNameFilter.MethodNameType;

import java.security.ProtectionDomain;

/**
 * The type Hbase plugin.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbasePlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        HbasePluginConfig config = new HbasePluginConfig(context.getConfig());

        if (!config.isHbaseProfile()) {
            logger.info("Disable HbasePlugin. config={}", config);
            return;
        }
        addHbaseClientTransformer();

        if (config.isOperationProfile()) {
            addHbaseAdminTransformer();
            addHbaseTableTransformer();
        }
    }

    private void addHbaseClientTransformer() {

        TransformCallback transformCallback = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("call"))) {

                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseClientMethodInterceptor", HbasePluginConstants.HBASE_SCOPE);
                }
                return target.toBytecode();
            }
        };

        transformTemplate.transform("org.apache.hadoop.hbase.ipc.RpcClientImpl", transformCallback);
        transformTemplate.transform("org.apache.hadoop.hbase.ipc.AsyncRpcClient", transformCallback);
    }

    private void addHbaseAdminTransformer() {

        final HbasePluginMethodNameFilter methodNameFilter = new HbasePluginMethodNameFilter(MethodNameType.ADMIN);

        transformTemplate.transform("org.apache.hadoop.hbase.client.HBaseAdmin", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(methodNameFilter)) {

                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseAdminMethodInterceptor", HbasePluginConstants.HBASE_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addHbaseTableTransformer() {

        final HbasePluginMethodNameFilter methodNameFilter = new HbasePluginMethodNameFilter(MethodNameType.TABLE);

        transformTemplate.transform("org.apache.hadoop.hbase.client.HTable", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {

                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseTableMethodInterceptor", HbasePluginConstants.HBASE_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }
}
