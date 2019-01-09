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
import com.navercorp.pinpoint.plugin.hystrix.field.EnclosingInstanceAccessor;
import com.navercorp.pinpoint.plugin.hystrix.field.HystrixKeyNameAccessor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandGetFallbackOrThrowExceptionArgs5Interceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableTimeoutListenerConstructorInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableTimeoutListenerTickInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixObservableTimeoutOperatorCallInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixCircuitBreakerConstructInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixCollapserMetricsConstructInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixCommandMetricsConstructInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixKeyConstructInterceptor;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics.HystrixThreadPoolMetricsConstructInterceptor;

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
        if (!config.isTraceHystrix()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        addHystrixCommandTransformers();
        addHystrixMetricsTransformers();
        addTransformersForTimeoutsInObservables();
        addHystrixContextSchedulerWorkerScheduleTransformers();
    }

    private void addHystrixCommandTransformers() {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommand", HystrixCommandTransform.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixObservableCommand", HystrixObservableCommandTransform.class);
        transformTemplate.transform("com.netflix.hystrix.AbstractCommand", AbstractCommandTransform.class);
    }

    public static class HystrixCommandTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Methods
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("execute", "queue"))) {
                method.addScopedInterceptor(HystrixCommandInterceptor.class, HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.BOUNDARY);
            }
            InstrumentMethod getExecutionObservable = target.getDeclaredMethod("getExecutionObservable");
            if (getExecutionObservable != null) {
                getExecutionObservable.addScopedInterceptor(BasicMethodInterceptor.class, va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
            }
            InstrumentMethod getFallbackObservable = target.getDeclaredMethod("getFallbackObservable");
            if (getFallbackObservable != null) {
                getFallbackObservable.addScopedInterceptor(BasicMethodInterceptor.class, va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
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
                        HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor.class,
                        HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE,
                        ExecutionPolicy.ALWAYS);
            }
            return target.toBytecode();
        }
    }

    public static class HystrixObservableCommandTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Methods
            InstrumentMethod getExecutionObservable = target.getDeclaredMethod("getExecutionObservable");
            if (getExecutionObservable != null) {
                getExecutionObservable.addScopedInterceptor(BasicMethodInterceptor.class, va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
            }
            InstrumentMethod getFallbackObservable = target.getDeclaredMethod("getFallbackObservable");
            if (getFallbackObservable != null) {
                getFallbackObservable.addScopedInterceptor(BasicMethodInterceptor.class, va(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE), HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE, ExecutionPolicy.ALWAYS);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractCommandTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("observe", "toObservable"))) {
                method.addScopedInterceptor(HystrixCommandInterceptor.class, HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE);
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
                        HystrixCommandGetFallbackOrThrowExceptionArgs5Interceptor.class,
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
                            HystrixCommandGetFallbackOrThrowExceptionArgs4Interceptor.class,
                            HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE,
                            ExecutionPolicy.ALWAYS);
                }
            }
            return target.toBytecode();
        }
    }

    private void addHystrixMetricsTransformers() {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommandMetrics", HystrixCommandMetricsTransformer.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixCircuitBreaker$HystrixCircuitBreakerImpl", HystrixCircuitBreakerImplTransformer.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixThreadPoolMetrics", HystrixThreadPoolMetricsTransform.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixCollapserMetrics", HystrixCollapserMetricsTransformer.class);

        transformTemplate.transform("com.netflix.hystrix.HystrixCommandKey$Factory$HystrixCommandKeyDefault", HystrixKeyTransform.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixCommandGroupKey$Factory$HystrixCommandGroupDefault", HystrixKeyTransform.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixThreadPoolKey$Factory$HystrixThreadPoolKeyDefault", HystrixKeyTransform.class);
        transformTemplate.transform("com.netflix.hystrix.HystrixCollapserKey$Factory$HystrixCollapserKeyDefault", HystrixKeyTransform.class);
    }

    public static class HystrixCommandMetricsTransformer implements TransformCallback {
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
            constructor.addInterceptor(HystrixCommandMetricsConstructInterceptor.class);
            return target.toBytecode();
        }
    };

    public static class HystrixCircuitBreakerImplTransformer implements TransformCallback {
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
            constructor.addInterceptor(HystrixCircuitBreakerConstructInterceptor.class);
            return target.toBytecode();
        }
    };

    public static class HystrixThreadPoolMetricsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("com.netflix.hystrix.HystrixThreadPoolKey", "java.util.concurrent.ThreadPoolExecutor", "com.netflix.hystrix.HystrixThreadPoolProperties");
            if (constructor == null) {
                return null;
            }
            constructor.addInterceptor(HystrixThreadPoolMetricsConstructInterceptor.class);
            return target.toBytecode();
        }
    };

    public static class HystrixCollapserMetricsTransformer implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("com.netflix.hystrix.HystrixCollapserKey", "com.netflix.hystrix.HystrixCollapserProperties");
            if (constructor == null) {
                return null;
            }
            constructor.addInterceptor(HystrixCollapserMetricsConstructInterceptor.class);
            return target.toBytecode();
        }
    };

    public static class HystrixKeyTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor("java.lang.String");
            if (constructor == null) {
                return null;
            }
            target.addField(HystrixKeyNameAccessor.class);
            constructor.addInterceptor(HystrixKeyConstructInterceptor.class);
            return target.toBytecode();
        }
    };

    private void addTransformersForTimeoutsInObservables() {
        transformTemplate.transform("com.netflix.hystrix.AbstractCommand$HystrixObservableTimeoutOperator", HystrixObservableTimeoutOperatorTransformer.class);
    }

    public static class HystrixObservableTimeoutOperatorTransformer implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod call = target.getDeclaredMethod("call", "rx.Subscriber");
            if (call == null) {
                return null;
            }
            target.addField(AsyncContextAccessor.class);
            call.addInterceptor(HystrixObservableTimeoutOperatorCallInterceptor.class);

            for (InstrumentClass nested : target.getNestedClasses(ClassFilters.chain(ClassFilters.interfaze("com.netflix.hystrix.util.HystrixTimer$TimerListener"), ClassFilters.enclosingMethod("call", "rx.Subscriber")))) {
                instrumentor.transform(classLoader, nested.getName(), TimerListenerTransformer.class);
            }

            return target.toBytecode();
        }
    }

    public static class TimerListenerTransformer implements TransformCallback {
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
            target.addField(EnclosingInstanceAccessor.class);
            constructor.addInterceptor(HystrixObservableTimeoutListenerConstructorInterceptor.class);
            tick.addInterceptor(HystrixObservableTimeoutListenerTickInterceptor.class);
            return target.toBytecode();
        }
    }

    private void addHystrixContextSchedulerWorkerScheduleTransformers() {
        transformTemplate.transform("com.netflix.hystrix.strategy.concurrency.HystrixContextScheduler$HystrixContextSchedulerWorker", HystrixSchedulerWorkerTransformCallback.class);
    }

    public static class HystrixSchedulerWorkerTransformCallback extends SchedulerWorkerTransformCallback {
        public HystrixSchedulerWorkerTransformCallback() {
            super(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE);
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
