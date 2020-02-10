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
import com.navercorp.pinpoint.plugin.reactor.interceptor.PeriodicSchedulerTaskRunMethodInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.SchedulerAndWorkerScheduleMethodInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.SchedulerAndWorkerTaskRunMethodInterceptor;
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

    private static final String[] SCHEDULER_AND_WORKER_LIST = {
            // Scheduler - Provides an abstract asynchronous boundary to operators.
            "reactor.core.scheduler.BoundedElasticScheduler",
            "reactor.core.scheduler.DelegateServiceScheduler",
            "reactor.core.scheduler.SingleScheduler",
            "reactor.core.scheduler.ParallelScheduler",
            "reactor.core.scheduler.ImmediateScheduler",
            "reactor.core.scheduler.ElasticScheduler",
            "reactor.core.scheduler.ExecutorScheduler",
            "reactor.core.scheduler.SingleWorkerScheduler",
            // Worker - A worker representing an asynchronous boundary that executes tasks.
            "reactor.core.scheduler.BoundedElasticScheduler$ActiveWorker",
            "reactor.core.scheduler.BoundedElasticScheduler$DeferredWorker",
            "reactor.core.scheduler.ElasticScheduler$ElasticWorker",
            "reactor.core.scheduler.ExecutorScheduler$ExecutorSchedulerTrampolineWorker",
            "reactor.core.scheduler.ExecutorScheduler$ExecutorSchedulerWorker",
            "reactor.core.scheduler.ExecutorServiceWorker",
            "reactor.core.scheduler.ImmediateScheduler$ImmediateSchedulerWorker"
    };

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ReactorPluginConfig config = new ReactorPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} version range=[3.1.0.RELEASE, 3.3.0.RELEASE], config:{}", this.getClass().getSimpleName(), config);

        addProcessor();
        addPublisher();
        if (config.isTraceSchedule()) {
            addScheduler(config);
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addProcessor() {
        for (String className : PROCESSOR) {
            transformTemplate.transform(className, ProcessorTransform.class);
        }
    }

    private void addPublisher() {
        final Matcher monoMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Mono", true));
        transformTemplate.transform(monoMatcher, FluxAndMonoTransform.class);
        final Matcher fluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Flux", true));
        transformTemplate.transform(fluxMatcher, FluxAndMonoTransform.class);
        final Matcher parallelFluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.ParallelFlux", true));
        transformTemplate.transform(parallelFluxMatcher, ParallelFluxTransform.class);
        final Matcher coreSubscriberMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(coreSubscriberMatcher, CoreSubscriberTransform.class);
    }

    private void addScheduler(final ReactorPluginConfig config) {
        for (String className : SCHEDULER_AND_WORKER_LIST) {
            transformTemplate.transform(className, SchedulerAndWorkerTransform.class);
        }
        // Task & Runnable
        transformTemplate.transform("reactor.core.scheduler.SchedulerTask", SchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.WorkerTask", WorkerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.ExecutorScheduler$ExecutorPlainRunnable", ExecutorPlainRunnableTransform.class);
        transformTemplate.transform("reactor.core.scheduler.ExecutorScheduler$ExecutorTrackedRunnable", ExecutorTrackedRunnableTransform.class);
        transformTemplate.transform("reactor.core.scheduler.InstantPeriodicWorkerTask", InstantPeriodicWorkerTaskTransform.class);

        if (config.isTraceSchedulePeriodically()) {
            transformTemplate.transform("reactor.core.scheduler.PeriodicSchedulerTask", PeriodicSchedulerTaskTransform.class);
            transformTemplate.transform("reactor.core.scheduler.PeriodicWorkerTask", PeriodicSchedulerTaskTransform.class);
        } else {
            transformTemplate.transform("reactor.core.scheduler.PeriodicSchedulerTask", InstantPeriodicWorkerTaskTransform.class);
            transformTemplate.transform("reactor.core.scheduler.PeriodicWorkerTask", InstantPeriodicWorkerTaskTransform.class);
        }
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

    public static class SchedulerAndWorkerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Disposable schedule(Runnable task);
            final InstrumentMethod scheduleMethod = target.getDeclaredMethod("schedule", "java.lang.Runnable");
            if (scheduleMethod != null) {
                scheduleMethod.addInterceptor(SchedulerAndWorkerScheduleMethodInterceptor.class);
            }
            // Disposable schedule(Runnable task, long delay, TimeUnit unit)
            final InstrumentMethod scheduleDelayMethod = target.getDeclaredMethod("schedule", "java.lang.Runnable", "long", "java.util.concurrent.TimeUnit");
            if (scheduleDelayMethod != null) {
                scheduleDelayMethod.addInterceptor(SchedulerAndWorkerScheduleMethodInterceptor.class);
            }
            // Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit)
            final InstrumentMethod schedulePeriodicallyMethod = target.getDeclaredMethod("schedulePeriodically", "java.lang.Runnable", "long", "long", "java.util.concurrent.TimeUnit");
            if (schedulePeriodicallyMethod != null) {
                schedulePeriodicallyMethod.addInterceptor(SchedulerAndWorkerScheduleMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SchedulerTaskTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            // Void call();
            addSchedulerAndWorkerTaskRunMethodInterceptor(target, "call");
            return target.toBytecode();
        }
    }

    public static class ExecutorPlainRunnableTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            // void run()
            addSchedulerAndWorkerTaskRunMethodInterceptor(target, "run");
            return target.toBytecode();
        }
    }

    public static class WorkerTaskTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            // Void call();
            addSchedulerAndWorkerTaskRunMethodInterceptor(target, "call");
            return target.toBytecode();
        }
    }

    public static class ExecutorTrackedRunnableTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            // void run();
            addSchedulerAndWorkerTaskRunMethodInterceptor(target, "run");
            return target.toBytecode();
        }
    }

    public static class InstantPeriodicWorkerTaskTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            // Void call();
            addSchedulerAndWorkerTaskRunMethodInterceptor(target, "call");
            return target.toBytecode();
        }
    }

    public static class PeriodicSchedulerTaskTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            // Periodc scheduler task
            final InstrumentMethod callMethod = target.getDeclaredMethod("call");
            if (callMethod != null) {
                callMethod.addInterceptor(PeriodicSchedulerTaskRunMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private static void addSchedulerAndWorkerTaskRunMethodInterceptor(final InstrumentClass target, String name, String... parameterTypes) throws InstrumentException {
        final InstrumentMethod scheduleRunMethod = target.getDeclaredMethod(name, parameterTypes);
        if (scheduleRunMethod != null) {
            scheduleRunMethod.addInterceptor(SchedulerAndWorkerTaskRunMethodInterceptor.class);
        }
    }
}