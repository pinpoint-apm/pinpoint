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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor;
import com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor;
import com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInterceptor;
import com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteMethodInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class HttpClientPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClientPluginConfig config = new HttpClientPluginConfig(context.getConfig());
        if (Boolean.FALSE == config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        logger.debug("[GoogleHttpClient] Add HttpRequest class.");
        addHttpRequestClass(config);
    }

    private void addHttpRequestClass(final HttpClientPluginConfig config) {
        transformTemplate.transform("com.google.api.client.http.HttpRequest", HttpRequestTransform.class);
    }

    public static class HttpRequestTransform implements TransformCallback {
        private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod execute = target.getDeclaredMethod("execute");
            if (execute != null) {
                execute.addInterceptor(HttpRequestExecuteMethodInterceptor.class);
            }
            final HttpClientPluginConfig config = new HttpClientPluginConfig(instrumentor.getProfilerConfig());
            if (config.isAsync()) {
                InstrumentMethod executeAsync = target.getDeclaredMethod("executeAsync", "java.util.concurrent.Executor");
                if (executeAsync != null) {
                    executeAsync.addScopedInterceptor(HttpRequestExecuteAsyncMethodInterceptor.class, HttpClientConstants.EXECUTE_ASYNC_SCOPE, ExecutionPolicy.ALWAYS);
                }

                for (InstrumentClass nestedClass : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("executeAsync", "java.util.concurrent.Executor"), ClassFilters.interfaze("java.util.concurrent.Callable")))) {
                    logger.debug("Find nested class {}", target.getName());
                    instrumentor.transform(loader, nestedClass.getName(), NestedClassTransform.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class NestedClassTransform implements TransformCallback {
        private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            InstrumentMethod constructor = target.getConstructor("com.google.api.client.http.HttpRequest");
            if (constructor != null) {
                logger.debug("Add constructor interceptor for nested class {}", target.getName());
                constructor.addScopedInterceptor(HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor.class, HttpClientConstants.EXECUTE_ASYNC_SCOPE, ExecutionPolicy.ALWAYS);
            }

            InstrumentMethod m = target.getDeclaredMethod("call");
            if (m != null) {
                logger.debug("Add method interceptor for nested class {}.{}", target.getName(), m.getName());
                m.addInterceptor(HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}