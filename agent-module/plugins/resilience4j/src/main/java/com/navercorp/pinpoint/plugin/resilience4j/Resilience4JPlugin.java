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

package com.navercorp.pinpoint.plugin.resilience4j;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnNextInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeOrReturnInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorActualAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriberAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.resilience4j.interceptor.CircuitBreakerSubscriberInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class Resilience4JPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final Resilience4JPluginConfig config = new Resilience4JPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        transformTemplate.transform("io.github.resilience4j.reactor.circuitbreaker.operator.MonoCircuitBreaker", MonoCircuitBreakerTransform.class);
        transformTemplate.transform("io.github.resilience4j.reactor.circuitbreaker.operator.FluxCircuitBreaker", FluxCircuitBreakerTransform.class);
        transformTemplate.transform("io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerSubscriber", CircuitBreakerSubscriberTransform.class);
    }

    public static class MonoCircuitBreakerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class);
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class FluxCircuitBreakerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class);
            }
            // since 3.3.0
            final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
            if (subscribeOrReturnMethod != null) {
                subscribeOrReturnMethod.addInterceptor(FluxAndMonoSubscribeOrReturnInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class CircuitBreakerSubscriberTransform implements TransformCallback {
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
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(Resilience4JConstants.RESILIENCE4J));
            }

            // protected void hookOnError(Throwable e)
            final InstrumentMethod hookOnErrorMethod = target.getDeclaredMethod("hookOnError", "java.lang.Throwable");
            if (hookOnErrorMethod != null) {
                hookOnErrorMethod.addInterceptor(CircuitBreakerSubscriberInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}