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
package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.BaseClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 *
 */
public class HttpClient3Plugin implements ProfilerPlugin, HttpClient3Constants {

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClient3PluginConfig config = new HttpClient3PluginConfig(context.getConfig());

        if (config.isApacheHttpClient3Profile()) {
            // apache http client 3
            addHttpClient3Class(context, config);

            // apache http client 3 retry
            addDefaultHttpMethodRetryHandlerClass(context, config);
            // 3.1.0
            addHttpConnectionClass(context, config);
            addHttpMethodBaseClass(context, config);
        }
    }

    private void addHttpClient3Class(ProfilerPluginSetupContext context, HttpClient3PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.commons.httpclient.HttpClient");

        injectHttpClientExecuteMethod(classEditorBuilder, "org.apache.commons.httpclient.HttpMethod");
        injectHttpClientExecuteMethod(classEditorBuilder, "org.apache.commons.httpclient.HostConfiguration", "org.apache.commons.httpclient.HttpMethod");
        injectHttpClientExecuteMethod(classEditorBuilder, "org.apache.commons.httpclient.HostConfiguration", "org.apache.commons.httpclient.HttpMethod", "org.apache.commons.httpclient.HttpState");

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void injectHttpClientExecuteMethod(final BaseClassFileTransformerBuilder classEditorBuilder, String... parameterTypeNames) {
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("executeMethod", parameterTypeNames);
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient3.interceptor.ExecuteInterceptor");
    }

    
    private void addDefaultHttpMethodRetryHandlerClass(ProfilerPluginSetupContext context, HttpClient3PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.commons.httpclient.DefaultHttpMethodRetryHandler");
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("retryMethod", "org.apache.commons.httpclient.HttpMethod", "java.io.IOException", "int");
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient3.interceptor.RetryMethodInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }
    
    private void addHttpConnectionClass(ProfilerPluginSetupContext context, HttpClient3PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.commons.httpclient.HttpConnection");
        classEditorBuilder.injectFieldAccessor(FIELD_HOST_NAME);
        classEditorBuilder.injectFieldAccessor(FIELD_PORT_NUMBER);
        classEditorBuilder.injectFieldAccessor(FIELD_PROXY_HOST_NAME);
        classEditorBuilder.injectFieldAccessor(FIELD_PROXY_PORT_NUMBER);
        
        MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("open");
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpConnectionOpenMethodInterceptor");

        context.addClassFileTransformer(classEditorBuilder.build());
    }
    
    private void addHttpMethodBaseClass(ProfilerPluginSetupContext context, HttpClient3PluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("org.apache.commons.httpclient.HttpMethodBase");
        MethodTransformerBuilder executeMethodEditorBuilder = classEditorBuilder.editMethod("execute", "org.apache.commons.httpclient.HttpState", "org.apache.commons.httpclient.HttpConnection");
        executeMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        executeMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpMethodBaseExecuteMethodInterceptor");
        
        if(config.isApacheHttpClient3ProfileIo()) {
            MethodTransformerBuilder writeRequestMethodEditorBuilder = classEditorBuilder.editMethod("writeRequest", "org.apache.commons.httpclient.HttpState", "org.apache.commons.httpclient.HttpConnection");
            writeRequestMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            writeRequestMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpMethodBaseRequestAndResponseMethodInterceptor");
            
            MethodTransformerBuilder readResponseMethodEditorBuilder = classEditorBuilder.editMethod("readResponse", "org.apache.commons.httpclient.HttpState", "org.apache.commons.httpclient.HttpConnection");
            readResponseMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            readResponseMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpMethodBaseRequestAndResponseMethodInterceptor");
        }

        context.addClassFileTransformer(classEditorBuilder.build());
    }
}