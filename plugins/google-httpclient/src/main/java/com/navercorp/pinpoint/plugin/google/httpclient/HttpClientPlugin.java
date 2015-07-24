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
package com.navercorp.pinpoint.plugin.google.httpclient;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerExceptionHandler;

/**
 * @author jaehong.kim
 *
 */
public class HttpClientPlugin implements ProfilerPlugin, HttpClientConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    
    @Override
    public void setup(ProfilerPluginContext context) {
        addHttpRequestClass(context);
        addHttpRequestExecuteAsyncMethodInnerClass(context);
    }

    private void addHttpRequestClass(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classBuilder = context.getClassFileTransformerBuilder("com.google.api.client.http.HttpRequest");

        MethodTransformerBuilder executeMethodBuilder = classBuilder.editMethod("execute");
        executeMethodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteMethodInterceptor");
        
        MethodTransformerBuilder executeAsyncMethodBuilder = classBuilder.editMethod("executeAsync", "java.util.concurrent.Executor");
        executeAsyncMethodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInterceptor");

        context.addClassFileTransformer(classBuilder.build());
    }
    
    private void addHttpRequestExecuteAsyncMethodInnerClass(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classBuilder = context.getClassFileTransformerBuilder("com.google.api.client.http.HttpRequest$1");
        classBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);
        
        ConstructorTransformerBuilder constructorBuilder = classBuilder.editConstructor("com.google.api.client.http.HttpRequest");
        constructorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor");
        
        classBuilder.conditional(ClassConditions.hasMethod("call", "com.google.api.client.http.HttpResponse"),new ConditionalClassFileTransformerSetup() {
            
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                MethodTransformerBuilder methodBuilder = conditional.editMethods(new HttpRequestExceuteAsyncMethodInnerClassMethodFilter());
                methodBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
                    public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
                        if (logger.isWarnEnabled()) {
                            logger.warn("[HttpClientPlugin] Unsupported method " + targetClassName + "." + targetMethodName, exception);
                        }
                    }
                });
                methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor");
            }
        }); 
        context.addClassFileTransformer(classBuilder.build());
    }
}