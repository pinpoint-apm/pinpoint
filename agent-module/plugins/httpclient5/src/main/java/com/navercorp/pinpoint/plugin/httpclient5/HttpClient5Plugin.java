/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.AsyncClientConnectionManagerConnectInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.AsyncExecCallbackHandleResponseInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.BasicClientExchangeHandlerConsumeResponseInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.ClientConnectionManagerConnectInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.CloseableHttpAsyncClientDoExecuteInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.CloseableHttpAsyncClientExecuteImmediateMethodInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.DefaultAsyncClientConnectionOperatorConnectInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.DefaultHttpClientConnectionOperatorConnectInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.FutureCancelInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.FutureCompletedInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.FutureFailedInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.InternalHttpClientDoExecuteInterceptor;
import com.navercorp.pinpoint.plugin.httpclient5.interceptor.MinimalHttpAsyncClientExecuteInterceptor;

import java.security.ProtectionDomain;

public class HttpClient5Plugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClient5PluginConfig config = new HttpClient5PluginConfig(context.getConfig());
        if (Boolean.FALSE == config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // Sync(classic)
        // request
        transformTemplate.transform("org.apache.hc.client5.http.impl.classic.InternalHttpClient", InternalHttpClientTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.impl.classic.MinimalHttpClient", InternalHttpClientTransform.class);
        // connect
        transformTemplate.transform("org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager", ClientConnectionManagerTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager", ClientConnectionManagerTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator", DefaultHttpClientConnectionOperatorTransform.class);

        // Async
        // request
        transformTemplate.transform("org.apache.hc.client5.http.impl.async.AbstractMinimalHttpAsyncClientBase", CloseableHttpAsyncClientTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.impl.async.InternalAbstractHttpAsyncClient", CloseableHttpAsyncClientTransform.class);
        // response - InternalAbstractHttpAsyncClient
        transformTemplate.transform("org.apache.hc.core5.http.nio.support.BasicRequestProducer", BasicRequestProducerTransform.class);
        final Matcher asyncExecCallbackMatcher = Matchers.newPackageBasedMatcher("org.apache.hc.client5.http.impl.async.InternalAbstractHttpAsyncClient$", new InterfaceInternalNameMatcherOperand("org.apache.hc.client5.http.async.AsyncExecCallback", false));
        transformTemplate.transform(asyncExecCallbackMatcher, AsyncExecCallbackTransform.class);
        // request - minimal
        transformTemplate.transform("org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient", MinimalHttpAsyncClientTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.impl.async.MinimalH2AsyncClient", MinimalHttpAsyncClientTransform.class);
        // response - AbstractMinimalHttpAsyncClientBase
        transformTemplate.transform("org.apache.hc.core5.http.nio.support.BasicClientExchangeHandler", BasicClientExchangeHandlerTransform.class);
        // connect
        transformTemplate.transform("org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager", AsyncClientConnectionManagerTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.impl.nio.DefaultAsyncClientConnectionOperator", DefaultAsyncClientConnectionOperatorTransform.class);

        // Future
        transformTemplate.transform("org.apache.hc.core5.concurrent.ComplexFuture", FutureTransform.class);
        transformTemplate.transform("org.apache.hc.core5.concurrent.BasicFuture", FutureTransform.class);

        // HttpContext
        transformTemplate.transform("org.apache.hc.client5.http.protocol.HttpClientContext", HttpContextTransform.class);
        transformTemplate.transform("org.apache.hc.client5.http.cache.HttpCacheContext", HttpContextTransform.class);
        transformTemplate.transform("org.apache.hc.core5.http.protocol.BasicHttpContext", HttpContextTransform.class);
        transformTemplate.transform("org.apache.hc.core5.http.protocol.HttpCoreContext", HttpContextTransform.class);
    }

    public static class InternalHttpClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod execute = target.getDeclaredMethod("doExecute", "org.apache.hc.core5.http.HttpHost", "org.apache.hc.core5.http.ClassicHttpRequest", "org.apache.hc.core5.http.protocol.HttpContext");
            if (execute != null) {
                execute.addInterceptor(InternalHttpClientDoExecuteInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ClientConnectionManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod connectMethod = target.getDeclaredMethod("connect", "org.apache.hc.client5.http.io.ConnectionEndpoint", "org.apache.hc.core5.util.TimeValue", "org.apache.hc.core5.http.protocol.HttpContext");
            if (connectMethod != null) {
                connectMethod.addInterceptor(ClientConnectionManagerConnectInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DefaultHttpClientConnectionOperatorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod connectMethod1 = target.getDeclaredMethod("connect", "org.apache.hc.client5.http.io.ManagedHttpClientConnection", "org.apache.hc.core5.http.HttpHost", "java.net.InetSocketAddress", "org.apache.hc.core5.util.TimeValue", "org.apache.hc.core5.http.io.SocketConfig", "org.apache.hc.core5.http.protocol.HttpContext");
            if (connectMethod1 != null) {
                connectMethod1.addScopedInterceptor(DefaultHttpClientConnectionOperatorConnectInterceptor.class, "DefaultHttpClientConnectionOperator_CONNECT");
            }
            // 5.2
            final InstrumentMethod connectMethod2 = target.getDeclaredMethod("connect", "org.apache.hc.client5.http.io.ManagedHttpClientConnection", "org.apache.hc.core5.http.HttpHost", "java.net.InetSocketAddress", "org.apache.hc.core5.util.Timeout", "org.apache.hc.core5.http.io.SocketConfig", "java.lang.Object", "org.apache.hc.core5.http.protocol.HttpContext");
            if (connectMethod2 != null) {
                connectMethod2.addScopedInterceptor(DefaultHttpClientConnectionOperatorConnectInterceptor.class, "DefaultHttpClientConnectionOperator_CONNECT");
            }
            // 5.4
            final InstrumentMethod connectMethod3 = target.getDeclaredMethod("connect", "org.apache.hc.client5.http.io.ManagedHttpClientConnection", "org.apache.hc.core5.http.HttpHost", "org.apache.hc.core5.net.NamedEndpoint", "java.net.InetSocketAddress", "org.apache.hc.core5.util.Timeout", "org.apache.hc.core5.http.io.SocketConfig", "java.lang.Object", "org.apache.hc.core5.http.protocol.HttpContext");
            if (connectMethod3 != null) {
                connectMethod3.addScopedInterceptor(DefaultHttpClientConnectionOperatorConnectInterceptor.class, "DefaultHttpClientConnectionOperator_CONNECT");
            }

            return target.toBytecode();
        }
    }

    public static class CloseableHttpAsyncClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod doEexecuteMethod = target.getDeclaredMethod("doExecute", "org.apache.hc.core5.http.HttpHost", "org.apache.hc.core5.http.nio.AsyncRequestProducer", "org.apache.hc.core5.http.nio.AsyncResponseConsumer", "org.apache.hc.core5.http.nio.HandlerFactory", "org.apache.hc.core5.http.protocol.HttpContext", "org.apache.hc.core5.concurrent.FutureCallback");
            if (doEexecuteMethod != null) {
                doEexecuteMethod.addInterceptor(CloseableHttpAsyncClientDoExecuteInterceptor.class);
            }
            final InstrumentMethod executeImmediateMethod1 = target.getDeclaredMethod("executeImmediate", "org.apache.hc.core5.http.HttpRequest", "org.apache.hc.core5.http.nio.AsyncEntityProducer", "org.apache.hc.client5.http.async.AsyncExecChain$Scope", "org.apache.hc.client5.http.async.AsyncExecCallback");
            if (executeImmediateMethod1 != null) {
                executeImmediateMethod1.addScopedInterceptor(CloseableHttpAsyncClientExecuteImmediateMethodInterceptor.class, "CloseableHttpAsyncClient_EXECUTE_IMMEDIATE");
            }
            final InstrumentMethod executeImmediateMethod2 = target.getDeclaredMethod("executeImmediate", "org.apache.hc.core5.http.HttpRequest", "org.apache.hc.core5.http.nio.AsyncEntityProducer", "org.apache.hc.client5.http.async.AsyncExecChain$Scope", "org.apache.hc.client5.http.async.AsyncExecChain", "org.apache.hc.client5.http.async.AsyncExecCallback");
            if (executeImmediateMethod2 != null) {
                executeImmediateMethod2.addScopedInterceptor(CloseableHttpAsyncClientExecuteImmediateMethodInterceptor.class, "CloseableHttpAsyncClient_EXECUTE_IMMEDIATE");
            }

            return target.toBytecode();
        }
    }

    public static class BasicRequestProducerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addGetter(HttpRequestGetter.class, "request");

            return target.toBytecode();
        }
    }

    public static class AsyncExecCallbackTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod handleResponseMethod = target.getDeclaredMethod("handleResponse", "org.apache.hc.core5.http.HttpResponse", "org.apache.hc.core5.http.EntityDetails");
            if (handleResponseMethod != null) {
                handleResponseMethod.addInterceptor(AsyncExecCallbackHandleResponseInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MinimalHttpAsyncClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute", "org.apache.hc.core5.http.nio.AsyncClientExchangeHandler", "org.apache.hc.core5.http.nio.HandlerFactory", "org.apache.hc.core5.http.protocol.HttpContext");
            if (executeMethod != null) {
                executeMethod.addInterceptor(MinimalHttpAsyncClientExecuteInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class BasicClientExchangeHandlerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod handleResponseMethod = target.getDeclaredMethod("consumeResponse", "org.apache.hc.core5.http.HttpResponse", "org.apache.hc.core5.http.EntityDetails", "org.apache.hc.core5.http.protocol.HttpContext");
            if (handleResponseMethod != null) {
                handleResponseMethod.addInterceptor(BasicClientExchangeHandlerConsumeResponseInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class AsyncClientConnectionManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod connectMethod = target.getDeclaredMethod("connect", "org.apache.hc.client5.http.nio.AsyncConnectionEndpoint", "org.apache.hc.core5.reactor.ConnectionInitiator", "org.apache.hc.core5.util.Timeout", "java.lang.Object", "org.apache.hc.core5.http.protocol.HttpContext", "org.apache.hc.core5.concurrent.FutureCallback");
            if (connectMethod != null) {
                connectMethod.addInterceptor(AsyncClientConnectionManagerConnectInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DefaultAsyncClientConnectionOperatorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod connectMethod1 = target.getDeclaredMethod("connect", "org.apache.hc.core5.reactor.ConnectionInitiator", "org.apache.hc.core5.http.HttpHost", "java.net.SocketAddress", "org.apache.hc.core5.util.Timeout", "java.lang.Object", "org.apache.hc.core5.concurrent.FutureCallback");
            if (connectMethod1 != null) {
                connectMethod1.addScopedInterceptor(DefaultAsyncClientConnectionOperatorConnectInterceptor.class, "DefaultAsyncClientConnectionOperator_CONNECT");
            }
            final InstrumentMethod connectMethod2 = target.getDeclaredMethod("connect", "org.apache.hc.core5.reactor.ConnectionInitiator", "org.apache.hc.core5.http.HttpHost", "org.apache.hc.core5.net.NamedEndpoint", "java.net.SocketAddress", "org.apache.hc.core5.util.Timeout", "java.lang.Object", "org.apache.hc.core5.http.protocol.HttpContext", "org.apache.hc.core5.concurrent.FutureCallback");
            if (connectMethod2 != null) {
                connectMethod2.addScopedInterceptor(DefaultAsyncClientConnectionOperatorConnectInterceptor.class, "DefaultAsyncClientConnectionOperator_CONNECT");
            }

            return target.toBytecode();
        }
    }

    public static class FutureTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod completedMethod = target.getDeclaredMethod("completed", "java.lang.Object");
            if (completedMethod != null) {
                completedMethod.addScopedInterceptor(FutureCompletedInterceptor.class, "FUTURE_COMPLETED");
            }
            final InstrumentMethod failedMethod = target.getDeclaredMethod("failed", "java.lang.Exception");
            if (failedMethod != null) {
                failedMethod.addScopedInterceptor(FutureFailedInterceptor.class, "FUTURE_FAILED");
            }
            final InstrumentMethod cancelMethod = target.getDeclaredMethod("cancel", "boolean");
            if (cancelMethod != null) {
                cancelMethod.addScopedInterceptor(FutureCancelInterceptor.class, "FUTURE_CANCEL");
            }

            return target.toBytecode();
        }
    }

    public static class HttpContextTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
