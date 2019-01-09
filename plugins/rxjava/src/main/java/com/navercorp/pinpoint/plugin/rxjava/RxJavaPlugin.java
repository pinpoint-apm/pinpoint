/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rxjava;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.rxjava.transformer.SchedulerWorkerTransformCallback;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.rxjava.interceptor.EventLoopsSchedulerScheduleDirectInterceptor;
import com.navercorp.pinpoint.plugin.rxjava.interceptor.ObservableSubscribeInterceptor;
import com.navercorp.pinpoint.plugin.rxjava.interceptor.SubscriptionTraceEnabledMethodInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author HyunGil Jeong
 */
public class RxJavaPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        RxJavaPluginConfig config = new RxJavaPluginConfig(context.getConfig());

        if (!config.isTraceRxJava()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        addObservableTransformers();
        addScheduledActionTransformers();
        addSchedulerWorkerTransformers();
    }

    public static class ObservableTransformCallback implements TransformCallback {

        private final String[] traceMethods;

        public ObservableTransformCallback(String... traceMethods) {
            this.traceMethods = traceMethods;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            transformNestedScheduledActions(target, instrumentor, classLoader);

            for (InstrumentMethod subscribe : target.getDeclaredMethods(MethodFilters.name("subscribe"))) {
                subscribe.addScopedInterceptor(ObservableSubscribeInterceptor.class, RxJavaPluginConstants.RX_JAVA_SUBSCRIBE_SCOPE, ExecutionPolicy.BOUNDARY);
            }
            for (InstrumentMethod traceMethod : target.getDeclaredMethods(MethodFilters.name(this.traceMethods))) {
                traceMethod.addScopedInterceptor(BasicMethodInterceptor.class, va(RxJavaPluginConstants.RX_JAVA), RxJavaPluginConstants.RX_JAVA_OBSERVABLE_SCOPE, ExecutionPolicy.BOUNDARY);
            }
            return target.toBytecode();
        }


        protected void transformNestedScheduledActions(InstrumentClass target, Instrumentor instrumentor, ClassLoader classLoader) {
            ;
        }
    }

    private void addObservableTransformers() {
        // Observable
        transform("rx.Observable", ObservableTransformCallback.class, "toBlocking", "publish", "groupBy");
        transform("rx.observables.BlockingObservable", ObservableTransformCallback.class,
                "first", "firstOrDefault", "mostRecent",
                "last", "lastOrDefault", "latest",
                "single", "singleOrDefault", "next",
                "forEach", "getIterator", "toFuture", "toIterable", "blockForSingle");
        transform("rx.observables.ConnectableObservable", ObservableTransformCallback.class, "connect", "autoConnect", "refCount");


        transform("rx.Single", RxSingleNestedScheduledActionTransformer.class, "toBlocking");
        transform("rx.singles.BlockingSingle", ObservableTransformCallback.class, "value", "toFuture");

        transform("rx.Completable", RxCompletableNestedScheduledActionTransformer.class, "await", "get");
    }

    private void transform(String className, Class<? extends TransformCallback> transformCallbackClass, String... constructorParameter) {
        transformTemplate.transform(className, transformCallbackClass, new Object[]{constructorParameter}, new Class[]{String[].class});
    }

    // Single
    public static class RxSingleNestedScheduledActionTransformer extends ObservableTransformCallback {
        public RxSingleNestedScheduledActionTransformer(String... traceMethods) {
            super(traceMethods);
        }

        @Override
        public void transformNestedScheduledActions(InstrumentClass target, Instrumentor instrumentor, ClassLoader classLoader) {
            for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Single$OnSubscribe")))) {
                for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.SingleSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass2.getName(), ScheduledActionTransform.class);
                }
            }
        }
    };

    // Completable
    public static class RxCompletableNestedScheduledActionTransformer extends ObservableTransformCallback {
        public RxCompletableNestedScheduledActionTransformer(String... traceMethods) {
            super(traceMethods);
        }

        @Override
        public void transformNestedScheduledActions(InstrumentClass target, Instrumentor instrumentor, ClassLoader classLoader) {
            // [1.1.1,1.1.9)
            for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Completable$CompletableOnSubscribe")))) {
                for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.Completable$CompletableSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass2.getName(), ScheduledActionTransform.class);
                }
            }
            // [1.1.9]
            for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Completable$CompletableOnSubscribe")))) {
                for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.CompletableSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass2.getName(), ScheduledActionTransform.class);
                }
            }
            // [1.1.10,)
            for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Completable$OnSubscribe")))) {
                for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.CompletableSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass2.getName(), ScheduledActionTransform.class);
                }
            }
        }
    };

    public static class ScheduledActionTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod call = target.getDeclaredMethod("call");
            if (call == null) {
                return null;
            }
            target.addField(AsyncContextAccessor.class);
            call.addInterceptor(SubscriptionTraceEnabledMethodInterceptor.class);
            return target.toBytecode();
        }
    };

    private void addScheduledActionTransformers() {
        // OperatorSubscribeOn
        transformTemplate.transform("rx.internal.operators.OperatorSubscribeOn", OperatorSubscribeOnTransform.class);
        // ScalarSynchronousObservable
        // [1.1.1,)
        transformTemplate.transform("rx.internal.util.ScalarSynchronousObservable", ScalarSynchronousObservable.class);
        // [1.0.8,1.1.1)
        transformTemplate.transform("rx.internal.util.ScalarSynchronousObservable$ScalarSynchronousAction", ScheduledActionTransform.class);
        // ScalarSynchronousSingle
        transformTemplate.transform("rx.internal.util.ScalarSynchronousSingleAction", ScheduledActionTransform.class);
    }

    public static class OperatorSubscribeOnTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // [1.0.0,1.1.1)
            for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.Subscriber"), ClassFilters.name("rx.internal.operators.OperatorSubscribeOn$1")))) {
                for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("onNext", "rx.Observable"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass2.getName(), ScheduledActionTransform.class);
                }
            }
            // [1.1.1,1.2.7)
            for (InstrumentClass nestedClass : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.Subscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                instrumentor.transform(classLoader, nestedClass.getName(), ScheduledActionTransform.class);
            }
            // [1.2.7,)
            for (InstrumentClass nestedClass : target.getNestedClasses(ClassFilters.name("rx.internal.operators.OperatorSubscribeOn$SubscribeOnSubscriber"))) {
                instrumentor.transform(classLoader, nestedClass.getName(), ScheduledActionTransform.class);
            }
            return target.toBytecode();
        }
    }

    public static class ScalarSynchronousObservable implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("scalarScheduleOn", "rx.Scheduler"), ClassFilters.interfaze("rx.functions.Func1")))) {
                for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.functions.Action0"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass2.getName(), ScheduledActionTransform.class);
                }
            }
            return target.toBytecode();
        }
    }

    private void addSchedulerWorkerTransformers() {
        // 1.1.4+
        transform("rx.internal.schedulers.EventLoopsScheduler$EventLoopWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        transform("rx.internal.schedulers.CachedThreadScheduler$EventLoopWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        transform("rx.internal.schedulers.ExecutorScheduler$ExecutorSchedulerWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        transform("rx.internal.schedulers.NewThreadWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        // pre 1.1.4, some of the schedulers weren't in internal package
        transform("rx.schedulers.EventLoopsScheduler$EventLoopWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        transform("rx.schedulers.CachedThreadScheduler$EventLoopWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        transform("rx.schedulers.ExecutorScheduler$ExecutorSchedulerWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);
        transform("rx.schedulers.NewThreadWorker", SchedulerWorkerTransformCallback.class, RxJavaPluginConstants.RX_JAVA_INTERNAL);

        // TODO enable custom scheduler worker transformation?

        transformTemplate.transform("rx.internal.schedulers.EventLoopsScheduler", EventLoopsScheduler.class);
    }

    private void transform(String className, Class<? extends TransformCallback> transformCallbackClass, ServiceType serviceType) {
        transformTemplate.transform(className, transformCallbackClass, new Object[]{serviceType}, new Class[]{ServiceType.class});
    }

    public static class EventLoopsScheduler implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod scheduleDirect = target.getDeclaredMethod("scheduleDirect", "rx.functions.Action0");
            if (scheduleDirect != null) {
                scheduleDirect.addInterceptor(EventLoopsSchedulerScheduleDirectInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
