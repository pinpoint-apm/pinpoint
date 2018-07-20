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
        if (config.isTraceRxJava()) {
            addObservableTransformers();
            addScheduledActionTransformers();
            addSchedulerWorkerTransformers();
        }
    }

    private static class ObservableTransformCallback implements TransformCallback {

        interface NestedScheduledActionTransformer {
            void transformNestedScheduledActions(InstrumentClass target, Instrumentor instrumentor, ClassLoader classLoader);
        }

        private static final NestedScheduledActionTransformer NONE = null;

        private final NestedScheduledActionTransformer nestedScheduledActionTransformer;
        private final String[] traceMethods;

        private ObservableTransformCallback(String... traceMethods) {
            this(NONE, traceMethods);
        }

        private ObservableTransformCallback(NestedScheduledActionTransformer nestedScheduledActionTransformer, String... traceMethods) {
            this.nestedScheduledActionTransformer = nestedScheduledActionTransformer;
            this.traceMethods = traceMethods;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            if (nestedScheduledActionTransformer != NONE) {
                nestedScheduledActionTransformer.transformNestedScheduledActions(target, instrumentor, classLoader);
            }
            for (InstrumentMethod subscribe : target.getDeclaredMethods(MethodFilters.name("subscribe"))) {
                subscribe.addScopedInterceptor("com.navercorp.pinpoint.plugin.rxjava.interceptor.ObservableSubscribeInterceptor", RxJavaPluginConstants.RX_JAVA_SUBSCRIBE_SCOPE, ExecutionPolicy.BOUNDARY);
            }
            for (InstrumentMethod traceMethod : target.getDeclaredMethods(MethodFilters.name(this.traceMethods))) {
                traceMethod.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(RxJavaPluginConstants.RX_JAVA), RxJavaPluginConstants.RX_JAVA_OBSERVABLE_SCOPE, ExecutionPolicy.BOUNDARY);
            }
            return target.toBytecode();
        }
    }

    private void addObservableTransformers() {
        // Observable
        transformTemplate.transform("rx.Observable", new ObservableTransformCallback("toBlocking", "publish", "groupBy"));
        transformTemplate.transform("rx.observables.BlockingObservable", new ObservableTransformCallback(
                "first", "firstOrDefault", "mostRecent",
                "last", "lastOrDefault", "latest",
                "single", "singleOrDefault", "next",
                "forEach", "getIterator", "toFuture", "toIterable", "blockForSingle"));
        transformTemplate.transform("rx.observables.ConnectableObservable", new ObservableTransformCallback("connect", "autoConnect", "refCount"));

        // Single
        ObservableTransformCallback.NestedScheduledActionTransformer rxSingleNestedScheduledActionTransformer = new ObservableTransformCallback.NestedScheduledActionTransformer() {
            @Override
            public void transformNestedScheduledActions(InstrumentClass target, Instrumentor instrumentor, ClassLoader classLoader) {
                for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Single$OnSubscribe")))) {
                    for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.SingleSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                        instrumentor.transform(classLoader, nestedClass2.getName(), scheduledActionTransformCallback);
                    }
                }
            }
        };
        transformTemplate.transform("rx.Single", new ObservableTransformCallback(rxSingleNestedScheduledActionTransformer, "toBlocking"));
        transformTemplate.transform("rx.singles.BlockingSingle", new ObservableTransformCallback("value", "toFuture"));

        // Completable
        ObservableTransformCallback.NestedScheduledActionTransformer rxCompletableNestedScheduledActionTransformer = new ObservableTransformCallback.NestedScheduledActionTransformer() {
            @Override
            public void transformNestedScheduledActions(InstrumentClass target, Instrumentor instrumentor, ClassLoader classLoader) {
                // [1.1.1,1.1.9)
                for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Completable$CompletableOnSubscribe")))) {
                    for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.Completable$CompletableSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                        instrumentor.transform(classLoader, nestedClass2.getName(), scheduledActionTransformCallback);
                    }
                }
                // [1.1.9]
                for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Completable$CompletableOnSubscribe")))) {
                    for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.CompletableSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                        instrumentor.transform(classLoader, nestedClass2.getName(), scheduledActionTransformCallback);
                    }
                }
                // [1.1.10,)
                for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("subscribeOn", "rx.Scheduler"), ClassFilters.interfaze("rx.Completable$OnSubscribe")))) {
                    for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.CompletableSubscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                        instrumentor.transform(classLoader, nestedClass2.getName(), scheduledActionTransformCallback);
                    }
                }
            }
        };
        transformTemplate.transform("rx.Completable", new ObservableTransformCallback(rxCompletableNestedScheduledActionTransformer, "await", "get"));
    }

    private final TransformCallback scheduledActionTransformCallback = new TransformCallback() {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod call = target.getDeclaredMethod("call");
            if (call == null) {
                return null;
            }
            target.addField(AsyncContextAccessor.class.getName());
            call.addInterceptor("com.navercorp.pinpoint.plugin.rxjava.interceptor.SubscriptionTraceEnabledMethodInterceptor");
            return target.toBytecode();
        }
    };

    private void addScheduledActionTransformers() {
        // OperatorSubscribeOn
        transformTemplate.transform("rx.internal.operators.OperatorSubscribeOn", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // [1.0.0,1.1.1)
                for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.Subscriber"), ClassFilters.name("rx.internal.operators.OperatorSubscribeOn$1")))) {
                    for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("onNext", "rx.Observable"), ClassFilters.interfaze("rx.functions.Action0")))) {
                        instrumentor.transform(classLoader, nestedClass2.getName(), scheduledActionTransformCallback);
                    }
                }
                // [1.1.1,1.2.7)
                for (InstrumentClass nestedClass : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.Subscriber"), ClassFilters.interfaze("rx.functions.Action0")))) {
                    instrumentor.transform(classLoader, nestedClass.getName(), scheduledActionTransformCallback);
                }
                // [1.2.7,)
                for (InstrumentClass nestedClass : target.getNestedClasses(ClassFilters.name("rx.internal.operators.OperatorSubscribeOn$SubscribeOnSubscriber"))) {
                    instrumentor.transform(classLoader, nestedClass.getName(), scheduledActionTransformCallback);
                }
                return target.toBytecode();
            }
        });
        // ScalarSynchronousObservable
        // [1.1.1,)
        transformTemplate.transform("rx.internal.util.ScalarSynchronousObservable", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                for (InstrumentClass nestedClass1 : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("scalarScheduleOn", "rx.Scheduler"), ClassFilters.interfaze("rx.functions.Func1")))) {
                    for (InstrumentClass nestedClass2 : nestedClass1.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("call", "rx.functions.Action0"), ClassFilters.interfaze("rx.functions.Action0")))) {
                        instrumentor.transform(classLoader, nestedClass2.getName(), scheduledActionTransformCallback);
                    }
                }
                return target.toBytecode();
            }
        });
        // [1.0.8,1.1.1)
        transformTemplate.transform("rx.internal.util.ScalarSynchronousObservable$ScalarSynchronousAction", scheduledActionTransformCallback);
        // ScalarSynchronousSingle
        transformTemplate.transform("rx.internal.util.ScalarSynchronousSingleAction", scheduledActionTransformCallback);
    }

    private void addSchedulerWorkerTransformers() {
        SchedulerWorkerTransformCallback callback = SchedulerWorkerTransformCallback.createFor(RxJavaPluginConstants.RX_JAVA_INTERNAL);
        // 1.1.4+
        transformTemplate.transform("rx.internal.schedulers.EventLoopsScheduler$EventLoopWorker", callback);
        transformTemplate.transform("rx.internal.schedulers.CachedThreadScheduler$EventLoopWorker", callback);
        transformTemplate.transform("rx.internal.schedulers.ExecutorScheduler$ExecutorSchedulerWorker", callback);
        transformTemplate.transform("rx.internal.schedulers.NewThreadWorker", callback);
        // pre 1.1.4, some of the schedulers weren't in internal package
        transformTemplate.transform("rx.schedulers.EventLoopsScheduler$EventLoopWorker", callback);
        transformTemplate.transform("rx.schedulers.CachedThreadScheduler$EventLoopWorker", callback);
        transformTemplate.transform("rx.schedulers.ExecutorScheduler$ExecutorSchedulerWorker", callback);
        transformTemplate.transform("rx.schedulers.NewThreadWorker", callback);

        // TODO enable custom scheduler worker transformation?

        transformTemplate.transform("rx.internal.schedulers.EventLoopsScheduler", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod scheduleDirect = target.getDeclaredMethod("scheduleDirect", "rx.functions.Action0");
                if (scheduleDirect != null) {
                    scheduleDirect.addInterceptor("com.navercorp.pinpoint.plugin.rxjava.interceptor.EventLoopsSchedulerScheduleDirectInterceptor");
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
