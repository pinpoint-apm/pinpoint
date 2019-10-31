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

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.reactor.interceptor.CorePublisherInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.CoreSubscriberInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.SubscribeOrReturnMethodInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class ReactorPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    private static final String[] PROCESSOR = {
            "reactor.core.publisher.MonoProcessor",
            "reactor.core.publisher.FluxProcessor",
            "reactor.core.publisher.UnicastProcessor",
            "reactor.core.publisher.DirectProcessor",
            "reactor.core.publisher.EmitterProcessor",
            "reactor.core.publisher.DelegateProcessor",
            "reactor.core.publisher.EventLoopProcessor",
            "reactor.core.publisher.WorkQueueProcessor",
            "reactor.core.publisher.TopicProcessor",
            "reactor.core.publisher.ReplayProcessor"
    };

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ReactorPluginConfig config = new ReactorPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} version range=[3.1.0.RELEASE, 3.3.0.RELEASE], config:{}", this.getClass().getSimpleName(), config);

        for (String className : PROCESSOR) {
            transformTemplate.transform(className, ProcessorTransform.class);
        }

        final Matcher monoMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Mono", true));
        transformTemplate.transform(monoMatcher, FluxAndMonoTransform.class);

        final Matcher fluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Flux", true));
        transformTemplate.transform(fluxMatcher, FluxAndMonoTransform.class);

        final Matcher parallelFluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.ParallelFlux", true));
        transformTemplate.transform(parallelFluxMatcher, ParallelFluxTransform.class);

        final Matcher coreSubscriberMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(coreSubscriberMatcher, CoreSubscriberTransform.class);
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class ProcessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            addCorePublisherInterceptor(target);
            addCoreSubscriberInterceptor(target);

            return target.toBytecode();
        }
    }

    public static class FluxAndMonoTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            addCorePublisherInterceptor(target);
            // since 3.3.0
            addCoreOperatorInterceptor(target);
            addCoreSubscriberInterceptor(target);

            return target.toBytecode();
        }
    }

    public static class ParallelFluxTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber[]");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(CorePublisherInterceptor.class);
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
            addCorePublisherInterceptor(target);
            // since 3.3.0
            addCoreOperatorInterceptor(target);
            addCoreSubscriberInterceptor(target);

            return target.toBytecode();

        }
    }

    private static void addCorePublisherInterceptor(final InstrumentClass target) throws InstrumentException {
        final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
        if (subscribeMethod != null) {
            subscribeMethod.addInterceptor(CorePublisherInterceptor.class);
        }
    }

    private static void addCoreOperatorInterceptor(final InstrumentClass target) throws InstrumentException {
        final InstrumentMethod subscribeOrReturnMethod = target.getDeclaredMethod("subscribeOrReturn", "reactor.core.CoreSubscriber");
        if (subscribeOrReturnMethod != null) {
            subscribeOrReturnMethod.addInterceptor(SubscribeOrReturnMethodInterceptor.class);
        }
    }

    private static void addCoreSubscriberInterceptor(final InstrumentClass target) throws InstrumentException {
        // Skip onSubscribe
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("onNext", "onError", "onComplete"))) {
            method.addInterceptor(CoreSubscriberInterceptor.class);
        }
    }
}