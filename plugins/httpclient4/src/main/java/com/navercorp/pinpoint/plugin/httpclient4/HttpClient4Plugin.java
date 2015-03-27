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
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorProperty;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpClient4Plugin implements ProfilerPlugin, HttpClient4Constants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClient4PluginConfig config = new HttpClient4PluginConfig(context.getConfig());

        if (config.isApacheHttpClient4Profile()) {
            // apache http client 4
            addHttpClient4ClassEditor(context, config);
            // apache http client 4 retry
            addDefaultHttpRequestRetryHandlerClassEditor(context, config);
        }

        // apache nio http client
        // addModifier(new InternalHttpAsyncClientModifier(context, config));
        addClosableHttpAsyncClientClassEditor(context, config);
        addClosableHttpClientClassEditor(context, config);
        addBasicFutureClassEditor(context, config);
    }

    private void addHttpClient4ClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org.apache.http.impl.client.AbstractHttpClient");

        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        context.addClassEditor(classEditorBuilder.build());
    }

    private void injectHttpRequestExecuteMethodInterceptor(final ClassEditorBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpRequestExecuteInterceptor");
    }

    private void injectHttpUriRequestExecuteInterceptor(final ClassEditorBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpUriRequestExecuteInterceptor");
    }

    private void addDefaultHttpRequestRetryHandlerClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org/apache/http/impl/client/DefaultHttpRequestRetryHandler");
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("retryRequest", "java.io.IOException", "int", "org.apache.http.protocol.HttpContext");
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.RetryRequestInterceptor");

        context.addClassEditor(classEditorBuilder.build());
    }

    private void addClosableHttpAsyncClientClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org/apache/http/impl/nio/client/CloseableHttpAsyncClient");
        addAsyncClientInterceptor(classEditorBuilder);
        addAsyncInternalClientInterceptor(classEditorBuilder);

        context.addClassEditor(classEditorBuilder.build());
    }

    private void addAsyncClientInterceptor(final ClassEditorBuilder classEditorBuilder) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.AsyncClientExecuteInterceptor");
    }

    private void addAsyncInternalClientInterceptor(final ClassEditorBuilder classEditorBuilder) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder
                .editMethod("execute", "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer", "org.apache.http.concurrent.FutureCallback");
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.AsyncInternalClientExecuteInterceptor");
    }

    private void addClosableHttpClientClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org/apache/http/impl/client/CloseableHttpClient");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
        injectHttpRequestExecuteMethodInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
        injectHttpUriRequestExecuteInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

        context.addClassEditor(classEditorBuilder.build());
    }

    private void addBasicFutureClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org/apache/http/concurrent/BasicFuture");
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("get");
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureGetInterceptor");

        MethodEditorBuilder getMethodEditorBuilder = classEditorBuilder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        getMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureGetInterceptor");

        MethodEditorBuilder completedMethodEditorBuilder = classEditorBuilder.editMethod("completed", "java.lang.Object");
        getMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureCompletedInterceptor");

        MethodEditorBuilder failMethodEditorBuilder = classEditorBuilder.editMethod("failed", "java.lang.Exception");
        getMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureFailedInterceptor");

        context.addClassEditor(classEditorBuilder.build());
    }
}