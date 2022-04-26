/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.netty;

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
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.ChannelOperationsChannelMethodInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.ChannelOperationsInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.ChannelOperationsOnInboundCompleteMethodInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpClientHandlerRequestWithBodyInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpClientHandlerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpClientOperationsOnInboundNextInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpClientOperationsOnOutboundCompleteInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpClientOperationsOnOutboundErrorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpClientOperationsSendInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpServerHandleHttpServerStateInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpServerHandleStateInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpTcpClientConnectInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class ReactorNettyPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ReactorNettyPluginConfig config = new ReactorNettyPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} version range=[0.8.2.RELEASE, 1.0.4.RELEASE], config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final ReactorNettyDetector detector = new ReactorNettyDetector(config.getBootstrapMains());
            if (detector.detect()) {
                logger.info("Detected application type : {}", ReactorNettyConstants.REACTOR_NETTY);
                if (!context.registerApplicationType(ReactorNettyConstants.REACTOR_NETTY)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), ReactorNettyConstants.REACTOR_NETTY);
                }
            }
        }

        // HTTP server
        transformTemplate.transform("reactor.netty.http.server.HttpServerHandle", HttpServerHandleTransform.class);
        // over reactor-netty-1.0
        transformTemplate.transform("reactor.netty.http.server.HttpServer$HttpServerHandle", HttpServerHandleTransform.class);

        transformTemplate.transform("reactor.netty.channel.ChannelOperations", ChannelOperationsTransform.class);
        transformTemplate.transform("reactor.netty.http.server.HttpServerOperations", HttpServerOperationsTransform.class);

        // HTTP client
        if (Boolean.TRUE == config.isClientEnable()) {
            // over reactor-netty-1.0
            transformTemplate.transform("reactor.netty.http.client.HttpClientConnect", HttpClientConnectTransform.class);

            transformTemplate.transform("reactor.netty.http.client.HttpClientConnect$HttpTcpClient", HttpTcpClientTransform.class);
            transformTemplate.transform("reactor.netty.http.client.HttpClientConnect$HttpClientHandler", HttpClientHandleTransform.class);
            transformTemplate.transform("reactor.netty.http.client.HttpClientOperations", HttpClientOperationsTransform.class);
        }

        transformTemplate.transform("reactor.netty.ByteBufFlux", FluxAndMonoOperatorTransform.class);
        transformTemplate.transform("reactor.netty.ByteBufFluxFuseable", FluxAndMonoOperatorTransform.class);
        transformTemplate.transform("reactor.netty.ByteBufMono", FluxAndMonoOperatorTransform.class);
        transformTemplate.transform("reactor.netty.ByteBufMonoFuseable", FluxAndMonoOperatorTransform.class);
        transformTemplate.transform("reactor.netty.DeferredFutureMono", FluxAndMonoTransform.class);
        transformTemplate.transform("reactor.netty.ImmediateFutureMono", FluxAndMonoTransform.class);
        transformTemplate.transform("reactor.netty.FluxReceive", FluxAndMonoTransform.class);
        transformTemplate.transform("reactor.netty.MonoSend", FluxAndMonoTransform.class);
        transformTemplate.transform("reactor.netty.MonoSendMany", FluxAndMonoTransform.class);

        final Matcher coreSubscriberMatcher = Matchers.newPackageBasedMatcher("reactor.netty", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(coreSubscriberMatcher, CoreSubscriberTransform.class);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class HttpServerHandleTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("onStateChange", "reactor.netty.Connection", "reactor.netty.ConnectionObserver$State");
            if (method != null) {
                if (instrumentor.exist(loader, "reactor.netty.http.server.HttpServerState")) {
                    // over reactor-netty.0.7.x
                    method.addInterceptor(HttpServerHandleHttpServerStateInterceptor.class);
                } else {
                    method.addInterceptor(HttpServerHandleStateInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class ChannelOperationsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // HTTP server end-point
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("terminate", "onInboundError", "onInboundCancel", "onTerminate", "dispose", "onComplete", "onError"))) {
                method.addInterceptor(ChannelOperationsInterceptor.class);
            }

            final InstrumentMethod onInboundCompleteMethod = target.getDeclaredMethod("onInboundComplete");
            if (onInboundCompleteMethod != null) {
                onInboundCompleteMethod.addInterceptor(ChannelOperationsOnInboundCompleteMethodInterceptor.class);
            }

            // HTTP server end-point(defense code for try ~ catch)
            final InstrumentMethod channelMethod = target.getDeclaredMethod("channel");
            if (channelMethod != null) {
                channelMethod.addInterceptor(ChannelOperationsChannelMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpServerOperationsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }

    // Over reactor-netty-1.0
    public static class HttpClientConnectTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod method = target.getDeclaredMethod("connect");
            if (method != null) {
                method.addInterceptor(HttpTcpClientConnectInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpTcpClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod method = target.getDeclaredMethod("connect", "io.netty.bootstrap.Bootstrap");
            if (method != null) {
                method.addInterceptor(HttpTcpClientConnectInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpClientHandleTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            // Over reactor-netty-1.0
            InstrumentMethod constructor = target.getConstructor("reactor.netty.http.client.HttpClientConfig");
            if (constructor != null) {
                constructor.addInterceptor(HttpClientHandlerConstructorInterceptor.class);
            } else {
                // For compatibility
                constructor = target.getConstructor("reactor.netty.http.client.HttpClientConfiguration", "java.net.SocketAddress", "reactor.netty.tcp.SslProvider", "reactor.netty.tcp.ProxyProvider");
                if (constructor != null) {
                    constructor.addInterceptor(HttpClientHandlerConstructorInterceptor.class);
                }
            }
            final InstrumentMethod method = target.getDeclaredMethod("requestWithBody", "reactor.netty.http.client.HttpClientOperations");
            if (method != null) {
                method.addInterceptor(HttpClientHandlerRequestWithBodyInterceptor.class);
            }

            return target.toBytecode();
        }
    }


    public static class HttpClientOperationsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod sendMethod = target.getDeclaredMethod("followRedirectPredicate", "java.util.function.BiPredicate");
            if (sendMethod != null) {
                sendMethod.addInterceptor(HttpClientOperationsSendInterceptor.class);
            }
            final InstrumentMethod onOutboundCompleteMethod = target.getDeclaredMethod("onOutboundComplete");
            if (onOutboundCompleteMethod != null) {
                onOutboundCompleteMethod.addInterceptor(HttpClientOperationsOnOutboundCompleteInterceptor.class);
            }
            final InstrumentMethod onOutboundErrorMethod = target.getDeclaredMethod("onOutboundError", "java.lang.Throwable");
            if (onOutboundErrorMethod != null) {
                onOutboundErrorMethod.addInterceptor(HttpClientOperationsOnOutboundErrorInterceptor.class);
            }

            final InstrumentMethod onInboundNextMethod = target.getDeclaredMethod("onInboundNext", "io.netty.channel.ChannelHandlerContext", "java.lang.Object");
            if (onInboundNextMethod != null) {
                onInboundNextMethod.addInterceptor(HttpClientOperationsOnInboundNextInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class FluxAndMonoTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(FluxAndMonoConstructorInterceptor.class);
                }
            }

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoSubscribeInterceptor.class, va(ReactorNettyConstants.REACTOR_NETTY_INTERNAL));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeInterceptor.class, va(ReactorNettyConstants.REACTOR_NETTY_INTERNAL));
            }

            return target.toBytecode();
        }
    }

    public static class FluxAndMonoOperatorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(FluxAndMonoOperatorConstructorInterceptor.class);
                }
            }
            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class, va(ReactorNettyConstants.REACTOR_NETTY_INTERNAL));
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class, va(ReactorNettyConstants.REACTOR_NETTY_INTERNAL));
            }
            return target.toBytecode();
        }
    }

    public static class CoreSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(CoreSubscriberConstructorInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }
}
