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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.vertx.interceptor.ContextImplExecuteBlockingInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.ContextImplRunOnContextInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HandleExceptionInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HandlerInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientImplDoRequestInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientImplInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplDoHandleResponseInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientRequestImplInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientResponseImplInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpClientStreamInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerRequestImplHandleEndInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.HttpServerResponseImplInterceptor;
import com.navercorp.pinpoint.plugin.vertx.interceptor.ServerConnectionHandleRequestInterceptor;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
@InterfaceStability.Unstable
public class VertxPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final VertxConfig config = new VertxConfig(context.getConfig());
        if (!config.isEnable() || (!config.isEnableHttpServer() && !config.isEnableHttpClient())) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }

            // 3.3 <= x <= 3.5
        logger.info("Enable VertxPlugin. version range=[3.3, 3.5.0]");
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // for vertx.io 3.3.x, 3.4.x
        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final VertxDetector vertxDetector = new VertxDetector(config.getBootstrapMains());
            if (vertxDetector.detect()) {
                logger.info("Detected application type : {}", VertxConstants.VERTX);
                if (!context.registerApplicationType(VertxConstants.VERTX)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), VertxConstants.VERTX);
                }
            }
        }

        logger.info("Adding Vertx transformers");

        final List<String> basePackageNames = filterBasePackageNames(config.getHandlerBasePackageNames());
        if (!basePackageNames.isEmpty()) {
            // add async field & interceptor
            addHandlerInterceptor(basePackageNames);
            logger.info("Adding Vertx Handler. base-packages={}.", config.getHandlerBasePackageNames());

            // runOnContext, executeBlocking
            addContextImpl("io.vertx.core.impl.ContextImpl");
            addContextImpl("io.vertx.core.impl.EventLoopContext");
            addContextImpl("io.vertx.core.impl.MultiThreadedWorkerContext");
            addContextImpl("io.vertx.core.impl.WorkerContext");
        }

        if (config.isEnableHttpServer()) {
            logger.info("Adding Vertx HTTP Server.");
            final VertxHttpServerConfig serverConfig = new VertxHttpServerConfig(context.getConfig());
            final String requestHandlerMethodName = serverConfig.getRequestHandlerMethodName();
            if (requestHandlerMethodName == null || requestHandlerMethodName.isEmpty()) {
                logger.warn("Not found 'profiler.vertx.http.server.request-handler.method.name' in config");
            } else {
                try {
                    final String className = toClassName(requestHandlerMethodName);
                    final String methodName = toMethodName(requestHandlerMethodName);
                    logger.info("Add request handler method for Vertx HTTP Server. class={}, method={}", className, methodName);
                    addRequestHandlerMethod(className, methodName);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid 'profiler.vertx.http.server.request-handler.method.name' value={}", requestHandlerMethodName);
                }
            }
            addHttpServerRequestImpl();
            addHttpServerResponseImpl();
        }

        if (config.isEnableHttpClient()) {
            logger.info("Adding Vertx HTTP Client.");
            addHttpClientImpl();
            addHttpClientRequestImpl();
            addHttpClientStream();
            addHttpClientResponseImpl();
        }
    }

    List<String> filterBasePackageNames(List<String> basePackageNames) {
        final List<String> list = new ArrayList<String>();
        for (String basePackageName : basePackageNames) {
            final String name = basePackageName.trim();
            if (!name.isEmpty()) {
                list.add(name);
            }
        }
        return list;
    }

    String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition).trim();
    }

    String toMethodName(String fullQualifiedMethodName) {
        final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(methodBeginPosition + 1).trim();
    }


    private void addHandlerInterceptor(final List<String> basePackageNames) {
        // basepackageNames AND io.vertx.core.Handler
        final Matcher matcher = Matchers.newPackageBasedMatcher(basePackageNames, new InterfaceInternalNameMatcherOperand("io.vertx.core.Handler", true));
        transformTemplate.transform(matcher, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField(AsyncContextAccessor.class);
                final InstrumentMethod handleMethod = target.getDeclaredMethod("handle", "java.lang.Object");
                if (handleMethod != null) {
                    handleMethod.addInterceptor(HandlerInterceptor.class);
                }

                return target.toBytecode();
            }
        });
    }

    private void addContextImpl(final String className) {
        transformTemplate.transform(className, ContextImplTransform.class);
    }

    public static class ContextImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod runOnContextMethod = target.getDeclaredMethod("runOnContext", "io.vertx.core.Handler");
            if (runOnContextMethod != null) {
                runOnContextMethod.addInterceptor(ContextImplRunOnContextInterceptor.class);
            }

            final InstrumentMethod executeBlockingMethod1 = target.getDeclaredMethod("executeBlocking", "io.vertx.core.impl.Action", "io.vertx.core.Handler");
            if (executeBlockingMethod1 != null) {
                executeBlockingMethod1.addInterceptor(ContextImplExecuteBlockingInterceptor.class);
            }

            final InstrumentMethod executeBlockingMethod2 = target.getDeclaredMethod("executeBlocking", "io.vertx.core.Handler", "boolean", "io.vertx.core.Handler");
            if (executeBlockingMethod2 != null) {
                executeBlockingMethod2.addInterceptor(ContextImplExecuteBlockingInterceptor.class);
            }

            // internal
            final InstrumentMethod executeBlockingMethod3 = target.getDeclaredMethod("executeBlocking", "io.vertx.core.Handler", "io.vertx.core.impl.TaskQueue", "io.vertx.core.Handler");
            if (executeBlockingMethod3 != null) {
                executeBlockingMethod3.addInterceptor(ContextImplExecuteBlockingInterceptor.class);
            }

            // skip executeFromIO()
            return target.toBytecode();
        }
    }

    private void addRequestHandlerMethod(final String className, final String methodName) {
        transformTemplate.transform(className, RequestHandlerMethodTransform.class, new Object[]{methodName}, new Class[]{String.class});
    }

    public static class RequestHandlerMethodTransform implements TransformCallback {
        private final String methodName;

        public RequestHandlerMethodTransform(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final InstrumentMethod handleRequestMethod = target.getDeclaredMethod(methodName, "io.vertx.core.http.HttpServerRequest");
            if (handleRequestMethod != null) {
                // entry point & set asynchronous of req, res.
                handleRequestMethod.addInterceptor(ServerConnectionHandleRequestInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addHttpServerRequestImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpServerRequestImpl", HttpServerRequestImplTransform.class);
    }

    public static class HttpServerRequestImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
            if (handleExceptionMethod != null) {
                handleExceptionMethod.addInterceptor(HandleExceptionInterceptor.class);
            }

            final InstrumentMethod handleEndMethod = target.getDeclaredMethod("handleEnd");
            if (handleEndMethod != null) {
                handleEndMethod.addInterceptor(HttpServerRequestImplHandleEndInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addHttpServerResponseImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpServerResponseImpl", HttpServerResponseImplTransform.class);
    }

    public static class HttpServerResponseImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod endMethod = target.getDeclaredMethod("end");
            if (endMethod != null) {
                endMethod.addInterceptor(HttpServerResponseImplInterceptor.class);
            }

            final InstrumentMethod endBufferMethod = target.getDeclaredMethod("end", "io.vertx.core.buffer.Buffer");
            if (endBufferMethod != null) {
                endBufferMethod.addInterceptor(HttpServerResponseImplInterceptor.class);
            }

            final InstrumentMethod closeMethod = target.getDeclaredMethod("close");
            if (closeMethod != null) {
                closeMethod.addInterceptor(HttpServerResponseImplInterceptor.class);
            }

            for (InstrumentMethod sendFileMethod : target.getDeclaredMethods(MethodFilters.name("sendFile"))) {
                if (sendFileMethod != null) {
                    sendFileMethod.addInterceptor(HttpServerResponseImplInterceptor.class);
                }
            }

            final InstrumentMethod handleDrainedMethod = target.getDeclaredMethod("handleDrained");
            if (handleDrainedMethod != null) {
                handleDrainedMethod.addInterceptor(HttpServerResponseImplInterceptor.class);
            }

            final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
            if (handleExceptionMethod != null) {
                handleExceptionMethod.addInterceptor(HandleExceptionInterceptor.class);
            }

            final InstrumentMethod handleClosedMethod = target.getDeclaredMethod("handleClosed");
            if (handleClosedMethod != null) {
                handleClosedMethod.addInterceptor(HttpServerResponseImplInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addHttpClientImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpClientImpl", HttpClientImplTransform.class);
    }

    public static class HttpClientImplTransform implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("requestAbs", "request", "get", "getAbs", "getNow", "post", "postAbs", "head", "headAbs", "headNow", "options", "optionsAbs", "optionsNow", "put", "putAbs", "delete", "deleteAbs"))) {
            if (method != null) {
                method.addScopedInterceptor(HttpClientImplInterceptor.class, VertxConstants.HTTP_CLIENT_REQUEST_SCOPE);
            }
        }

        // 3.3.x
        final InstrumentMethod doRequestMethod = target.getDeclaredMethod("doRequest", "io.vertx.core.http.HttpMethod", "java.lang.String", "int", "java.lang.String", "io.vertx.core.MultiMap");
        if (doRequestMethod != null) {
            doRequestMethod.addInterceptor(HttpClientImplDoRequestInterceptor.class);
        }

        // 3.4.0, 3.4.1
        final InstrumentMethod createRequestMethod1 = target.getDeclaredMethod("createRequest", "io.vertx.core.http.HttpMethod", "java.lang.String", "int", "java.lang.Boolean", "java.lang.String", "io.vertx.core.MultiMap");
        if (createRequestMethod1 != null) {
            createRequestMethod1.addScopedInterceptor(HttpClientImplDoRequestInterceptor.class, VertxConstants.HTTP_CLIENT_CREATE_REQUEST_SCOPE);
        }

        // 3.4.2
        final InstrumentMethod createRequestMethod2 = target.getDeclaredMethod("createRequest", "io.vertx.core.http.HttpMethod", "java.lang.String", "java.lang.String", "int", "java.lang.Boolean", "java.lang.String", "io.vertx.core.MultiMap");
        if (createRequestMethod2 != null) {
            createRequestMethod2.addScopedInterceptor(HttpClientImplDoRequestInterceptor.class, VertxConstants.HTTP_CLIENT_CREATE_REQUEST_SCOPE);
        }

        return target.toBytecode();
    }
}

    private void addHttpClientRequestImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpClientRequestImpl", HttpClientRequestImplTransform.class);
    }

    public static class HttpClientRequestImplTransform implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        target.addField(AsyncContextAccessor.class);

        // for HttpClientResponseImpl.
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("doHandleResponse"))) {
            if (method != null) {
                method.addInterceptor(HttpClientRequestImplDoHandleResponseInterceptor.class);
            }
        }

        // for stream.writeHead(), stream.writeHeadWithContent(), headersCompletionHandler.
        final InstrumentMethod connectedMethod = target.getDeclaredMethod("connected", "io.vertx.core.http.impl.HttpClientStream", "io.vertx.core.Handler");
        if (connectedMethod != null) {
            connectedMethod.addInterceptor(HttpClientRequestImplInterceptor.class);
        }

        // handle.
        final InstrumentMethod handleDrainedMethod = target.getDeclaredMethod("handleDrained");
        if (handleDrainedMethod != null) {
            handleDrainedMethod.addInterceptor(HttpClientRequestImplInterceptor.class);
        }

        final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
        if (handleExceptionMethod != null) {
            handleExceptionMethod.addInterceptor(HandleExceptionInterceptor.class);
        }

        return target.toBytecode();
    }
}

    private void addHttpClientStream() {
        transformTemplate.transform("io.vertx.core.http.impl.ClientConnection", ClientConnectionTransform.class);
    }

    public static class ClientConnectionTransform implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

        // add pinpoint headers.
        final InstrumentMethod prepareHeadersMethod = target.getDeclaredMethod("prepareHeaders", "io.netty.handler.codec.http.HttpRequest", "java.lang.String", "boolean");
        if (prepareHeadersMethod != null) {
            prepareHeadersMethod.addInterceptor(HttpClientStreamInterceptor.class);
        }

        return target.toBytecode();
    }
}

    private void addHttpClientResponseImpl() {
        transformTemplate.transform("io.vertx.core.http.impl.HttpClientResponseImpl", HttpClientResponseImplTransform.class);
    }

    public static class HttpClientResponseImplTransform implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        target.addField(AsyncContextAccessor.class);

        final InstrumentMethod handleEndMethod = target.getDeclaredMethod("handleEnd", "io.vertx.core.buffer.Buffer", "io.vertx.core.MultiMap");
        if (handleEndMethod != null) {
            handleEndMethod.addInterceptor(HttpClientResponseImplInterceptor.class);
        }

        final InstrumentMethod handleExceptionMethod = target.getDeclaredMethod("handleException", "java.lang.Throwable");
        if (handleExceptionMethod != null) {
            handleExceptionMethod.addInterceptor(HandleExceptionInterceptor.class);
        }

        return target.toBytecode();
    }
}

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}