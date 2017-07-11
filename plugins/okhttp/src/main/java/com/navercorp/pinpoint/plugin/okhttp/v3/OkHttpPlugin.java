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

package com.navercorp.pinpoint.plugin.okhttp.v3;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * okhttp 3.x
 *
 * @author jaehong.kim
 */
public class OkHttpPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final OkHttpPluginConfig config = new OkHttpPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disable OkHttpPlugin 3.x");
            return;
        }

        logger.info("Setup OkHttpPlugin 3.x");
        addRealCall();
        addDispatcher();
        if (config.isAsync()) {
            addAsyncCall();
        }
        addBridegInterceptor();
        addRequestBuilder();
        addRealConnection();
    }

    private void addRealCall() {
        transformTemplate.transform("okhttp3.RealCall", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "enqueue", "cancel"))) {
                    method.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL), OkHttpConstants.CALL_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addDispatcher() {
        transformTemplate.transform("okhttp3.Dispatcher", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "cancel"))) {
                    method.addInterceptor(BasicMethodInterceptor.class.getName(), va(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL));
                }

                final InstrumentMethod enqueueMethod = target.getDeclaredMethod("enqueue", "okhttp3.RealCall$AsyncCall");
                if (enqueueMethod != null) {
                    enqueueMethod.addInterceptor("com.navercorp.pinpoint.plugin.okhttp.interceptor.DispatcherEnqueueMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addAsyncCall() {
        transformTemplate.transform("okhttp3.RealCall$AsyncCall", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
                if (executeMethod != null) {
                    target.addField(AsyncContextAccessor.class.getName());
                    executeMethod.addInterceptor("com.navercorp.pinpoint.plugin.okhttp.interceptor.AsyncCallExecuteMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addBridegInterceptor() {
        transformTemplate.transform("okhttp3.internal.http.BridgeInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod proceedMethod = target.getDeclaredMethod("intercept", "okhttp3.Interceptor$Chain");
                if (proceedMethod != null) {
                    proceedMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.okhttp.v3.interceptor.BridgeInterceptorInterceptMethodInterceptor", OkHttpConstants.SEND_REQUEST_SCOPE, ExecutionPolicy.ALWAYS);
                }

                return target.toBytecode();
            }
        });
    }

    private void addRequestBuilder() {
        transformTemplate.transform("okhttp3.Request$Builder", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod buildMethod = target.getDeclaredMethod("build");
                if (buildMethod != null) {
                    target.addGetter("com.navercorp.pinpoint.plugin.okhttp.v3.HttpUrlGetter", OkHttpConstants.FIELD_HTTP_URL);
                    buildMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.okhttp.v3.interceptor.RequestBuilderBuildMethodInterceptor", OkHttpConstants.SEND_REQUEST_SCOPE, ExecutionPolicy.INTERNAL);
                }

                return target.toBytecode();
            }
        });
    }

    private void addRealConnection() {
        transformTemplate.transform("okhttp3.internal.connection.RealConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod connectMethod = target.getDeclaredMethod("connect", "int", "int", "int", "boolean");
                if (connectMethod != null) {
                    target.addGetter("com.navercorp.pinpoint.plugin.okhttp.v3.RouteGetter", "route");
                    connectMethod.addInterceptor("com.navercorp.pinpoint.plugin.okhttp.v3.interceptor.RealConnectionConnectMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}