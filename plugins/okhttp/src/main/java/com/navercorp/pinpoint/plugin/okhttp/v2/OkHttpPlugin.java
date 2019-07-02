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
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;
import com.navercorp.pinpoint.plugin.okhttp.interceptor.AsyncCallExecuteMethodInterceptor;
import com.navercorp.pinpoint.plugin.okhttp.interceptor.DispatcherEnqueueMethodInterceptor;

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
            logger.info("{} 2.x disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

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
        transformTemplate.transform("com.squareup.okhttp.Call", CallTransform.class);
    }

    public static class CallTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "enqueue", "cancel"))) {
                method.addScopedInterceptor(BasicMethodInterceptor.class, va(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL), OkHttpConstants.CALL_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addDispatcher() {
        transformTemplate.transform("com.squareup.okhttp.Dispatcher", DispatcherTransform.class);
    }

    public static class DispatcherTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "cancel"))) {
                method.addInterceptor(BasicMethodInterceptor.class, va(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL));
            }

            final InstrumentMethod enqueueMethod = target.getDeclaredMethod("enqueue", "com.squareup.okhttp.Call$AsyncCall");
            if (enqueueMethod != null) {
                enqueueMethod.addInterceptor(DispatcherEnqueueMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addAsyncCall() {
        transformTemplate.transform("com.squareup.okhttp.Call$AsyncCall", AsyncCallTransform.class);
    }

    public static class AsyncCallTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                target.addField(AsyncContextAccessor.class);
                executeMethod.addInterceptor(AsyncCallExecuteMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addHttpEngine(final OkHttpPluginConfig config) {
        transformTemplate.transform("com.squareup.okhttp.internal.http.HttpEngine", HttpEngineTransform.class);
    }

    public static class HttpEngineTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addGetter(OkHttpConstants.USER_REQUEST_GETTER_V2, OkHttpConstants.FIELD_USER_REQUEST);
            target.addGetter(OkHttpConstants.USER_RESPONSE_GETTER_V2, OkHttpConstants.FIELD_USER_RESPONSE);

            final InstrumentMethod sendRequestMethod = target.getDeclaredMethod("sendRequest");
            if (sendRequestMethod != null) {
                sendRequestMethod.addScopedInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineSendRequestMethodInterceptor.class, OkHttpConstants.SEND_REQUEST_SCOPE);
            }

            // pre-2.7.0
            if (target.hasField("connection", "com.squareup.okhttp.Connection")) {
                target.addGetter(OkHttpConstants.CONNECTION_GETTER_V2, OkHttpConstants.FIELD_CONNECTION);
                // 2.3.0+
                final InstrumentMethod connectMethod = target.getDeclaredMethod("connect");
                if (connectMethod != null) {
                    connectMethod.addInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineConnectMethodInterceptor.class);
                }
                // pre-2.3.0
                final InstrumentMethod connectMethodWithParam = target.getDeclaredMethod("connect", "com.squareup.okhttp.Request");
                if (connectMethodWithParam != null) {
                    connectMethodWithParam.addInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineConnectMethodInterceptor.class);
                }
            } else {
                // 2.7.0+
                final InstrumentMethod connectMethod = target.getDeclaredMethod("connect");
                if (connectMethod != null) {
                    connectMethod.addInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineConnectMethodFromUserRequestInterceptor.class);
                }
            }

            final OkHttpPluginConfig config = new OkHttpPluginConfig(instrumentor.getProfilerConfig());
            final InstrumentMethod readResponseMethod = target.getDeclaredMethod("readResponse");
            if (readResponseMethod != null) {
                readResponseMethod.addInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.HttpEngineReadResponseMethodInterceptor.class, va(config.isStatusCode()));
            }

            return target.toBytecode();
        }
    }

    private void addRequestBuilder() {
        transformTemplate.transform("com.squareup.okhttp.Request$Builder", RequestBuilderTransform.class);
    }

    public static class RequestBuilderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod buildMethod = target.getDeclaredMethod("build");
            if (buildMethod != null) {
                if (instrumentor.exist(loader, "com.squareup.okhttp.HttpUrl", protectionDomain)) {
                    // over 2.4.0
                    target.addGetter(OkHttpConstants.HTTP_URL_GETTER, OkHttpConstants.FIELD_HTTP_URL);
                    buildMethod.addScopedInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.RequestBuilderBuildMethodInterceptor.class, OkHttpConstants.SEND_REQUEST_SCOPE, ExecutionPolicy.INTERNAL);
                } else {
                    // 2.0 ~ 2.3
                    target.addGetter(OkHttpConstants.URL_GETTER, OkHttpConstants.FIELD_HTTP_URL);
                    buildMethod.addScopedInterceptor(com.navercorp.pinpoint.plugin.okhttp.v2.interceptor.RequestBuilderBuildMethodBackwardCompatibilityInterceptor.class, OkHttpConstants.SEND_REQUEST_SCOPE, ExecutionPolicy.INTERNAL);
                }
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}