/*
 * Copyright 2022 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
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
import com.navercorp.pinpoint.common.util.VarArgs;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.CancelledInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.DispatchInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.NotifyCancellingInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.ResumeWithInterceptor;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor.ScheduleResumeInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class CoroutinesPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final CoroutinesConfig config = new CoroutinesConfig(context.getConfig());

        final String simpleClazzName = this.getClass().getSimpleName();

        if (!config.isTraceCoroutines()) {
            logger.info("{} disabled", simpleClazzName);
            return;
        }

        logger.info("{} config:{}", simpleClazzName, config);

        /** Coroutines Lifecycle

           Before                   Run                  After

         +-----------+
         | Start     | -------------+
         +-----------+              |
               |                    |      +---------+
               +------------------- | -----| Executor|-------+
               |                    |      +---------+       |
               |                    |                        |
               V                    V                        |
         +-----------+       +--------------+       +------------+
         | Scheduler | ----> | Continuation | ----> | Dispatcher |
         | Ldispatch |       + L resumeWith |       + Ldispatch  |
         +-----------+       +--------------+       +------------+
                                    |                       |
                                    |                       V
                                    |               +------------+
                                    + ------------> |  Finish    |
                                                    +------------+
        */

        addContinuationTransformer();
        addCombinedContextTransformer();
        addCoroutineDispatcherTransformer();
        if (config.isTraceCancelEvent()) {
            addJobCancelTransformer();
        }
    }

    private void addContinuationTransformer() {
        //        Matcher matcher = Matchers.newClassBasedMatcher("kotlinx.coroutines.Continuation");
        //        transformTemplate.transform(matcher, ContinuationTransform.class);
        //

        // It will be excpected that only the below class to be traced will be sufficient,
        // because user's coroutine implementation is regenerated based on BaseContinuationImpl.
        // However, need to uncomment the above section if it is not being traced.
        transformTemplate.transform("kotlin.coroutines.jvm.internal.BaseContinuationImpl", ContinuationTransform.class);
    }

    private void addCombinedContextTransformer() {
        transformTemplate.transform("kotlin.coroutines.CombinedContext", CombinedContextTransform.class);
    }

    private void addCoroutineDispatcherTransformer() {
        Matcher dispatcherMatcher = Matchers.newPackageBasedMatcher("kotlinx.coroutines",
                new SuperClassInternalNameMatcherOperand("kotlinx.coroutines.CoroutineDispatcher", true));
        transformTemplate.transform(dispatcherMatcher, CoroutineDispatcherTransform.class);
    }

    private void addJobCancelTransformer() {
        transformTemplate.transform("kotlinx.coroutines.JobSupport", JobCancelTransform.class);
    }

    public static class ContinuationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            List<InstrumentMethod> resumeWith = target.getDeclaredMethods(MethodFilters.name("resumeWith"));
            for (InstrumentMethod instrumentMethod : resumeWith) {
                instrumentMethod.addInterceptor(ResumeWithInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            return target.toBytecode();
        }

    }

    public static class CombinedContextTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }

    }

    public static class CoroutineDispatcherTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            List<InstrumentMethod> dispatch = target.getDeclaredMethods(MethodFilters.name("dispatch"));
            for (InstrumentMethod instrumentMethod : dispatch) {
                instrumentMethod.addInterceptor(DispatchInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            List<InstrumentMethod> scheduleResumeAfterDelay = target.getDeclaredMethods(MethodFilters.name("scheduleResumeAfterDelay"));
            for (InstrumentMethod instrumentMethod : scheduleResumeAfterDelay) {
                instrumentMethod.addInterceptor(ScheduleResumeInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            return target.toBytecode();
        }
    }

    public static class JobCancelTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            List<InstrumentMethod> childCancelled = target.getDeclaredMethods(MethodFilters.name("childCancelled"));
            for (InstrumentMethod instrumentMethod : childCancelled) {
                instrumentMethod.addInterceptor(CancelledInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            List<InstrumentMethod> parentCancelled = target.getDeclaredMethods(MethodFilters.name("parentCancelled"));
            for (InstrumentMethod instrumentMethod : parentCancelled) {
                instrumentMethod.addInterceptor(CancelledInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            List<InstrumentMethod> notifyCancelling = target.getDeclaredMethods(MethodFilters.name("notifyCancelling"));
            for (InstrumentMethod instrumentMethod : notifyCancelling) {
                instrumentMethod.addInterceptor(NotifyCancellingInterceptor.class, VarArgs.va(CoroutinesConstants.SERVICE_TYPE));
            }

            return target.toBytecode();
        }

    }

}
