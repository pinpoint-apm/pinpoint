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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
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
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.BasicFutureMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.DefaultClientExchangeHandlerImplStartMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.DefaultHttpRequestRetryHandlerRetryRequestMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpAsyncClientExecuteMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpClientConnectionManagerConnectMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.HttpRequestExecutorExecuteMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient4.interceptor.ManagedClientConnectionOpenMethodInterceptor;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 *
 */
public class HttpClient4Plugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        if (logger.isInfoEnabled()) {
            HttpClient4PluginConfig config = new HttpClient4PluginConfig(context.getConfig());
            logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        }
        // common
        addHttpRequestExecutorClass();
        addDefaultHttpRequestRetryHandlerClass();

        logger.debug("Add HttpClient4(4.0 ~ 4.2");
        // Apache httpclient4 (version 4.0 ~ 4.2)
        addAbstractHttpClient4Class();
        addAbstractPooledConnAdapterClass();
        addManagedClientConnectionImplClass();
        
        // Apache httpclient4 (version 4.3 ~ 4.4)
        logger.debug("Add CloseableHttpClient4(4.3 ~ ");
        addCloseableHttpClientClass();
        addBasicHttpClientConnectionManagerClass();
        addPoolingHttpClientConnectionManagerClass();

        // Apache httpAsyncClient4 (version 4.0)
        // unsupported AbstractHttpAsyncClient because of deprecated.
        logger.debug("Add CloseableHttpAsyncClient4(4.0 ~ ");
        addClosableHttpAsyncClientClass();
        addDefaultClientExchangeHandlerImplClass();
        addBasicFutureClass();
    }

    private void addHttpRequestExecutorClass() {
        transformTemplate.transform("org.apache.http.protocol.HttpRequestExecutor", HttpRequestExecutorTransform.class);
    }

    public static class HttpRequestExecutorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod execute = target.getDeclaredMethod("execute", "org.apache.http.HttpRequest", "org.apache.http.HttpClientConnection", "org.apache.http.protocol.HttpContext");
            if (execute != null) {
                execute.addScopedInterceptor(HttpRequestExecutorExecuteMethodInterceptor.class, HttpClient4Constants.HTTP_CLIENT4_SCOPE, ExecutionPolicy.ALWAYS);
            }

            InstrumentMethod doSendRequest = target.getDeclaredMethod("doSendRequest", "org.apache.http.HttpRequest", "org.apache.http.HttpClientConnection", "org.apache.http.protocol.HttpContext");
            if (doSendRequest != null) {
                doSendRequest.addScopedInterceptor(HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor.class, HttpClient4Constants.HTTP_CLIENT4_SCOPE, ExecutionPolicy.ALWAYS);
            }

            InstrumentMethod doReceiveResponse = target.getDeclaredMethod("doReceiveResponse", "org.apache.http.HttpRequest", "org.apache.http.HttpClientConnection", "org.apache.http.protocol.HttpContext");
            if (doReceiveResponse != null) {
                doReceiveResponse.addScopedInterceptor(HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor.class, HttpClient4Constants.HTTP_CLIENT4_SCOPE, ExecutionPolicy.ALWAYS);
            }

            return target.toBytecode();
        }
    }
    
    private void addAbstractHttpClient4Class() {
        transformTemplate.transform("org.apache.http.impl.client.AbstractHttpClient", AbstractHttpClientTransform.class);
    }

    public static class AbstractHttpClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, false, "org.apache.http.client.methods.HttpUriRequest");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, false, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

            return target.toBytecode();
        }

    }


    private void addCloseableHttpClientClass() {
        transformTemplate.transform("org.apache.http.impl.client.CloseableHttpClient", CloseableHttpClientTransform.class);
    }

    public static class CloseableHttpClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, false, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpRequestInterceptor(target, true, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, false, "org.apache.http.client.methods.HttpUriRequest");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, false, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler");
            HttpClientUtils.injectHttpClientExecuteMethodWithHttpUriRequestInterceptor(target, true, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext");

            return target.toBytecode();
        }

    }

    private void addDefaultHttpRequestRetryHandlerClass() {
        transformTemplate.transform("org.apache.http.impl.client.DefaultHttpRequestRetryHandler", DefaultHttpRequestRetryHandlerTransformer.class);
    }

    public static class DefaultHttpRequestRetryHandlerTransformer implements  TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod retryRequest = target.getDeclaredMethod("retryRequest", "java.io.IOException", "int", "org.apache.http.protocol.HttpContext");

            if (retryRequest != null) {
                retryRequest.addInterceptor(DefaultHttpRequestRetryHandlerRetryRequestMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addAbstractPooledConnAdapterClass() {
        transformTemplate.transform("org.apache.http.impl.conn.AbstractPooledConnAdapter", AbstractPooledConnAdapterTransform.class);
    }

    public static class AbstractPooledConnAdapterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod open = target.getDeclaredMethod("open", "org.apache.http.conn.routing.HttpRoute", "org.apache.http.protocol.HttpContext", "org.apache.http.params.HttpParams");
            if (open != null) {
                open.addInterceptor(ManagedClientConnectionOpenMethodInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addManagedClientConnectionImplClass() {
        transformTemplate.transform("org.apache.http.impl.conn.ManagedClientConnectionImpl", ManagedClientConnectionImplTransform.class);
    }

    public static class ManagedClientConnectionImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod open = target.getDeclaredMethod("open", "org.apache.http.conn.routing.HttpRoute", "org.apache.http.protocol.HttpContext", "org.apache.http.params.HttpParams");

            if (open != null) {
                open.addInterceptor(ManagedClientConnectionOpenMethodInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addBasicHttpClientConnectionManagerClass() {
        transformTemplate.transform("org.apache.http.impl.conn.BasicHttpClientConnectionManager", BasicHttpClientConnectionManagerTransform.class);
    }

    public static class BasicHttpClientConnectionManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod connect = target.getDeclaredMethod("connect", "org.apache.http.HttpClientConnection", "org.apache.http.conn.routing.HttpRoute", "int", "org.apache.http.protocol.HttpContext");

            if (connect != null) {
                connect.addInterceptor(HttpClientConnectionManagerConnectMethodInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addPoolingHttpClientConnectionManagerClass() {
        transformTemplate.transform("org.apache.http.impl.conn.PoolingHttpClientConnectionManager", PoolingHttpClientConnectionManagerTransform.class);
    }

    public static class PoolingHttpClientConnectionManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod connect = target.getDeclaredMethod("connect", "org.apache.http.HttpClientConnection", "org.apache.http.conn.routing.HttpRoute", "int", "org.apache.http.protocol.HttpContext");

            if (connect != null) {
                connect.addInterceptor(HttpClientConnectionManagerConnectMethodInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addClosableHttpAsyncClientClass() {
        transformTemplate.transform("org.apache.http.impl.nio.client.CloseableHttpAsyncClient", CloseableHttpAsyncClient.class);
    }

    public static class CloseableHttpAsyncClient implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            injectHttpAsyncClientExecuteMethodInterceptor(target, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");
            injectHttpAsyncClientExecuteMethodInterceptor(target, "org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.concurrent.FutureCallback");
            injectHttpAsyncClientExecuteMethodInterceptor(target, "org.apache.http.nio.protocol.HttpAsyncRequestProducer", "org.apache.http.nio.protocol.HttpAsyncResponseConsumer", "org.apache.http.concurrent.FutureCallback");
            injectHttpAsyncClientExecuteMethodInterceptor(target, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.concurrent.FutureCallback");
            injectHttpAsyncClientExecuteMethodInterceptor(target, "org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext", "org.apache.http.concurrent.FutureCallback");

            return target.toBytecode();
        }

        private void injectHttpAsyncClientExecuteMethodInterceptor(InstrumentClass target, String... parameterTypeNames) throws InstrumentException {
            InstrumentMethod execute = target.getDeclaredMethod("execute", parameterTypeNames);

            if (execute != null) {
                execute.addScopedInterceptor(HttpAsyncClientExecuteMethodInterceptor.class, HttpClient4Constants.HTTP_CLIENT4_SCOPE);
            }
        }
    }


    private void addDefaultClientExchangeHandlerImplClass() {
        transformTemplate.transform("org.apache.http.impl.nio.client.DefaultClientExchangeHandlerImpl", DefaultClientExchangeHandlerImplTransform.class);
    }

    public static class DefaultClientExchangeHandlerImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(RequestProducerGetter.class, HttpClient4Constants.FIELD_REQUEST_PRODUCER);
            target.addGetter(ResultFutureGetter.class, HttpClient4Constants.FIELD_RESULT_FUTURE);

            InstrumentMethod start = target.getDeclaredMethod("start");

            if (start != null) {
                start.addInterceptor(DefaultClientExchangeHandlerImplStartMethodInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addBasicFutureClass() {
        transformTemplate.transform("org.apache.http.concurrent.BasicFuture", BasicFutureTransform.class);
    }

    public static class BasicFutureTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(AsyncContextAccessor.class);

            InstrumentMethod get = target.getDeclaredMethod("get");
            if (get != null) {
                get.addInterceptor(BasicFutureMethodInterceptor.class);
            }

            InstrumentMethod get2 = target.getDeclaredMethod("get", "long", "java.util.concurrent.TimeUnit");
            if (get2 != null) {
                get2.addInterceptor(BasicFutureMethodInterceptor.class);
            }

            InstrumentMethod completed = target.getDeclaredMethod("completed", "java.lang.Object");
            if (completed != null) {
                completed.addInterceptor(BasicFutureMethodInterceptor.class);
            }

            InstrumentMethod failed = target.getDeclaredMethod("failed", "java.lang.Exception");
            if (failed != null) {
                failed.addInterceptor(BasicFutureMethodInterceptor.class);

            }

            InstrumentMethod cancel = target.getDeclaredMethod("cancel", "boolean");
            if (cancel != null) {
                cancel.addInterceptor(BasicFutureMethodInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}