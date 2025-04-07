/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.AsyncHandlerOnCompletedInterceptor;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.AsyncHandlerOnStatusReceivedInterceptor;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.AsyncHandlerOnThrowableInterceptor;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.ExecuteInterceptor;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.ExecuteRequestInterceptor;

import java.security.ProtectionDomain;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 */
public class NingAsyncHttpClientPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NingAsyncHttpClientPluginConfig config = new NingAsyncHttpClientPluginConfig(context.getConfig());

        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // 1.8.x ~ 1.9.x
        logger.debug("Add AsyncHttpClient(1.8.x ~ 1.9.x)");
        addAsyncHttpClientTransformer();

        // 2.x
        logger.debug("Add DefaultAsyncHttpClient(2.x ~");
        addDefaultAsyncHttpClientTransformer();
        addAsyncHanlderTransformer();
    }

    private void addAsyncHttpClientTransformer() {
        transformTemplate.transform("com.ning.http.client.AsyncHttpClient", AsyncHttpClientTransform.class);
    }

    public static class AsyncHttpClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod executeRequestMethod = target.getDeclaredMethod("executeRequest", "com.ning.http.client.Request", "com.ning.http.client.AsyncHandler");
            if (executeRequestMethod != null) {
                executeRequestMethod.addInterceptor(ExecuteRequestInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addDefaultAsyncHttpClientTransformer() {
        transformTemplate.transform("org.asynchttpclient.DefaultAsyncHttpClient", DefaultAsyncHttpClientTransform.class);
    }

    public static class DefaultAsyncHttpClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod executeRequestMethod = target.getDeclaredMethod("execute", "org.asynchttpclient.Request", "org.asynchttpclient.AsyncHandler");
            if (executeRequestMethod != null) {
                executeRequestMethod.addInterceptor(ExecuteInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addAsyncHanlderTransformer() {
        final Matcher matcher = Matchers.newPackageBasedMatcher("org.asynchttpclient", new InterfaceInternalNameMatcherOperand("org.asynchttpclient.AsyncHandler", true));
        transformTemplate.transform(matcher, AsyncHandlerTransform.class);
    }

    public static class AsyncHandlerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod onStatusReceivedMethod = target.getDeclaredMethod("onStatusReceived", "org.asynchttpclient.HttpResponseStatus");
            if (onStatusReceivedMethod != null) {
                onStatusReceivedMethod.addInterceptor(AsyncHandlerOnStatusReceivedInterceptor.class);
            }
            final InstrumentMethod onThrowableMethod = target.getDeclaredMethod("onThrowable", "java.lang.Throwable");
            if (onThrowableMethod != null) {
                onThrowableMethod.addInterceptor(AsyncHandlerOnThrowableInterceptor.class);
            }
            final InstrumentMethod onCompletedMethod = target.getDeclaredMethod("onCompleted");
            if (onCompletedMethod != null) {
                onCompletedMethod.addInterceptor(AsyncHandlerOnCompletedInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}