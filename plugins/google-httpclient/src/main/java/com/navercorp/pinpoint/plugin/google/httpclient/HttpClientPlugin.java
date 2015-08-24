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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;

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

    private void addHttpRequestClass(ProfilerPluginSetupContext context, final HttpClientPluginConfig config) {
        context.addClassFileTransformer("com.google.api.client.http.HttpRequest", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                
                InstrumentMethod execute = target.getDeclaredMethod("execute", new String[] {});
                
                if (execute != null) {
                    execute.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteMethodInterceptor");
                }
                
                
                if (config.isAsync()) {
                    InstrumentMethod executeAsync = target.getDeclaredMethod("executeAsync", "java.util.concurrent.Executor");
                    
                    if (executeAsync != null) {
                        executeAsync.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInterceptor");
                    }
                }
                        
                return target.toBytecode();
            }
        });
    }

    private void addHttpRequestExecuteAsyncMethodInnerClass(ProfilerPluginSetupContext context, String targetClassName) {
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(AsyncTraceIdAccessor.class.getName());

                if (target.hasConstructor("com.google.api.client.http.HttpRequest") && target.hasMethod("call", "com.google.api.client.http.HttpResponse")) {
                    InstrumentMethod constructor = target.getConstructor("com.google.api.client.http.HttpRequest");
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor");
                    
                    for (InstrumentMethod m : target.getDeclaredMethods(new HttpRequestExceuteAsyncMethodInnerClassMethodFilter())) {
                        try {
                            m.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor");
                        } catch (Throwable t) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("[GoogleHttpClient] Unsupported method " + className + "." + m.getName(), t);
                            }
                        }
                        
                    }
                }
                
                return target.toBytecode();
            }
        });
   }
}