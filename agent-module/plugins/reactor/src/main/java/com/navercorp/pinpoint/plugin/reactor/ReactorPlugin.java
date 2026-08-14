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
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberOnNextInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.SchedulerTaskConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.SchedulerTaskRunInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeOrReturnInterceptor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.reactor.interceptor.CoreSubscriberRunInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoDelayInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoIntervalInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoPublishOnInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoSubscribeMethodInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoSubscribeOnInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.OnErrorSubscriberInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.ParallelFluxSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.PeriodicSchedulerTaskRunInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RetrySubscriberResubscribeInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RetrySubscriberSeedInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RetryWhenMainSubscriberInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RunnableSubscriptionConstructorInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.RunnableSubscriptionInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.TimeoutMainSubscriberDoTimeoutInterceptor;
import com.navercorp.pinpoint.plugin.reactor.interceptor.WrappingFluxAndMonoPublishOnInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class ReactorPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private static final String PERIODIC_SCHEDULER_TASK_SCOPE = "##REACTOR_PERIODIC_SCHEDULER_TASK";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
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
            "reactor.core.publisher.ReplayProcessor",
            "reactor.core.publisher.NextProcessor"
    };

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ReactorPluginConfig config = new ReactorPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{}, config:{}", this.getClass().getSimpleName(), config);

        addFluxAndMono(config.isTracePublishOn() && config.isWrapPublisherPublishOn());
        addThreadingAndSchedulers();
        if (config.isTraceSchedulerTask() || !config.isSubscriberInstrument()) {
            // The carrier is REQUIRED when the generic subscriber layer is off: a scheduler hop
            // then has no operator relay, so without the carrier's run() trace window, user code
            // after the hop (e.g. a WebClient call assembled inside flatMap) runs out-of-trace
            // and outbound header propagation silently breaks (verified end to end).
            if (!config.isTraceSchedulerTask()) {
                logger.info("profiler.reactor.subscriber.instrument=false requires the scheduler task carrier - enabling it (profiler.reactor.trace.scheduler.task)");
            }
            addSchedulerTasks();
        }
        if (config.isTracePeriodicSchedulerTask()) {
            addPeriodicSchedulerTasks();
        }
        addProcessor();
        addFlux();
        addMono();
        addParallelFlux();
        addTimeout();
        addRetry();
        addOnError();
        if (config.isSubscriberInstrument()) {
            // The generic per-operator layer (200+ reactor.core.publisher CoreSubscriber types:
            // addField + constructor/onSubscribe/onNext/run). The dedicated subscriber transforms
            // above (timeout/retry/onError/scheduler tasks) are exact-name registrations, so they
            // stay regardless of this gate. Turning this off keeps the publisher field injection
            // and the subscribe relay; boundary linking then travels through the scheduler-task
            // carrier (forced on above) and the seam wrappers instead of the operator chain.
            addCoreSubscriber();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    private void addFluxAndMono(boolean wrapPublishOn) {
        if (wrapPublishOn) {
            transformTemplate.transform("reactor.core.publisher.Flux", FluxPublishOnSeamMethodTransform.class);
            transformTemplate.transform("reactor.core.publisher.Mono", MonoPublishOnSeamMethodTransform.class);
            return;
        }
        transformTemplate.transform("reactor.core.publisher.Flux", FluxMethodTransform.class);
        transformTemplate.transform("reactor.core.publisher.Mono", MonoMethodTransform.class);
    }

    private void addThreadingAndSchedulers() {
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnValue$ScheduledEmpty", RunnableSubscriptionTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnValue$ScheduledScalar", RunnableSubscriptionTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxSubscribeOnCallable$CallableSubscribeOnSubscription", RunnableSubscriptionTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxInterval$IntervalRunnable", RunnableSubscriptionTransform.class);
        transformTemplate.transform("reactor.core.publisher.MonoDelay$MonoDelayRunnable", RunnableSubscriptionTransform.class);
    }

    /**
     * One-shot scheduler task carriers: every non-periodic task the built-in schedulers wrap a
     * submitted Runnable in before it crosses to a worker thread. The carrier receives the
     * AsyncContext at construction and re-activates it around run()/call() on the worker thread.
     * <p>
     * The virtual-thread boundedElastic task exists only as a {@code META-INF/versions/21} entry
     * (reactor 3.6+), so on JDK 8~20 — and on versions without the other classes (3.1.x has no
     * InstantPeriodicWorkerTask) — the unmatched transform is a no-op.
     */
    private void addSchedulerTasks() {
        transformTemplate.transform("reactor.core.scheduler.SchedulerTask", SchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.WorkerTask", SchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.ExecutorScheduler$ExecutorPlainRunnable", SchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.ExecutorScheduler$ExecutorTrackedRunnable", SchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.BoundedElasticThreadPerTaskScheduler$SchedulerTask", SchedulerTaskTransform.class);
    }

    /**
     * Periodic scheduler tasks must not carry the trace that happened to schedule them for their
     * whole lifetime. They are therefore excluded by default and, when explicitly enabled, each
     * execution is recorded as a new independent transaction.
     */
    private void addPeriodicSchedulerTasks() {
        transformTemplate.transform("reactor.core.scheduler.PeriodicSchedulerTask", PeriodicSchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.PeriodicWorkerTask", PeriodicSchedulerTaskTransform.class);
        transformTemplate.transform("reactor.core.scheduler.InstantPeriodicWorkerTask", PeriodicSchedulerTaskTransform.class);
    }

    private void addProcessor() {
        for (String className : PROCESSOR) {
            transformTemplate.transform(className, ProcessorTransform.class);
        }
    }

    private void addFlux() {
        transformTemplate.transform("reactor.core.publisher.FluxEmpty", FluxEmptyTransform.class);

        final Matcher internalFluxOperatorMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.InternalFluxOperator", true));
        transformTemplate.transform(internalFluxOperatorMatcher, FluxOperatorTransform.class);

        final Matcher internalConnectableFluxOperatorMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.InternalConnectableFluxOperator", true));
        transformTemplate.transform(internalConnectableFluxOperatorMatcher, FluxOperatorTransform.class);

        final Matcher connectableFluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.ConnectableFlux", true));
        transformTemplate.transform(connectableFluxMatcher, FluxTransform.class);

        final Matcher fluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Flux", true));
        transformTemplate.transform(fluxMatcher, FluxTransform.class);
    }

    private void addMono() {
        // MonoEmpty
        transformTemplate.transform("reactor.core.publisher.MonoEmpty", MonoEmptyTransform.class);
        final Matcher monoFromFluxOperatorMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.MonoFromFluxOperator", true));
        transformTemplate.transform(monoFromFluxOperatorMatcher, MonoOperatorTransform.class);

        final Matcher InternalMonoOperatorMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.InternalMonoOperator", true));
        transformTemplate.transform(InternalMonoOperatorMatcher, MonoOperatorTransform.class);

        final Matcher monoMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.Mono", true));
        transformTemplate.transform(monoMatcher, MonoTransform.class);
    }

    private void addParallelFlux() {
        transformTemplate.transform("reactor.core.publisher.ParallelFlux", ParallelFluxMethodTransform.class);

        final Matcher parallelFluxMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new SuperClassInternalNameMatcherOperand("reactor.core.publisher.ParallelFlux", true));
        transformTemplate.transform(parallelFluxMatcher, ParallelFluxTransform.class);
    }

    private void addTimeout() {
        transformTemplate.transform("reactor.core.publisher.FluxTimeout$TimeoutMainSubscriber", TimeoutMainSubscriberTransform.class);
    }

    private void addRetry() {
        transformTemplate.transform("reactor.core.publisher.FluxRetry$RetrySubscriber", RetrySubscriberTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxRetryWhen$RetryWhenMainSubscriber", RetrySubscriberTransform.class);
    }

    private void addOnError() {
        transformTemplate.transform("reactor.core.publisher.FluxOnErrorResume$ResumeSubscriber", OnErrorSubscriberTransform.class);
        transformTemplate.transform("reactor.core.publisher.FluxOnErrorReturn$ReturnSubscriber", OnErrorSubscriberTransform.class);
    }

    private void addCoreSubscriber() {
        final Matcher coreSubscriberMatcher = Matchers.newPackageBasedMatcher("reactor.core.publisher", new InterfaceInternalNameMatcherOperand("reactor.core.CoreSubscriber", true));
        transformTemplate.transform(coreSubscriberMatcher, CoreSubscriberTransform.class);
    }

    public static class FluxMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            return transformFluxMethods(instrumentor, loader, className, classfileBuffer, FluxAndMonoPublishOnInterceptor.class);
        }
    }

    public static class FluxPublishOnSeamMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            return transformFluxMethods(instrumentor, loader, className, classfileBuffer, WrappingFluxAndMonoPublishOnInterceptor.class);
        }
    }

    private static byte[] transformFluxMethods(Instrumentor instrumentor, ClassLoader loader, String className, byte[] classfileBuffer,
                                               Class<? extends Interceptor> publishOnInterceptor) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

        final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "org.reactivestreams.Subscriber");
        if (subscribeMethod != null) {
            subscribeMethod.addInterceptor(FluxAndMonoSubscribeMethodInterceptor.class);
        }
        final InstrumentMethod publishOnMethod = target.getDeclaredMethod("publishOn", "reactor.core.scheduler.Scheduler", "boolean", "int", "int");
        if (publishOnMethod != null) {
            publishOnMethod.addInterceptor(publishOnInterceptor);
        }
        final InstrumentMethod subscribeOnMethod = target.getDeclaredMethod("subscribeOn", "reactor.core.scheduler.Scheduler", "boolean");
        if (subscribeOnMethod != null) {
            subscribeOnMethod.addInterceptor(FluxAndMonoSubscribeOnInterceptor.class);
        }
        final InstrumentMethod intervalMethod = target.getDeclaredMethod("interval", "java.time.Duration", "java.time.Duration", "reactor.core.scheduler.Scheduler");
        if (intervalMethod != null) {
            intervalMethod.addInterceptor(FluxAndMonoIntervalInterceptor.class);
        }

        return target.toBytecode();
    }

    public static class FluxTransform implements TransformCallback {

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

    public static class FluxOperatorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
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
            // TODO connectable

            return target.toBytecode();
        }
    }

    public static class FluxEmptyTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            return target.toBytecode();
        }
    }

    public static class RunnableSubscriptionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(RunnableSubscriptionConstructorInterceptor.class);
                }
            }
            final InstrumentMethod runMethod = target.getDeclaredMethod("run");
            if (runMethod != null) {
                runMethod.addInterceptor(RunnableSubscriptionInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class SchedulerTaskTransform implements TransformCallback {
        // ASM ACC_SYNTHETIC: skips the bridge call():Object the compiler adds next to call():Void.
        private static final int ACC_SYNTHETIC = 0x1000;

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(SchedulerTaskConstructorInterceptor.class, va(ReactorConstants.REACTOR));
                }
            }
            // the execution entry point differs per version and class (run() delegating to call(),
            // call() only, run() only) - weave both; the async trace scope activates only once.
            for (InstrumentMethod method : target.getDeclaredMethods()) {
                final String name = method.getName();
                if (!"run".equals(name) && !"call".equals(name)) {
                    continue;
                }
                if (ArrayUtils.hasLength(method.getParameterTypes())) {
                    continue;
                }
                if ((method.getModifiers() & ACC_SYNTHETIC) != 0) {
                    continue;
                }
                method.addInterceptor(SchedulerTaskRunInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class PeriodicSchedulerTaskTransform implements TransformCallback {
        // ASM ACC_SYNTHETIC: skips the bridge call():Object the compiler adds next to call():Void.
        private static final int ACC_SYNTHETIC = 0x1000;

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // No injected AsyncContext field and no constructor capture. A periodic task may live
            // forever; retaining the scheduling trace would make every tick a continuation of one
            // stale request and keep that request reachable until cancellation.
            for (InstrumentMethod method : target.getDeclaredMethods()) {
                final String name = method.getName();
                if (!"run".equals(name) && !"call".equals(name)) {
                    continue;
                }
                if (ArrayUtils.hasLength(method.getParameterTypes())) {
                    continue;
                }
                if ((method.getModifiers() & ACC_SYNTHETIC) != 0) {
                    continue;
                }
                // Reactor versions differ between run(), call(), and run()->call(). The shared
                // boundary scope makes a delegating pair one transaction per scheduler tick.
                method.addScopedInterceptor(PeriodicSchedulerTaskRunInterceptor.class,
                        va(ReactorConstants.REACTOR_SCHEDULER),
                        PERIODIC_SCHEDULER_TASK_SCOPE,
                        ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    public static class MonoMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            return transformMonoMethods(instrumentor, loader, className, classfileBuffer, FluxAndMonoPublishOnInterceptor.class);
        }
    }

    public static class MonoPublishOnSeamMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            return transformMonoMethods(instrumentor, loader, className, classfileBuffer, WrappingFluxAndMonoPublishOnInterceptor.class);
        }
    }

    private static byte[] transformMonoMethods(Instrumentor instrumentor, ClassLoader loader, String className, byte[] classfileBuffer,
                                               Class<? extends Interceptor> publishOnInterceptor) throws InstrumentException {
        final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

        final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "org.reactivestreams.Subscriber");
        if (subscribeMethod != null) {
            subscribeMethod.addInterceptor(FluxAndMonoSubscribeMethodInterceptor.class);
        }
        final InstrumentMethod publishOnMethod = target.getDeclaredMethod("publishOn", "reactor.core.scheduler.Scheduler");
        if (publishOnMethod != null) {
            publishOnMethod.addInterceptor(publishOnInterceptor);
        }
        // Keep the pre-existing Mono.subscribeOn wiring unchanged in this PoC so the seam effect
        // is isolated to publishOn.
        final InstrumentMethod subscribeOnMethod = target.getDeclaredMethod("subscribeOn", "reactor.core.scheduler.Scheduler");
        if (subscribeOnMethod != null) {
            subscribeOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
        }
        final InstrumentMethod delayMethod = target.getDeclaredMethod("delay", "java.time.Duration", "reactor.core.scheduler.Scheduler");
        if (delayMethod != null) {
            delayMethod.addInterceptor(FluxAndMonoDelayInterceptor.class);
        }
        final InstrumentMethod delayElementMethod = target.getDeclaredMethod("delayElement", "java.time.Duration", "reactor.core.scheduler.Scheduler");
        if (delayElementMethod != null) {
            delayElementMethod.addInterceptor(FluxAndMonoDelayInterceptor.class);
        }

        return target.toBytecode();
    }

    public static class MonoTransform implements TransformCallback {
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

    public static class MonoOperatorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
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
            // TODO connectable

            return target.toBytecode();
        }
    }

    public static class MonoEmptyTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            return target.toBytecode();
        }
    }

    public static class ParallelFluxMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod runOnMethod = target.getDeclaredMethod("runOn", "reactor.core.scheduler.Scheduler", "int");
            if (runOnMethod != null) {
                runOnMethod.addInterceptor(FluxAndMonoPublishOnInterceptor.class);
            }
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
                subscribeMethod.addInterceptor(ParallelFluxSubscribeInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ProcessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "reactor.core.CoreSubscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class OnErrorSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

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
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(ReactorConstants.REACTOR));
            }
            final InstrumentMethod onErrorMethod = target.getDeclaredMethod("onError", "java.lang.Throwable");
            if (onErrorMethod != null) {
                onErrorMethod.addInterceptor(OnErrorSubscriberInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class CoreSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

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
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(ReactorConstants.REACTOR));
            }
            // reactor.core.publisher.FluxPublishOn$PublishOnConditionalSubscriber
            // reactor.core.publisher.FluxPublishOn$PublishOnSubscriber
            final InstrumentMethod runMethod = target.getDeclaredMethod("run");
            if (runMethod != null) {
                runMethod.addInterceptor(CoreSubscriberRunInterceptor.class, va(ReactorConstants.REACTOR));
            }

            return target.toBytecode();
        }
    }

    public static class TimeoutMainSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addGetter(TimeoutDescriptionGetter.class, "timeoutDescription");

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
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(ReactorConstants.REACTOR));
            }

            final InstrumentMethod doTimeoutMethod = target.getDeclaredMethod("handleTimeout");
            if (doTimeoutMethod != null) {
                doTimeoutMethod.addInterceptor(TimeoutMainSubscriberDoTimeoutInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class RetrySubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(RetrySubscriberSeedInterceptor.class, va(ReactorConstants.REACTOR));
                }
            }

            final InstrumentMethod onSubscribeMethod = target.getDeclaredMethod("onSubscribe", "org.reactivestreams.Subscription");
            if (onSubscribeMethod != null) {
                onSubscribeMethod.addInterceptor(CoreSubscriberOnSubscribeInterceptor.class);
            }
            final InstrumentMethod onNextMethod = target.getDeclaredMethod("onNext", "java.lang.Object");
            if (onNextMethod != null) {
                onNextMethod.addInterceptor(CoreSubscriberOnNextInterceptor.class, va(ReactorConstants.REACTOR));
            }

            // FluxRetry uses resubscribe() in every supported Reactor line. RetryWhen uses the
            // zero-argument form in 3.1 and resubscribe(Object) from 3.2 onward.
            final InstrumentMethod resubscribeMethod = target.getDeclaredMethod("resubscribe");
            if (resubscribeMethod != null) {
                resubscribeMethod.addInterceptor(RetrySubscriberResubscribeInterceptor.class);
            }
            final InstrumentMethod resubscribeWithTriggerMethod = target.getDeclaredMethod("resubscribe", "java.lang.Object");
            if (resubscribeWithTriggerMethod != null) {
                resubscribeWithTriggerMethod.addInterceptor(RetrySubscriberResubscribeInterceptor.class);
            }

            final InstrumentMethod whenErrorMethod = target.getDeclaredMethod("whenError", "java.lang.Throwable");
            if (whenErrorMethod != null) {
                whenErrorMethod.addInterceptor(RetryWhenMainSubscriberInterceptor.class);
            }

            return target.toBytecode();
        }
    }
}
