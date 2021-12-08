/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.CopyAsyncContextInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.DispatchInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.ExecuteTaskInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Taejin Koo
 */
public class CoroutinesPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final CoroutinesConfig config = new CoroutinesConfig(context.getConfig());

        final String simpleClazzName = this.getClass().getSimpleName();

        if (!config.isTraceCoroutines()) {
            logger.info("{} disabled", simpleClazzName);
            return;
        }
        if (config.getIncludedNameList().isEmpty()) {
            logger.info("{} could not find any included name.", simpleClazzName);
            return;
        }

        logger.info("{} config:{}", simpleClazzName, config);

        /**
         * 1. Starts coroutine task
         * 2. Creates DispatchedContinuation
         * 3. Dispatches DispatchedContinuation
         *  L addCoroutineDispatcherTransformer()
         * 4. Creates CancellableContinuation based on DispatchedContinuation
         *  L propagateAsyncContextTransformer
         * 5. Dispatches CancellableContinuation
         * 6. Executes task(actual implementation)
         *  L addExecuteTaskTransformer
         */
        addCoroutineDispatcherTransformer();
        propagateAsyncContextTransformer();
        addExecuteTaskTransformer();
    }

    private void addCoroutineDispatcherTransformer() {
        Matcher dispatcherMatcher = Matchers.newPackageBasedMatcher("kotlinx.coroutines",
                new SuperClassInternalNameMatcherOperand("kotlinx.coroutines.CoroutineDispatcher", true));
        transformTemplate.transform(dispatcherMatcher, CoroutineDispatcherTransform.class);
    }

    private void propagateAsyncContextTransformer() {
        // For adding AsyncContextAccessor
        // > 1.4.0
        transformTemplate.transform("kotlinx.coroutines.internal.DispatchedContinuation",
                DispatchedContinuationTransform.class);
        // < 1.4.0
        transformTemplate.transform("kotlinx.coroutines.DispatchedContinuation",
                DispatchedContinuationTransform.class);


        // For adding AsyncContextAccessor to CancellableContinuation and propagation AsyncContext from DispatchedTask
        transformTemplate.transform("kotlinx.coroutines.CancellableContinuationImpl",
                CancellableContinuationTransform.class);
    }

    private void addExecuteTaskTransformer() {
        // If below tracing makes problems, you could consider tracing the executeTask of CoroutineScheduler$Worker.
        transformTemplate.transform("kotlinx.coroutines.scheduling.CoroutineScheduler", WorkerTransform.class);
    }

    public static class CoroutineDispatcherTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final CoroutinesConfig config = new CoroutinesConfig(instrumentor.getProfilerConfig());


            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod dispatchMethod = target.getDeclaredMethod("dispatch", "kotlin.coroutines.CoroutineContext", "java.lang.Runnable");
            if (dispatchMethod != null) {
                dispatchMethod.addScopedInterceptor(DispatchInterceptor.class, va(config), CoroutinesConstants.SCOPE);
            }

            return target.toBytecode();
        }

    }

    public static class DispatchedContinuationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }

    }

    public static class WorkerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            InstrumentMethod runSafelyMethod = target.getDeclaredMethod("runSafely", "kotlinx.coroutines.scheduling.Task");
            if (runSafelyMethod != null) {
                runSafelyMethod.addInterceptor(ExecuteTaskInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class CancellableContinuationTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            List<InstrumentMethod> declaredConstructors = target.getDeclaredConstructors();
            for (InstrumentMethod declaredConstructor : declaredConstructors) {
                String[] parameterTypes = declaredConstructor.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes) && "kotlin.coroutines.Continuation".equals(parameterTypes[0])) {
                    declaredConstructor.addInterceptor(CopyAsyncContextInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
