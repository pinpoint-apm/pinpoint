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

package com.navercorp.pinpoint.plugin.spring.webflux;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParametersBuilder;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnNextInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorActualAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriberAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.AbstractHandlerMethodMappingInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.AbstractUrlHandlerMappingInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.BodyInserterRequestBuilderWriteToInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.ClientResponseFunctionInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DefaultWebClientExchangeMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DispatchHandlerGetLambdaInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DispatchHandlerHandleMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DispatchHandlerInvokeHandlerMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.ExchangeFunctionMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.InvocableHandlerMethodInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class SpringWebFluxPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final SpringWebFluxPluginConfig config = new SpringWebFluxPluginConfig(context.getConfig());
        if (Boolean.FALSE == config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }

        logger.info("{}, config:{}", this.getClass().getSimpleName(), config);
        // Server
        transformTemplate.transform("org.springframework.web.reactive.DispatcherHandler", DispatchHandlerTransform.class,
                TransformCallbackParametersBuilder.newBuilder()
                        .addBoolean(config.isUriStatEnable())
                        .addBoolean(config.isUriStatUseUserInput())
                        .build());
        final Matcher invokeMatcher = Matchers.newLambdaExpressionMatcher("org.springframework.web.reactive.DispatcherHandler", "java.util.function.Function");
        transformTemplate.transform(invokeMatcher, DispatchHandlerInvokeHandlerTransform.class);

        transformTemplate.transform("org.springframework.web.server.adapter.DefaultServerWebExchange", ServerWebExchangeTransform.class);
        transformTemplate.transform("org.springframework.web.reactive.result.method.InvocableHandlerMethod", InvocableHandlerMethodTransform.class);

        // Reactive
        final Matcher httpServerFluxMatcher = Matchers.newPackageBasedMatcher("org.springframework.http.server.reactive", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Flux", true));
        transformTemplate.transform(httpServerFluxMatcher, FluxAndMonoTransform.class);
        final Matcher httpServerMonoMatcher = Matchers.newPackageBasedMatcher("org.springframework.http.server.reactive", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Mono", true));
        transformTemplate.transform(httpServerMonoMatcher, FluxAndMonoTransform.class);
        final Matcher httpServerCoreSubscriberMatcher = Matchers.newPackageBasedMatcher("org.springframework.http.server.reactive", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(httpServerCoreSubscriberMatcher, CoreSubscriberTransform.class);

        // Client
        if (Boolean.TRUE == config.isClientEnable()) {
            // If there is a conflict with Reactor-Netty, set it to false.
            transformTemplate.transform("org.springframework.web.reactive.function.client.DefaultWebClient$DefaultRequestBodyUriSpec", DefaultWebClientTransform.class);
            transformTemplate.transform("org.springframework.web.reactive.function.client.ExchangeFunctions$DefaultExchangeFunction", ExchangeFunctionTransform.class);
            transformTemplate.transform("org.springframework.web.reactive.function.client.DefaultClientRequestBuilder$BodyInserterRequest", BodyInserterRequestTransform.class);
            // Reactive
            final Matcher httpClientFluxMatcher = Matchers.newPackageBasedMatcher("org.springframework.http.client.reactive", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Flux", true));
            transformTemplate.transform(httpClientFluxMatcher, FluxAndMonoTransform.class);
            final Matcher httpClientMonoMatcher = Matchers.newPackageBasedMatcher("org.springframework.http.client.reactive", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Mono", true));
            transformTemplate.transform(httpClientMonoMatcher, FluxAndMonoTransform.class);
            final Matcher httpClientCoreSubscriberMatcher = Matchers.newPackageBasedMatcher("org.springframework.http.client.reactive", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
            transformTemplate.transform(httpClientCoreSubscriberMatcher, CoreSubscriberTransform.class);
        }

        // uri stat
        if (config.isUriStatEnable()) {
            transformTemplate.transform("org.springframework.web.reactive.result.method.AbstractHandlerMethodMapping", AbstractHandlerMethodMappingTransform.class,
                    TransformCallbackParametersBuilder.newBuilder()
                            .addBoolean(config.isUriStatCollectMethod())
                            .build());
            transformTemplate.transform("org.springframework.web.reactive.handler.AbstractUrlHandlerMapping", AbstractUrlHandlerMappingTransform.class,
                    TransformCallbackParametersBuilder.newBuilder()
                            .addBoolean(config.isUriStatCollectMethod())
                            .build());
        }


    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class DispatchHandlerTransform implements TransformCallback {
        private final Boolean uriStatEnable;
        private final Boolean uriStatUseUserInput;

        public DispatchHandlerTransform(Boolean uriStatEnable, Boolean uriStatUseUserInput) {
            this.uriStatEnable = uriStatEnable;
            this.uriStatUseUserInput = uriStatUseUserInput;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // Dispatch
            final InstrumentMethod handleMethod = target.getDeclaredMethod("handle", "org.springframework.web.server.ServerWebExchange");
            if (handleMethod != null) {
                handleMethod.addInterceptor(DispatchHandlerHandleMethodInterceptor.class);
            }
            // Invoke
            final InstrumentMethod invokerHandlerMethod = target.getDeclaredMethod("invokeHandler", "org.springframework.web.server.ServerWebExchange", "java.lang.Object");
            if (invokerHandlerMethod != null) {
                invokerHandlerMethod.addInterceptor(DispatchHandlerInvokeHandlerMethodInterceptor.class, va(this.uriStatEnable, Boolean.FALSE));
            }
            // 6.x
            final InstrumentMethod handleRequestWithMethod = target.getDeclaredMethod("handleRequestWith", "org.springframework.web.server.ServerWebExchange", "java.lang.Object");
            if (handleRequestWithMethod != null) {
                handleRequestWithMethod.addInterceptor(DispatchHandlerInvokeHandlerMethodInterceptor.class, va(this.uriStatEnable, Boolean.FALSE));
            }
            // Result
            final InstrumentMethod handleResultMethod = target.getDeclaredMethod("handleResult", "org.springframework.web.server.ServerWebExchange", "org.springframework.web.reactive.HandlerResult");
            if (handleResultMethod != null) {
                handleResultMethod.addInterceptor(DispatchHandlerInvokeHandlerMethodInterceptor.class, va(this.uriStatEnable, this.uriStatUseUserInput));
            }
            // 6.x
            final InstrumentMethod handleResultMethod2 = target.getDeclaredMethod("handleResult", "org.springframework.web.server.ServerWebExchange", "org.springframework.web.reactive.HandlerResult", "java.lang.String");
            if (handleResultMethod2 != null) {
                handleResultMethod2.addInterceptor(DispatchHandlerInvokeHandlerMethodInterceptor.class, va(this.uriStatEnable, this.uriStatUseUserInput));
            }

            return target.toBytecode();
        }
    }

    public static class DispatchHandlerInvokeHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            // flatMap(handler -> invokeHandler(exchange, handler))
            // flatMap(result -> handleResult(exchange, result))
            InstrumentMethod handlerAndResultGetLambdaMethod = target.getConstructor("org.springframework.web.reactive.DispatcherHandler", "org.springframework.web.server.ServerWebExchange");
            if (handlerAndResultGetLambdaMethod != null) {
                handlerAndResultGetLambdaMethod.addInterceptor(DispatchHandlerGetLambdaInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ServerWebExchangeTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }

    public static class InvocableHandlerMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod invokerMethod = target.getDeclaredMethod("invoke", "org.springframework.web.server.ServerWebExchange", "org.springframework.web.reactive.BindingContext", "java.lang.Object[]");
            if (invokerMethod != null) {
                invokerMethod.addInterceptor(InvocableHandlerMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class DefaultWebClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Set AsyncContext
            final InstrumentMethod exchangeMethod = target.getDeclaredMethod("exchange");
            if (exchangeMethod != null) {
                exchangeMethod.addInterceptor(DefaultWebClientExchangeMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ExchangeFunctionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Set AsyncContext
            final InstrumentMethod exchangeMethod = target.getDeclaredMethod("exchange", "org.springframework.web.reactive.function.client.ClientRequest");
            if (exchangeMethod != null) {
                exchangeMethod.addInterceptor(ExchangeFunctionMethodInterceptor.class);
            }

            final InstrumentMethod logResponseMethod = target.getDeclaredMethod("logResponse", "org.springframework.http.client.reactive.ClientHttpResponse", "java.lang.String");
            if (logResponseMethod != null) {
                final int springVersion = SpringVersion.getVersion(loader);
                logResponseMethod.addInterceptor(ClientResponseFunctionInterceptor.class, va(springVersion));
            }

            return target.toBytecode();
        }
    }

    public static class BodyInserterRequestTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            // RPC
            final InstrumentMethod method = target.getDeclaredMethod("writeTo", "org.springframework.http.client.reactive.ClientHttpRequest", "org.springframework.web.reactive.function.client.ExchangeStrategies");
            if (method != null) {
                method.addInterceptor(BodyInserterRequestBuilderWriteToInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class AbstractHandlerMethodMappingTransform implements TransformCallback {
        private final Boolean uriStatCollectMethod;

        public AbstractHandlerMethodMappingTransform(Boolean uriStatCollectMethod) {
            this.uriStatCollectMethod = uriStatCollectMethod;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // Add attribute listener.
            final InstrumentMethod lookupHandlerMethod = target.getDeclaredMethod("lookupHandlerMethod", "org.springframework.web.server.ServerWebExchange");
            if (lookupHandlerMethod != null) {
                final int springVersion = SpringVersion.getVersion(classLoader);
                lookupHandlerMethod.addInterceptor(AbstractHandlerMethodMappingInterceptor.class, va(uriStatCollectMethod, springVersion));
            }
            return target.toBytecode();
        }
    }

    public static class AbstractUrlHandlerMappingTransform implements TransformCallback {
        private final Boolean uriStatCollectMethod;

        public AbstractUrlHandlerMappingTransform(Boolean uriStatCollectMethod) {
            this.uriStatCollectMethod = uriStatCollectMethod;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // Add attribute listener.
            final InstrumentMethod exposePathWithinMapping = target.getDeclaredMethod("lookupHandler", "org.springframework.http.server.PathContainer", "org.springframework.web.server.ServerWebExchange");
            if (exposePathWithinMapping != null) {
                final int springVersion = SpringVersion.getVersion(classLoader);
                exposePathWithinMapping.addInterceptor(AbstractUrlHandlerMappingInterceptor.class, va(uriStatCollectMethod, springVersion));
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

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoSubscribeInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class CoreSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorActualAccessor.class);
            target.addField(ReactorSubscriberAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(CoreSubscriberConstructorInterceptor.class);
                }
            }

            final InstrumentMethod onSubscribeMethod = target.getDeclaredMethod("onSubscribe", "org.reactivestreams.Subscription");
            if (onSubscribeMethod != null) {
                onSubscribeMethod.addInterceptor(CoreSubscriberOnSubscribeInterceptor.class);
            }
            final InstrumentMethod onNextMethod = target.getDeclaredMethod("onNext", "java.lang.Object");
            if (onNextMethod != null) {
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(SpringWebFluxConstants.SPRING_WEBFLUX));
            }

            return target.toBytecode();
        }
    }
}