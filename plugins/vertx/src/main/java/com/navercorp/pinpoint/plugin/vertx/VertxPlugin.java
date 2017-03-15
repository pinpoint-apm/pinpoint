/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;

import java.security.ProtectionDomain;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class VertxPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final VertxConfig config = new VertxConfig(context.getConfig());
        if (!config.isEnable() || (!config.isEnableHttpServer() && !config.isEnableHttpClient())) {
            return;
        }
        // for vertx.io 3.x
        final VertxDetector vertxDetector = new VertxDetector(config.getBootstrapMains());
        context.addApplicationTypeDetector(vertxDetector);

        boolean hasHandlers = false;
        for (String className : config.getHandlerClassNames()) {
            final String classNameTrim = className.trim();
            if (classNameTrim.isEmpty()) {
                continue;
            }

            if (logger.isInfoEnabled()) {
                logger.info("Adding Vertx Handler {}.", classNameTrim);
            }
            addHandlerInterceptor(classNameTrim);
            hasHandlers = true;
        }

        if (hasHandlers) {
            // runOnContext, executeBlocking
            addVertxImpl();
        }

        if (config.isEnableHttpServer()) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding Vertx HTTP Server.");
            }
            addServerConnection();
            addHttpServerRequestImpl();
            addHttpServerResponseImpl();
        }

        if (config.isEnableHttpClient()) {
            if (logger.isInfoEnabled()) {
                logger.info("Adding Vertx HTTP Client.");
            }
            addHttpClientImpl();
            addHttpClientRequestImpl();
            addHttpClientStream();
            addHttpClientResponseImpl();
        }
    }

    private void addHandlerInterceptor(final String className) {
        transformTemplate.transform(className, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncTraceIdAccessor.class.getName());

                final InstrumentMethod handleMethod = target.getDeclaredMethod("handle", "java.lang.Object");
                if (handleMethod != null) {
                    handleMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HandlerInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addVertxImpl() {
        transformTemplate.transform("io.vertx.core.impl.VertxImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                final InstrumentMethod runOnContextMethod = target.getDeclaredMethod("runOnContext", "io.vertx.core.Handler");
                if (runOnContextMethod != null) {
                    runOnContextMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.VertxImplRunOnContextInterceptor");
                }

                final InstrumentMethod executeBlockingMethod = target.getDeclaredMethod("executeBlocking", "io.vertx.core.Handler", "boolean", "io.vertx.core.Handler");
                if (executeBlockingMethod != null) {
                    executeBlockingMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.VertxImplExecuteBlockingInterceptor");
                }

                return target.toBytecode();
            }
        });
    }


    private void addServerConnection() {
        transformTemplate.transform("io.vertx.core.http.impl.ServerConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                final InstrumentMethod handleRequestMethod = target.getDeclaredMethod("handleRequest", "io.vertx.core.http.impl.HttpServerRequestImpl", "io.vertx.core.http.impl.HttpServerResponseImpl");
                if (handleRequestMethod != null) {
                    // entry point & set asynchronous of req, res.
                    handleRequestMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.ServerConnectionHandleRequestInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpServerRequestImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpServerRequestImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncTraceIdAccessor.class.getName());

                final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
                if (handleExceptionMethod != null) {
                    handleExceptionMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HandleExceptionInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpServerResponseImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpServerResponseImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncTraceIdAccessor.class.getName());

                final InstrumentMethod endMethod = target.getDeclaredMethod("end");
                if (endMethod != null) {
                    endMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor");
                }

                final InstrumentMethod endBufferMethod = target.getDeclaredMethod("end", "io.vertx.core.buffer.Buffer");
                if (endBufferMethod != null) {
                    endBufferMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor");
                }

                final InstrumentMethod closeMethod = target.getDeclaredMethod("close");
                if (closeMethod != null) {
                    closeMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor");
                }

                for (InstrumentMethod sendFileMethod : target.getDeclaredMethods(MethodFilters.name("sendFile"))) {
                    if (sendFileMethod != null) {
                        sendFileMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor");
                    }
                }

                final InstrumentMethod handleDrainedMethod = target.getDeclaredMethod("handleDrained");
                if (handleDrainedMethod != null) {
                    handleDrainedMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor");
                }

                final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
                if (handleExceptionMethod != null) {
                    handleExceptionMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HandleExceptionInterceptor");
                }

                final InstrumentMethod handleClosedMethod = target.getDeclaredMethod("handleClosed");
                if (handleClosedMethod != null) {
                    handleClosedMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpClientImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpClientImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("requestAbs", "request", "get", "getAbs", "getNow", "post", "postAbs", "head", "headAbs", "headNow", "options", "optionsAbs", "optionsNow", "put", "putAbs", "delete", "deleteAbs"))) {
                    if (method != null) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientImplInterceptor", VertxConstants.HTTP_CLIENT_REQUEST_SCOPE);
                    }
                }

                final InstrumentMethod doRequestMethod = target.getDeclaredMethod("doRequest", "io.vertx.core.http.HttpMethod", "java.lang.String", "int", "java.lang.String", "io.vertx.core.MultiMap");
                if (doRequestMethod != null) {
                    doRequestMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientImplDoRequestInterceptor");
                }

                // connect
                final InstrumentMethod getConnectionForRequestMethod = target.getDeclaredMethod("getConnectionForRequest", "int", "java.lang.String", "io.vertx.core.http.impl.Waiter");
                if (getConnectionForRequestMethod != null) {
                    getConnectionForRequestMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientImplGetConnectionForRequest");
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpClientRequestImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpClientRequestImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncTraceIdAccessor.class.getName());

                // for HttpClientResponseImpl.
                final InstrumentMethod doHandleResponseMethod = target.getDeclaredMethod("doHandleResponse", "io.vertx.core.http.impl.HttpClientResponseImpl");
                if (doHandleResponseMethod != null) {
                    doHandleResponseMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplDoHandleResponseInterceptor");
                }

                // for completionHandler, writeHead(), connect().
                final InstrumentMethod sendHeadMethod = target.getDeclaredMethod("sendHead", "io.vertx.core.Handler");
                if (sendHeadMethod != null) {
                    sendHeadMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplInterceptor");
                }

                // for stream.writeHeadWithContent().
                final InstrumentMethod writeMethod = target.getDeclaredMethod("write", "io.netty.buffer.ByteBuf", "boolean");
                if (writeMethod != null) {
                    writeMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplInterceptor");
                }

                // for stream.writeHead(), stream.writeHeadWithContent(), headersCompletionHandler.
                final InstrumentMethod connectedMethod = target.getDeclaredMethod("connected", "io.vertx.core.http.impl.HttpClientStream", "io.vertx.core.Handler");
                if (connectedMethod != null) {
                    connectedMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplInterceptor");
                }

                // handle.
                final InstrumentMethod handleDrainedMethod = target.getDeclaredMethod("handleDrained", "java.lang.Throwable");
                if (handleDrainedMethod != null) {
                    handleDrainedMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplInterceptor");
                }

                final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
                if (handleExceptionMethod != null) {
                    handleExceptionMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HandleExceptionInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpClientStream() {
        transformTemplate.transform("io.vertx.core.http.impl.ClientConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // add pinpoint headers.
                final InstrumentMethod prepareHeadersMethod = target.getDeclaredMethod("prepareHeaders", "io.netty.handler.codec.http.HttpRequest", "java.lang.String", "boolean");
                if (prepareHeadersMethod != null) {
                    prepareHeadersMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientStreamInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addHttpClientResponseImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpClientResponseImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncTraceIdAccessor.class.getName());

                final InstrumentMethod handleEndMethod = target.getDeclaredMethod("handleEnd", "io.vertx.core.buffer.Buffer", "io.vertx.core.MultiMap");
                if (handleEndMethod != null) {
                    handleEndMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientResponseImplInterceptor");
                }

                final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
                if (handleExceptionMethod != null) {
                    handleExceptionMethod.addInterceptor("com.navercorp.pinpoint.plugin.vertx.interceptor.HandleExceptionInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}