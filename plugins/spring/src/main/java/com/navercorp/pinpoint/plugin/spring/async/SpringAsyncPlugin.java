/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.async;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.spring.async.interceptor.AsyncTaskExecutorSubmitInterceptor;
import com.navercorp.pinpoint.plugin.spring.async.interceptor.TaskCallInterceptor;

import java.security.ProtectionDomain;
import java.util.Set;

public class SpringAsyncPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final SpringAsyncConfig config = new SpringAsyncConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        interceptTask(config);
        interceptTaskExecutor(config);
    }

    private void interceptTask(SpringAsyncConfig config) {
        final Set<String> list = config.getAsyncTaskClassNameList();
        for (String className : list) {
            if (StringUtils.hasLength(className)) {
                addAsyncExecutionInterceptorTask(Matchers.newPackageBasedMatcher(className));
            }
        }
    }

    private void interceptTaskExecutor(SpringAsyncConfig config) {
        final Set<String> list = config.getAsyncTaskExecutorClassNameList();
        for (String className : list) {
            if (StringUtils.hasLength(className)) {
                addAsyncTaskExecutor(className);
            }
        }
    }

    private void addAsyncExecutionInterceptorTask(final Matcher matcher) {
        transformTemplate.transform(matcher, AsyncExecutionInterceptorTaskTransform.class);
    }

    public static class AsyncExecutionInterceptorTaskTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            final InstrumentMethod callMethod = target.getDeclaredMethod("call");
            if (callMethod != null) {
                callMethod.addInterceptor(TaskCallInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addAsyncTaskExecutor(final String className) {
        transformTemplate.transform(className, AsyncTaskExecutorTransform.class);
    }

    public static class AsyncTaskExecutorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final String callable = "java.util.concurrent.Callable";

            final InstrumentMethod submitMethod = target.getDeclaredMethod("submit", callable);
            if (submitMethod != null) {
                submitMethod.addScopedInterceptor(AsyncTaskExecutorSubmitInterceptor.class, SpringAsyncConstants.ASYNC_TASK_EXECUTOR_SCOPE);
            }

            final InstrumentMethod submitListenableMethod = target.getDeclaredMethod("submitListenable", callable);
            if (submitListenableMethod != null) {
                submitListenableMethod.addScopedInterceptor(AsyncTaskExecutorSubmitInterceptor.class, SpringAsyncConstants.ASYNC_TASK_EXECUTOR_SCOPE);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}