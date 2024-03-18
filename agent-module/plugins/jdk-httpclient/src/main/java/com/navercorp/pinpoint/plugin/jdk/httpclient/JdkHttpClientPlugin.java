/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdk.httpclient;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.jdk.httpclient.interceptor.HttpClientInterceptor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.interceptor.HttpResponseImplInterceptor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.interceptor.MultiExchangeResponseAsyncImplInterceptor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.interceptor.MultiExchangeResponseAsyncInterceptor;

import java.security.ProtectionDomain;

public class JdkHttpClientPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JdkHttpClientPluginConfig config = new JdkHttpClientPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disable JdkHttpClientPlugin");
            return;
        }

        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // Java 11 or later
        transformTemplate.transform("jdk.internal.net.http.HttpClientImpl", HttpClientImplTransform.class);
        transformTemplate.transform("jdk.internal.net.http.MultiExchange", MultiExchangeTransform.class);
        transformTemplate.transform("jdk.internal.net.http.HttpRequestImpl", HttpRequestImplTransform.class);
        transformTemplate.transform("jdk.internal.net.http.HttpResponseImpl", HttpResponseImplTransform.class);
    }

    public static class HttpClientImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("send", "sendAsync"))) {
                method.addScopedInterceptor(HttpClientInterceptor.class, JdkHttpClientConstants.HTTP_CLIENT_SEND_SCOPE);
            }

            return target.toBytecode();
        }
    }

    public static class MultiExchangeTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addGetter(HttpRequestImplGetter.class, "currentreq");

            final InstrumentMethod responseAsyncMethod = target.getDeclaredMethod("responseAsync", "java.util.concurrent.Executor");
            if (responseAsyncMethod != null) {
                responseAsyncMethod.addInterceptor(MultiExchangeResponseAsyncInterceptor.class);
            }
            final InstrumentMethod responseAsyncImplMethod = target.getDeclaredMethod("responseAsyncImpl");
            if (responseAsyncImplMethod != null) {
                responseAsyncImplMethod.addInterceptor(MultiExchangeResponseAsyncImplInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpRequestImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }

    public static class HttpResponseImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addGetter(ResponseCodeGetter.class, "responseCode");

            final InstrumentMethod constructor = target.getConstructor("java.net.http.HttpRequest", "jdk.internal.net.http.Response", "java.net.http.HttpResponse", "java.lang.Object", "jdk.internal.net.http.Exchange");
            if (constructor != null) {
                constructor.addInterceptor(HttpResponseImplInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}