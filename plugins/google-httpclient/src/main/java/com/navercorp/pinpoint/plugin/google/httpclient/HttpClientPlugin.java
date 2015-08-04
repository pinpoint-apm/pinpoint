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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerExceptionHandler;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 * @author jaehong.kim
 *
 */
public class HttpClientPlugin implements ProfilerPlugin, HttpClientConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClientPluginConfig config = new HttpClientPluginConfig(context.getConfig());
        logger.debug("[GoogleHttpClient] Initialized config={}", config);

        if (!config.isEnable()) {
            return;
        }

        logger.debug("[GoogleHttpClient] Add HttpRequest class.");
        addHttpRequestClass(context, config);

        if (config.isAsync()) {
            final int max = config.getMaxAnonymousInnerClassNameNumber();
            for (int i = 1; i <= max; i++) {
                final String targetClassName = "com.google.api.client.http.HttpRequest$" + i;
                logger.debug("[GoogleHttpClient] Add {} class.", targetClassName);
                addHttpRequestExecuteAsyncMethodInnerClass(context, targetClassName);
            }
        }
    }

    private void addHttpRequestClass(ProfilerPluginSetupContext context, HttpClientPluginConfig config) {
        final ClassFileTransformerBuilder classBuilder = context.getClassFileTransformerBuilder("com.google.api.client.http.HttpRequest");

        MethodTransformerBuilder executeMethodBuilder = classBuilder.editMethod("execute");
        executeMethodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        executeMethodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteMethodInterceptor");

        if (config.isAsync()) {
            MethodTransformerBuilder executeAsyncMethodBuilder = classBuilder.editMethod("executeAsync", "java.util.concurrent.Executor");
            executeAsyncMethodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            executeAsyncMethodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInterceptor");
        }

        context.addClassFileTransformer(classBuilder.build());
    }

    private void addHttpRequestExecuteAsyncMethodInnerClass(ProfilerPluginSetupContext context, String targetClassName) {
        final ClassFileTransformerBuilder classBuilder = context.getClassFileTransformerBuilder(targetClassName);
        classBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);

        classBuilder.conditional(new ClassCondition() {
            @Override
            public boolean check(ProfilerPluginSetupContext context, ClassLoader classLoader, InstrumentClass target) {
                if (!target.hasConstructor(new String[] { "com.google.api.client.http.HttpRequest" })) {
                    return false;
                }

                if (!target.hasMethod("call", null, "com.google.api.client.http.HttpResponse")) {
                    return false;
                }
                
                return true;
            }
        }, new ConditionalClassFileTransformerSetup() {
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                ConstructorTransformerBuilder constructorBuilder = conditional.editConstructor("com.google.api.client.http.HttpRequest");
                constructorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor");
                
                MethodTransformerBuilder methodBuilder = conditional.editMethods(new HttpRequestExceuteAsyncMethodInnerClassMethodFilter());
                methodBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
                    public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
                        if (logger.isWarnEnabled()) {
                            logger.warn("[GoogleHttpClient] Unsupported method " + targetClassName + "." + targetMethodName, exception);
                        }
                    }
                });
                methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor");
            }
        });
       context.addClassFileTransformer(classBuilder.build());
    }
}