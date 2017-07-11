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
package com.navercorp.pinpoint.plugin.okhttp.v2;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * okhttp 2.x
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
            logger.info("Disable OkHttpPlugin 2.x");
            return;
        }

        logger.info("Setup OkHttpPlugin 2.x");
        addCall();
        addDispatcher();
        if (config.isAsync()) {
            addAsyncCall();
        }
        addHttpEngine(config);
        addRequestBuilder();
    }

    private void addCall() {
        transformTemplate.transform("com.squareup.okhttp.Call", new TransformCallback() {

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
        transformTemplate.transform("com.squareup.okhttp.Dispatcher", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "cancel"))) {
                    method.addInterceptor(BasicMethodInterceptor.class.getName(), va(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL));
                }

                final InstrumentMethod enqueueMethod = target.getDeclaredMethod("enqueue", "com.squareup.okhttp.Call$AsyncCall");
                if (enqueueMethod != null) {
                    enqueueMethod.addInterceptor("com.navercorp.pinpoint.plugin.okhttp.interceptor.DispatcherEnqueueMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addAsyncCall() {
        transformTemplate.transform("com.squareup.okhttp.Call$AsyncCall", new TransformCallback() {

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

    private void addHttpEngine(final OkHttpPluginConfig config) {
        transformTemplate.transform("com.squareup.okhttp.internal.http.HttpEngine", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addGetter(OkHttpConstants.USER_REQUEST_GETTER, OkHttpConstants.FIELD_USER_REQUEST);
                target.addGetter(OkHttpConstants.USER_RESPONSE_GETTER, OkHttpConstants.FIELD_USER_RESPONSE);
                target.addGetter(OkHttpConstants.CONNECTION_GETTER, OkHttpConstants.FIELD_CONNECTION);

                final InstrumentMethod sendRequestMethod = target.getDeclaredMethod("sendRequest");
                if (sendRequestMethod != null) {
                    sendRequestMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineSendRequestMethodInterceptor", OkHttpConstants.SEND_REQUEST_SCOPE);
                }

                final InstrumentMethod connectMethod = target.getDeclaredMethod("connect");
                if (connectMethod != null) {
                    connectMethod.addInterceptor("com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineConnectMethodInterceptor");
                }

                final InstrumentMethod readResponseMethod = target.getDeclaredMethod("readResponse");
                if (readResponseMethod != null) {
                    readResponseMethod.addInterceptor("com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineReadResponseMethodInterceptor", va(config.isStatusCode()));
                }

                return target.toBytecode();
            }
        });
    }

    private void addRequestBuilder() {
        transformTemplate.transform("com.squareup.okhttp.Request$Builder", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod buildMethod = target.getDeclaredMethod("build");
                if (buildMethod != null) {
                    if (instrumentor.exist(loader, "com.squareup.okhttp.HttpUrl")) {
                        // over 2.4.0
                        target.addGetter(OkHttpConstants.HTTP_URL_GETTER, OkHttpConstants.FIELD_HTTP_URL);
                        buildMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.RequestBuilderBuildMethodInterceptor", OkHttpConstants.SEND_REQUEST_SCOPE, ExecutionPolicy.INTERNAL);
                    } else {
                        // 2.0 ~ 2.3
                        target.addGetter(OkHttpConstants.URL_GETTER, OkHttpConstants.FIELD_HTTP_URL);
                        buildMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.RequestBuilderBuildMethodBackwardCompatibilityInterceptor", OkHttpConstants.SEND_REQUEST_SCOPE, ExecutionPolicy.INTERNAL);
                    }
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