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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.httpclient3.interceptor.ExecuteInterceptor;
import com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpConnectionOpenMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpMethodBaseExecuteMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient3.interceptor.HttpMethodBaseRequestAndResponseMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient3.interceptor.RetryMethodInterceptor;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 *
 */
public class HttpClient3Plugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClient3PluginConfig config = new HttpClient3PluginConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // apache http client 3
        addHttpClient3Class();

        // apache http client 3 retry
        addDefaultHttpMethodRetryHandlerClass();
        // 3.1.0
        addHttpConnectionClass();
        addHttpMethodBaseClass(config);
    }

    private void addHttpClient3Class() {
        transformTemplate.transform("org.apache.commons.httpclient.HttpClient", HttpClientTransform.class);
    }

    public static class HttpClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumenttor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumenttor.getInstrumentClass(loader, className, classfileBuffer);

            injectHttpClientExecuteMethod(target, "org.apache.commons.httpclient.HttpMethod");
            injectHttpClientExecuteMethod(target, "org.apache.commons.httpclient.HostConfiguration", "org.apache.commons.httpclient.HttpMethod");
            injectHttpClientExecuteMethod(target, "org.apache.commons.httpclient.HostConfiguration", "org.apache.commons.httpclient.HttpMethod", "org.apache.commons.httpclient.HttpState");

            return target.toBytecode();
        }

        private void injectHttpClientExecuteMethod(InstrumentClass target, String... parameterTypeNames) throws InstrumentException {
            InstrumentMethod method = target.getDeclaredMethod("executeMethod", parameterTypeNames);

            if (method != null) {
                method.addScopedInterceptor(ExecuteInterceptor.class, HttpClient3Constants.HTTP_CLIENT3_SCOPE);
            }
        }
    }


    
    private void addDefaultHttpMethodRetryHandlerClass() {
        transformTemplate.transform("org.apache.commons.httpclient.DefaultHttpMethodRetryHandler", DefaultHttpMethodRetryHandlerTransform.class);
    }

    public static class DefaultHttpMethodRetryHandlerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod retryMethod = target.getDeclaredMethod("retryMethod", "org.apache.commons.httpclient.HttpMethod", "java.io.IOException", "int");

            if (retryMethod != null) {
                retryMethod.addInterceptor(RetryMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }
    
    private void addHttpConnectionClass() {
        transformTemplate.transform("org.apache.commons.httpclient.HttpConnection", HttpConnectionTransform.class);
    }

    public static class HttpConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(HostNameGetter.class, HttpClient3Constants.FIELD_HOST_NAME);
            target.addGetter(PortNumberGetter.class, HttpClient3Constants.FIELD_PORT_NUMBER);
            target.addGetter(ProxyHostNameGetter.class, HttpClient3Constants.FIELD_PROXY_HOST_NAME);
            target.addGetter(ProxyPortNumberGetter.class, HttpClient3Constants.FIELD_PROXY_PORT_NUMBER);

            InstrumentMethod open = target.getDeclaredMethod("open");

            if (open != null) {
                open.addInterceptor(HttpConnectionOpenMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }
    
    private void addHttpMethodBaseClass(final HttpClient3PluginConfig config) {
        transformTemplate.transform("org.apache.commons.httpclient.HttpMethodBase", HttpMethodBaseTransform.class);
    }

    public static class HttpMethodBaseTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod execute = target.getDeclaredMethod("execute", "org.apache.commons.httpclient.HttpState", "org.apache.commons.httpclient.HttpConnection");
            if (execute != null) {
                execute.addScopedInterceptor(HttpMethodBaseExecuteMethodInterceptor.class, HttpClient3Constants.HTTP_CLIENT3_METHOD_BASE_SCOPE, ExecutionPolicy.ALWAYS);
            }

            final HttpClient3PluginConfig config = new HttpClient3PluginConfig(instrumentor.getProfilerConfig());
            if (config.isIo()) {
                final InstrumentMethod writeRequest = target.getDeclaredMethod("writeRequest", "org.apache.commons.httpclient.HttpState", "org.apache.commons.httpclient.HttpConnection");
                if (writeRequest != null) {
                    writeRequest.addScopedInterceptor(HttpMethodBaseRequestAndResponseMethodInterceptor.class, HttpClient3Constants.HTTP_CLIENT3_METHOD_BASE_SCOPE, ExecutionPolicy.ALWAYS);
                }

                final InstrumentMethod readResponse = target.getDeclaredMethod("readResponse", "org.apache.commons.httpclient.HttpState", "org.apache.commons.httpclient.HttpConnection");
                if (readResponse != null) {
                    readResponse.addScopedInterceptor(HttpMethodBaseRequestAndResponseMethodInterceptor.class, HttpClient3Constants.HTTP_CLIENT3_METHOD_BASE_SCOPE, ExecutionPolicy.ALWAYS);
                }
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}