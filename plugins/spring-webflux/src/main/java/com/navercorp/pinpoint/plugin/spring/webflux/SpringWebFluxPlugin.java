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
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.BodyInserterRequestBuilderConstructorInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.BodyInserterRequestBuilderWriteToInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.ClientResponseFunctionInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DefaultWebClientExchangeMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DispatchHandlerHandleMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.DispatchHandlerInvokeHandlerMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.ExchangeFunctionMethodInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class SpringWebFluxPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final SpringWebFluxPluginConfig config = new SpringWebFluxPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }

        logger.info("{} version range=[5.0.0.RELEASE, 5.2.1.RELEASE], config:{}", this.getClass().getSimpleName(), config);
        // Server
        transformTemplate.transform("org.springframework.web.reactive.DispatcherHandler", DispatchHandlerTransform.class);
        transformTemplate.transform("org.springframework.web.server.adapter.DefaultServerWebExchange", ServerWebExchangeTransform.class);

        // Client
        transformTemplate.transform("org.springframework.web.reactive.function.client.DefaultWebClient$DefaultRequestBodyUriSpec", DefaultWebClientTransform.class);
        transformTemplate.transform("org.springframework.web.reactive.function.client.ExchangeFunctions$DefaultExchangeFunction", ExchangeFunctionTransform.class);
        transformTemplate.transform("org.springframework.web.reactive.function.client.DefaultClientRequestBuilder$BodyInserterRequest", BodyInserterRequestTransform.class);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class DispatchHandlerTransform implements TransformCallback {
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
                invokerHandlerMethod.addInterceptor(DispatchHandlerInvokeHandlerMethodInterceptor.class);
            }
            // Result
            final InstrumentMethod handleResultMethod = target.getDeclaredMethod("handleResult", "org.springframework.web.server.ServerWebExchange", "org.springframework.web.reactive.HandlerResult");
            if (handleResultMethod != null) {
                handleResultMethod.addInterceptor(DispatchHandlerInvokeHandlerMethodInterceptor.class);
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
                logResponseMethod.addInterceptor(ClientResponseFunctionInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class BodyInserterRequestTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            // Set sample rate(s0)
            final InstrumentMethod constructor = target.getConstructor("org.springframework.http.HttpMethod", "java.net.URI", "org.springframework.http.HttpHeaders", "org.springframework.util.MultiValueMap", "org.springframework.web.reactive.function.BodyInserter", "java.util.Map");
            if (constructor != null) {
                constructor.addInterceptor(BodyInserterRequestBuilderConstructorInterceptor.class);
            }

            // RPC
            final InstrumentMethod method = target.getDeclaredMethod("writeTo", "org.springframework.http.client.reactive.ClientHttpRequest", "org.springframework.web.reactive.function.client.ExchangeStrategies");
            if (method != null) {
                method.addInterceptor(BodyInserterRequestBuilderWriteToInterceptor.class);
            }

            return target.toBytecode();
        }
    }
}
