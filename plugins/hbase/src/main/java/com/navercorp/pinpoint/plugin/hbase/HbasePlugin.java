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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseAdminMethodInterceptor;
import com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseClientMainInterceptor;
import com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseClientMethodInterceptor;
import com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseClientRunInterceptor;
import com.navercorp.pinpoint.plugin.hbase.interceptor.HbaseTableMethodInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

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
        if (!config.isClientProfile()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        if (config.isAdminProfile()) {
            addHbaseAdminTransformer();
        }
        if (config.isTableProfile()) {
            addHbaseTableTransformer();
        }
        addHbaseClientTransformer();
    }

    private void addHbaseClientTransformer() {

        transformTemplate.transform("org.apache.hadoop.hbase.client.AsyncProcess", AsyncProcessTransform.class);
        transformTemplate.transform("org.apache.hadoop.hbase.client.AsyncProcess$AsyncRequestFutureImpl$SingleServerRequestRunnable", SingleServerRequestRunnableTransform.class);
        transformTemplate.transform("org.apache.hadoop.hbase.ipc.RpcClientImpl", RpcClientImplTransform.class);
        transformTemplate.transform("org.apache.hadoop.hbase.ipc.AsyncRpcClient", RpcClientImplTransform.class);
    }

    public static class AsyncProcessTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("submit", "submitAll"))) {
                method.addScopedInterceptor(HbaseClientMainInterceptor.class, HbasePluginConstants.HBASE_CLIENT_SCOPE);
            }
            return target.toBytecode();
        }
    }

    public static class SingleServerRequestRunnableTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(AsyncContextAccessor.class);

            InstrumentMethod constructor = target.getConstructor("org.apache.hadoop.hbase.client.AsyncProcess$AsyncRequestFutureImpl", "org.apache.hadoop.hbase.client.MultiAction", "int", "org.apache.hadoop.hbase.ServerName", "java.util.Set");

            constructor.addScopedInterceptor(HbaseClientConstructorInterceptor.class, HbasePluginConstants.HBASE_CLIENT_SCOPE, ExecutionPolicy.INTERNAL);

            InstrumentMethod method = target.getDeclaredMethod("run");

            method.addInterceptor(HbaseClientRunInterceptor.class);

            return target.toBytecode();
        }
    }

    public static class RpcClientImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("call"))) {

                method.addInterceptor(HbaseClientMethodInterceptor.class);
            }
            return target.toBytecode();
        }
    };

    private void addHbaseAdminTransformer() {

        transformTemplate.transform("org.apache.hadoop.hbase.client.HBaseAdmin", HBaseAdminTransform.class);
    }

    public static class HBaseAdminTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            HbasePluginConfig config = new HbasePluginConfig(instrumentor.getProfilerConfig());
            final boolean paramsProfile = config.isParamsProfile();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(HbasePluginConstants.adminMethodNames))) {
                method.addInterceptor(HbaseAdminMethodInterceptor.class, va(paramsProfile));
            }
            return target.toBytecode();
        }
    }

    private void addHbaseTableTransformer() {
        transformTemplate.transform("org.apache.hadoop.hbase.client.HTable", HTableTransform.class);
    }

    public static class HTableTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            HbasePluginConfig config = new HbasePluginConfig(instrumentor.getProfilerConfig());
            final boolean paramsProfile = config.isParamsProfile();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(HbasePluginConstants.tableMethodNames))) {
                method.addInterceptor(HbaseTableMethodInterceptor.class, va(paramsProfile));
            }
            return target.toBytecode();
        }
    }
}
