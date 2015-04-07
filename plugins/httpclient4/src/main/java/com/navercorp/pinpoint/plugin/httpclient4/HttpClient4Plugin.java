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
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
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

        // if (config.isApacheHttpClient4Profile()) {
        // addHttpClient4ClassEditor(context, config);
        // addDefaultHttpRequestRetryHandlerClassEditor(context, config);
        // }

        addClosableHttpAsyncClientClassEditor(context, config);
        addDefaultClientExchangeHandlerImplConstructorInterceptor(context, config);
        // addClosableHttpClientClassEditor(context, config);
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
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org.apache.http.impl.client.DefaultHttpRequestRetryHandler");
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("retryRequest", "java.io.IOException", "int", "org.apache.http.protocol.HttpContext");
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.RetryRequestInterceptor");

        context.addClassEditor(classEditorBuilder.build());
    }

    private void addClosableHttpAsyncClientClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org.apache.http.impl.nio.client.CloseableHttpAsyncClient");
        // with HttpRequest
        injectCloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");
        injectCloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor(classEditorBuilder, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.concurrent.FutureCallback");
        // with HttpAsyncRequestProducer
        injectCloseableHttpAsyncClientExecuteMethodWithHttpAsyncRequestProducerInterceptor(classEditorBuilder, "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer", "org.apache.http.concurrent.FutureCallback");
        // with HttpUriRequest
        injectCloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.concurrent.FutureCallback");
        injectCloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(classEditorBuilder, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");
        
        context.addClassEditor(classEditorBuilder.build());
    }

    private void injectCloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor(final ClassEditorBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.CloseableHttpAsyncClientExecuteMethodWithHttpRequestInterceptor");
    }

    private void injectCloseableHttpAsyncClientExecuteMethodWithHttpAsyncRequestProducerInterceptor(final ClassEditorBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.CloseableHttpAsyncClientExecuteMethodWithHttpAsyncRequestProducerInterceptor");
    }
    
    private void injectCloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(final ClassEditorBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("execute", parameterTypeNames);
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.CloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor");
    }
    

    private void addDefaultClientExchangeHandlerImplConstructorInterceptor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl");
        ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor("org.apache.commons.logging.Log", "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer",
                "org.apache.http.client.protocol.HttpClientContext", "org.apache.http.concurrent.BasicFuture", "org.apache.http.nio.conn.NHttpClientConnectionManager", "org.apache.http.impl.nio.client.InternalClientExec");
        constructorEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.DefaultClientExchangeHandlerImplConstructorInterceptor");

        context.addClassEditor(classEditorBuilder.build());
    }

    private void addClosableHttpClientClassEditor(ProfilerPluginSetupContext context, HttpClient4PluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org.apache.http.impl.client.CloseableHttpClient");
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
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder("org.apache.http.concurrent.BasicFuture");
        classEditorBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("get");
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodEditorBuilder getMethodEditorBuilder = classEditorBuilder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        getMethodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        getMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodEditorBuilder completedMethodEditorBuilder = classEditorBuilder.editMethod("completed", "java.lang.Object");
        completedMethodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        completedMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodEditorBuilder failMethodEditorBuilder = classEditorBuilder.editMethod("failed", "java.lang.Exception");
        failMethodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        failMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");

        MethodEditorBuilder cancelMethodEditorBuilder = classEditorBuilder.editMethod("cancel", "boolean");
        cancelMethodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        cancelMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor");
        
        context.addClassEditor(classEditorBuilder.build());
    }
}