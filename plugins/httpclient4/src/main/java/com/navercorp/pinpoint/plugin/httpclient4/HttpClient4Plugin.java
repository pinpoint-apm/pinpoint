/*
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
package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.BaseClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 *
 */
public class HttpClient4Plugin implements ProfilerPlugin, HttpClient4Constants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClient4PluginConfig config = new HttpClient4PluginConfig(context.getConfig());

        if (config.isApacheHttpClient4Profile()) {
            logger.debug("Add HttpClient4(4.0 ~ 4.2");
            // Apache httpclient4 (version 4.0 ~ 4.2)
            addHttpClient4Class(context, config);
            addDefaultHttpRequestRetryHandlerClass(context, config);
        }

        // Apache httpclient4 (version 4.3 ~ 4.4)
        logger.debug("Add CloseableHttpClient4(4.3 ~ ");
        addCloseableHttpClientClass(context, config);

        // Apache httpAsyncClient4 (version 4.0)
        // unsupported AbstractHttpAsyncClient because of deprecated.
        logger.debug("Add CloseableHttpAsyncClient4(4.0 ~ ");
        addClosableHttpAsyncClientClass(context, config);
        addDefaultClientExchangeHandlerImplClass(context, config);
        addBasicFutureClass(context, config);
    }

    private void addHttpClient4Class(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.client.AbstractHttpClient");
        // The execute method was moved to CloseableHttpClient class from httpclient 4.0 or later.
        // Need to check if CloseableHttpClient class exist.
        classEditorBuilder.conditional(ClassConditions.hasClass("org.apache.http.impl.client.AbstractHttpClient"), new ConditionalClassFileTransformerSetup() {
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                injectHttpClientExecuteMethodWithHttpRequestInterceptor(conditional, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
                injectHttpClientExecuteMethodWithHttpRequestInterceptor(conditional, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
                injectHttpClientExecuteMethodWithHttpRequestInterceptor(conditional, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
                injectHttpClientExecuteMethodWithHttpRequestInterceptor(conditional, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

                injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(conditional, false, "org.apache.http.client.methods.HttpUriRequest");
                injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(conditional, false, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
                injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(conditional, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
                injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(conditional, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");
            }
        });

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addCloseableHttpClientClass(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.client.CloseableHttpClient");
        injectHttpClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
        injectHttpClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
        injectHttpClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
        injectHttpClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, false, "org.apache.http.client.methods.HttpUriRequest");
        injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, false, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
        injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
        injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void injectHttpClientExecuteMethodWithHttpRequestInterceptor(final BaseClassFileTransformerBuilder classEditorBuilder, boolean isHasCallbackParam, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpClientExecuteMethodWithHttpRequestInterceptor", isHasCallbackParam);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpClientExecuteMethodInternalInterceptor", isHasCallbackParam);
    }

    private void injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(final BaseClassFileTransformerBuilder classEditorBuilder, boolean isHasCallbackParam, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpClientExecuteMethodWithHttpUriRequestInterceptor", isHasCallbackParam);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpClientExecuteMethodInternalInterceptor", isHasCallbackParam);
    }

    private void addDefaultHttpRequestRetryHandlerClass(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.client.DefaultHttpRequestRetryHandler");
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("retryRequest", "java.io.IOException", "int", "org.apache.http.protocol.HttpContext");
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.DefaultHttpRequestRetryHandlerRetryRequestMethodInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addClosableHttpAsyncClientClass(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.nio.client.CloseableHttpAsyncClient");
        // with HttpRequest
        injectHttpAsyncClientExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");
        injectHttpAsyncClientExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.concurrent.FutureCallback");
        injectHttpAsyncClientExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer",
                "org.apache.http.concurrent.FutureCallback");
        injectHttpAsyncClientExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.concurrent.FutureCallback");
        injectHttpAsyncClientExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void injectHttpAsyncClientExecuteMethodInterceptor(final ClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpAsyncClientExecuteMethodInterceptor");
    }

    private void addDefaultClientExchangeHandlerImplClass(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl");
        classEditorBuilder.injectFieldAccessor(FIELD_REQUEST_PRODUCER);
        classEditorBuilder.injectFieldAccessor(FIELD_RESULT_FUTURE);

        final MethodTransformerBuilder startMethod = classEditorBuilder.editMethod("start");
        startMethod.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        startMethod.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.DefaultClientExchangeHandlerImplStartMethodInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addBasicFutureClass(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.http.concurrent.BasicFuture");
        classEditorBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);

        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("get");
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder getMethodEditorBuilder = classEditorBuilder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        getMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        getMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder completedMethodEditorBuilder = classEditorBuilder.editMethod("completed", "java.lang.Object");
        completedMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        completedMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder failMethodEditorBuilder = classEditorBuilder.editMethod("failed", "java.lang.Exception");
        failMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        failMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodTransformerBuilder cancelMethodEditorBuilder = classEditorBuilder.editMethod("cancel", "boolean");
        cancelMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        cancelMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }
}