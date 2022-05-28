/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.sdk;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.sdk.interceptor.AsyncEntryInterceptor;
import com.navercorp.pinpoint.plugin.sdk.interceptor.CommandInterceptor;
import com.navercorp.pinpoint.plugin.sdk.interceptor.ExecutorExecuteInterceptor;


import java.security.ProtectionDomain;

public class AgentSdkAsyncPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private TransformTemplate transformTemplate;


    @Override
    public void setup(ProfilerPluginSetupContext context) {
        AgentSdkAsyncConfig agentSdkAsyncConfig = new AgentSdkAsyncConfig(context.getConfig());
        if (!agentSdkAsyncConfig.isEnable()) {
            logger.info("AgentSdkAsyncConfig is disable");
            return;
        }

        logger.info("AgentSdkAsyncConfig config={}", agentSdkAsyncConfig);

        String sdkPackage = "com.navercorp.pinpoint.sdk.v1.concurrent";

        addTraceRunnableInterceptorTask(sdkPackage + ".TraceRunnable");
        addTraceCallableInterceptorTask(sdkPackage + ".TraceCallable");
        addTraceForkJoinTaskInterceptorTask(sdkPackage + ".TraceForkJoinTask");

        addTraceExecutor(sdkPackage + ".TraceExecutorService");
        addTraceExecutor(sdkPackage + ".TraceScheduledExecutorService");
        addTraceExecutor(sdkPackage + ".TraceExecutor");

//        addAsyncTaskExecutor(sdkPackage + ".TraceForkJoinPool");

    }


    private void addTraceRunnableInterceptorTask(final String className) {
        transformTemplate.transform(className, TraceRunnableCallback.class);
    }

    public static class TraceRunnableCallback implements TransformCallback {

        public TraceRunnableCallback() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            final InstrumentMethod callMethod = target.getDeclaredMethod("run");
            if (callMethod != null) {
                callMethod.addInterceptor(CommandInterceptor.class);
            }
            InstrumentMethod asyncEntry = target.getDeclaredMethod("asyncEntry", "java.lang.Runnable");
            if (asyncEntry != null) {
                asyncEntry.addInterceptor(AsyncEntryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addTraceCallableInterceptorTask(final String className) {
        transformTemplate.transform(className, TraceCallableCallback.class);
    }

    public static class TraceCallableCallback implements TransformCallback {

        public TraceCallableCallback() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            final InstrumentMethod callMethod = target.getDeclaredMethod("call");
            if (callMethod != null) {
                callMethod.addInterceptor(CommandInterceptor.class);
            }
            InstrumentMethod asyncEntry = target.getDeclaredMethod("asyncEntry", "java.util.concurrent.Callable");
            if (asyncEntry != null) {
                asyncEntry.addInterceptor(AsyncEntryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addTraceForkJoinTaskInterceptorTask(final String className) {
        transformTemplate.transform(className, TraceForkJoinTaskCallback.class);
    }

    public static class TraceForkJoinTaskCallback implements TransformCallback {

        public TraceForkJoinTaskCallback() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            final InstrumentMethod execMethod = target.getDeclaredMethod("exec");
            if (execMethod != null) {
                execMethod.addInterceptor(CommandInterceptor.class);
            }
            InstrumentMethod asyncEntry = target.getDeclaredMethod("asyncEntry", "java.util.concurrent.ForkJoinTask");
            if (asyncEntry != null) {
                asyncEntry.addInterceptor(AsyncEntryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addTraceExecutor(final String className) {
        transformTemplate.transform(className, ExecutorExecuteTransformCallback.class);
    }

    public static class ExecutorExecuteTransformCallback implements TransformCallback {
//        private final PLogger logger = PLoggerFactory.getLogger(getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("execute"))) {
                m.addScopedInterceptor(ExecutorExecuteInterceptor.class, AgentSdkAsyncConstants.AGENT_SDK_ASYNC_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("submit"))) {
                m.addScopedInterceptor(ExecutorExecuteInterceptor.class, AgentSdkAsyncConstants.AGENT_SDK_ASYNC_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("schedule"))) {
                m.addScopedInterceptor(ExecutorExecuteInterceptor.class, AgentSdkAsyncConstants.AGENT_SDK_ASYNC_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("scheduleAtFixedRate"))) {
                m.addScopedInterceptor(ExecutorExecuteInterceptor.class, AgentSdkAsyncConstants.AGENT_SDK_ASYNC_SCOPE);
            }
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("scheduleWithFixedDelay"))) {
                m.addScopedInterceptor(ExecutorExecuteInterceptor.class, AgentSdkAsyncConstants.AGENT_SDK_ASYNC_SCOPE);
            }
//            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("invokeAll"))) {
//                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, AgentSdkAsyncConstants.THREAD_POOL_EXECUTOR_SCOPE);
//            }
//            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("invokeAny"))) {
//                m.addScopedInterceptor(ThreadPoolExecutorSubmitInterceptor.class, AgentSdkAsyncConstants.THREAD_POOL_EXECUTOR_SCOPE);
//            }
            return target.toBytecode();
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
