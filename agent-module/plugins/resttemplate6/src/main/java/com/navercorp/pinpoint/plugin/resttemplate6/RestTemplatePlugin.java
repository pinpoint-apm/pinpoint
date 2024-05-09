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

package com.navercorp.pinpoint.plugin.resttemplate6;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.VersionMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.resttemplate6.interceptor.AsyncHttpRequestInterceptor;
import com.navercorp.pinpoint.plugin.resttemplate6.interceptor.ClientHttpResponseInterceptor;
import com.navercorp.pinpoint.plugin.resttemplate6.interceptor.HttpRequestInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Taejin Koo
 */
public class RestTemplatePlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        RestTemplateConfig config = new RestTemplateConfig(context.getConfig());
        final MatcherOperand versionMatcherOperand = new VersionMatcherOperand("[6.0.0,6.max]", config.isVersionForcedMatch());
        if (!config.isPluginEnable()) {
            logger.info("Disabled {}, version {}, config:{}", this.getClass().getSimpleName(), versionMatcherOperand, config);
            return;
        }
        logger.info("{}, version {}, config:{}", this.getClass().getSimpleName(), versionMatcherOperand, config);

        transformRequest(versionMatcherOperand);
        transformResponse(versionMatcherOperand);
    }

    private void transformRequest(MatcherOperand versionMatcherOperand) {
        final Matcher restTemplateTransformerMatcher = Matchers.newClassBasedMatcher("org.springframework.web.client.RestTemplate", versionMatcherOperand);
        transformTemplate.transform(restTemplateTransformerMatcher, RestTemplateTransformer.class);
        final Matcher httpRequestTransformerMatcher = Matchers.newClassBasedMatcher("org.springframework.http.client.AbstractClientHttpRequest", versionMatcherOperand);
        transformTemplate.transform(httpRequestTransformerMatcher, HttpRequestTransformer.class);
        final Matcher asyncHttpRequestTransformerMatcher = Matchers.newClassBasedMatcher("org.springframework.http.client.AbstractAsyncClientHttpRequest", versionMatcherOperand);
        transformTemplate.transform(asyncHttpRequestTransformerMatcher, AsyncHttpRequestTransformer.class);
    }

    private void transformResponse(MatcherOperand versionMatcherOperand) {
        String[] list = new String[]{
                "org.springframework.http.client.BufferingClientHttpResponseWrapper",
                "org.springframework.http.client.SimpleClientHttpResponse",
                "org.springframework.http.client.HttpComponentsAsyncClientHttpResponse",
                "org.springframework.http.client.HttpComponentsClientHttpResponse",
                "org.springframework.http.client.OkHttp3ClientHttpResponse",
                "org.springframework.http.client.OkHttpClientHttpResponse",
                "org.springframework.http.client.Netty4ClientHttpResponse",
        };
        for (String i : list) {
            final Matcher clientHttpResponseTransformerMatcher = Matchers.newClassBasedMatcher(i, versionMatcherOperand);
            transformTemplate.transform(clientHttpResponseTransformerMatcher, ClientHttpResponseTransformer.class);
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class RestTemplateTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod constructor1 = target.getConstructor();
            if (constructor1 != null) {
                constructor1.addScopedInterceptor(BasicMethodInterceptor.class, va(RestTemplateConstants.SERVICE_TYPE), RestTemplateConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            InstrumentMethod constructor2 = target.getConstructor("java.util.List");
            if (constructor2 != null) {
                constructor2.addScopedInterceptor(BasicMethodInterceptor.class, va(RestTemplateConstants.SERVICE_TYPE), RestTemplateConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    public static class HttpRequestTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod executeMethod = target.getDeclaredMethod("execute");
            if (executeMethod != null) {
                executeMethod.addScopedInterceptor(HttpRequestInterceptor.class, RestTemplateConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    public static class AsyncHttpRequestTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod executeAsyncMethod = target.getDeclaredMethod("executeAsync");
            if (executeAsyncMethod != null) {
                executeAsyncMethod.addScopedInterceptor(AsyncHttpRequestInterceptor.class, RestTemplateConstants.SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    public static class ClientHttpResponseTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final List<InstrumentMethod> constructors = target.getDeclaredConstructors();
            if (constructors != null && constructors.size() == 1) { //only intercept one-constructor response, no overloading
                for (InstrumentMethod constructor : constructors) {
                    constructor.addScopedInterceptor(ClientHttpResponseInterceptor.class, "HttpResponse", ExecutionPolicy.BOUNDARY);
                }
            }

            return target.toBytecode();
        }

    }

}
