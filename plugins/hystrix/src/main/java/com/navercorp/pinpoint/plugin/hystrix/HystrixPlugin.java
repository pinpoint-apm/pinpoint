/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.hystrix;

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
import com.navercorp.pinpoint.plugin.hystrix.field.HystrixKeyNameAccessor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * Any Pinpoint profiler plugin must implement ProfilerPlugin interface.
 * ProfilerPlugin declares only one method {@link #setup(ProfilerPluginSetupContext)}.
 * You should implement the method to do whatever you need to setup your plugin with the passed ProfilerPluginSetupContext object.
 *
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public class HystrixPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        HystrixPluginConfig config = new HystrixPluginConfig(context.getConfig());
        if (config.isTraceHystrix()) {
            addHystrixCommandTransformers();
            addHystrixMetricsTransformers();
            addTransformersForTimeoutsInObservables();
            addHystrixContextSchedulerWorkerScheduleTransformers();
        }
    }

    private void addHystrixCommandTransformers() {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommand", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Methods
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "queue"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandInterceptor", HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.BOUNDARY);
                }
                InstrumentMethod getExecutionObservable = target.getDeclaredMethod("getExecutionObservable");
                if (getExecutionObservable != null) {
                    getExecutionObservable.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
                }
                InstrumentMethod getFallbackObservable = target.getDeclaredMethod("getFallbackObservable");
                if (getFallbackObservable != null) {
                    getFallbackObservable.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
                }
                // pre 1.4.0
                InstrumentMethod getFallbackOrThrowException = target.getDeclaredMethod(
                        "getFallbackOrThrowException",
                        "com.netflix.hystrix.HystrixEventType",
                        "com.netflix.hystrix.exception.HystrixRuntimeException$FailureType",
                        "java.lang.String",
                        "java.lang.Exception");
                if (getFallbackOrThrowException != null) {
                    getFallbackOrThrowException.addScopedInterceptor(
                            "com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor",
                            HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE,
                            ExecutionPolicy.ALWAYS);
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.netflix.hystrix.HystrixObservableCommand", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Methods
                InstrumentMethod getExecutionObservable = target.getDeclaredMethod("getExecutionObservable");
                if (getExecutionObservable != null) {
                    getExecutionObservable.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
                }
                InstrumentMethod getFallbackObservable = target.getDeclaredMethod("getFallbackObservable");
                if (getFallbackObservable != null) {
                    getFallbackObservable.addScopedInterceptor(BasicMethodInterceptor.class.getName(), va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.netflix.hystrix.AbstractCommand", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("observe", "toObservable"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandInterceptor", HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE);
                }
                // 1.5.3+
                InstrumentMethod getFallbackOrThrowException = target.getDeclaredMethod(
                        "getFallbackOrThrowException",
                        "com.netflix.hystrix.AbstractCommand",
                        "com.netflix.hystrix.HystrixEventType",
                        "com.netflix.hystrix.exception.HystrixRuntimeException$FailureType",
                        "java.lang.String",
                        "java.lang.Exception");
                if (getFallbackOrThrowException != null) {
                    getFallbackOrThrowException.addScopedInterceptor(
                            "com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandGetFallbackOrThrowExceptionArgs5Interceptor",
                            HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE,
                            ExecutionPolicy.ALWAYS);
                } else {
                    // pre 1.5.3
                    getFallbackOrThrowException = target.getDeclaredMethod(
                            "getFallbackOrThrowException",
                            "com.netflix.hystrix.HystrixEventType",
                            "com.netflix.hystrix.exception.HystrixRuntimeException$FailureType",
                            "java.lang.String",
                            "java.lang.Exception");
                    if (getFallbackOrThrowException != null) {
                        getFallbackOrThrowException.addScopedInterceptor(
                                "com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor",
                                HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE,
                                ExecutionPolicy.ALWAYS);
                    }
                }
                return target.toBytecode();
            }
        });
    }

    private void addHystrixMetricsTransformers() {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommandMetrics", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor(
                        "com.netflix.hystrix.HystrixCommandKey",
                        "com.netflix.hystrix.HystrixCommandGroupKey",
                        "com.netflix.hystrix.HystrixThreadPoolKey",
                        "com.netflix.hystrix.HystrixCommandProperties",
                        "com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier");
                if (constructor == null) {
                    constructor = target.getConstructor(
                            "com.netflix.hystrix.HystrixCommandKey",
                            "com.netflix.hystrix.HystrixCommandGroupKey",
                            "com.netflix.hystrix.HystrixCommandProperties",
                            "com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier");
                }
                if (constructor == null) {
                    return null;
                }
                constructor.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixCommandMetricsConstructInterceptor");
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.netflix.hystrix.HystrixCircuitBreaker$HystrixCircuitBreakerImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor(
                        "com.netflix.hystrix.HystrixCommandKey",
                        "com.netflix.hystrix.HystrixCommandGroupKey",
                        "com.netflix.hystrix.HystrixCommandProperties",
                        "com.netflix.hystrix.HystrixCommandMetrics");
                if (constructor == null) {
                    return null;
                }
                constructor.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixCircuitBreakerConstructInterceptor");
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.netflix.hystrix.HystrixThreadPoolMetrics", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("com.netflix.hystrix.HystrixThreadPoolKey", "java.util.concurrent.ThreadPoolExecutor", "com.netflix.hystrix.HystrixThreadPoolProperties");
                if (constructor == null) {
                    return null;
                }
                constructor.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixThreadPoolMetricsConstructInterceptor");
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.netflix.hystrix.HystrixCollapserMetrics", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("com.netflix.hystrix.HystrixCollapserKey", "com.netflix.hystrix.HystrixCollapserProperties");
                if (constructor == null) {
                    return null;
                }
                constructor.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixCollapserMetricsConstructInterceptor");
                return target.toBytecode();
            }
        });

        TransformCallback hystrixKeyTransformCallback = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod constructor = target.getConstructor("java.lang.String");
                if (constructor == null) {
                    return null;
                }
                target.addField(HystrixKeyNameAccessor.class.getName());
                constructor.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixKeyConstructInterceptor");
                return target.toBytecode();
            }
        };
        transformTemplate.transform("com.netflix.hystrix.HystrixCommandKey$Factory$HystrixCommandKeyDefault", hystrixKeyTransformCallback);
        transformTemplate.transform("com.netflix.hystrix.HystrixCommandGroupKey$Factory$HystrixCommandGroupDefault", hystrixKeyTransformCallback);
        transformTemplate.transform("com.netflix.hystrix.HystrixThreadPoolKey$Factory$HystrixThreadPoolKeyDefault", hystrixKeyTransformCallback);
        transformTemplate.transform("com.netflix.hystrix.HystrixCollapserKey$Factory$HystrixCollapserKeyDefault", hystrixKeyTransformCallback);
    }

    private void addTransformersForTimeoutsInObservables() {
        transformTemplate.transform("com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod call = target.getDeclaredMethod("call", "rx.Subscriber");
                if (call == null) {
                    return null;
                }
                target.addField(AsyncContextAccessor.class.getName());
                call.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableTimeoutOperatorCallInterceptor");

                for (InstrumentClass nested : target.getNestedClasses(ClassFilters.chain(ClassFilters.interfaze("com.netflix.hystrix.util.HystrixTimer$TimerListener"), ClassFilters.enclosingMethod("call", "rx.Subscriber")))) {
                    instrumentor.transform(classLoader, nested.getName(), new TransformCallback() {
                        @Override
                        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                            // 1.5.12+
                            InstrumentMethod constructor = target.getConstructor(
                                    // Enclosing instance
                                    "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator",
                                    // References to enclosing method's final objects
                                    "rx.subscriptions.CompositeSubscription", "com.netflix.hystrix.strategy.concurrency.HystrixRequestContext", "rx.Subscriber");
                            if (constructor == null) {
                                // pre 1.5.12
                                constructor = target.getConstructor(
                                        // Enclosing instance
                                        "com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator",
                                        // References to enclosing method's final objects
                                        "rx.subscriptions.CompositeSubscription", "com.netflix.hystrix.strategy.concurrency.HystrixContextRunnable");
                            }
                            InstrumentMethod tick = target.getDeclaredMethod("tick");
                            if (constructor == null || tick == null) {
                                return null;
                            }
                            target.addField("com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceAccessor");
                            constructor.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableTimeoutListenerConstructorInterceptor");
                            tick.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableTimeoutListenerTickInterceptor");
                            return target.toBytecode();
                        }
                    });
                }

                return target.toBytecode();
            }
        });
    }

    private void addHystrixContextSchedulerWorkerScheduleTransformers() {
        SchedulerWorkerTransformCallback callback = SchedulerWorkerTransformCallback.createFor(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE);
        transformTemplate.transform("com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker", callback);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
