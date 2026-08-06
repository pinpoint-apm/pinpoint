/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * Captures the {@link AsyncContext} a scheduler task should carry across the thread hop, at task
 * construction time. Policy: <b>carrier-first, current-trace fallback</b>.
 * <ol>
 * <li>A context already on the target is kept (constructor delegation may fire this twice).</li>
 * <li>The single distinct context among the constructor arguments is copied — the scheduled
 * {@code Runnable} is often an instrumented operator subscriber ({@code publishOn}/
 * {@code subscribeOn} schedule themselves), and reusing its context preserves the async identity
 * the existing relay established instead of minting a sibling link per schedule.</li>
 * <li>Otherwise, a task scheduled inside an active trace gets a new boundary context recorded as
 * a span event — this covers plain application runnables that carry nothing.</li>
 * </ol>
 * Runs in the constructor's {@code after} on purpose: a failed constructor must not record a
 * boundary span event or leak a dangling async link, and scheduler tasks (unlike operator
 * subscribers) do not hand themselves to inner objects inside the constructor body, so the
 * before-copy timing {@link CoreSubscriberConstructorInterceptor} needs is unnecessary here.
 */
public class SchedulerTaskConstructorInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ServiceType serviceType;

    public SchedulerTaskConstructorInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, ServiceType serviceType) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    /**
     * Not woven in - see {@link IgnoreMethod}. The capture must not run before the constructor has
     * completed successfully.
     */
    @IgnoreMethod
    @Override
    public void before(Object target, Object[] args) {
        // do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (throwable != null) {
            return;
        }

        try {
            if (AsyncContextAccessorUtils.getAsyncContext(target) != null) {
                return;
            }

            final AsyncContext inherited = ReactorAsyncContextResolver.findUnique(args);
            if (inherited != null) {
                AsyncContextAccessorUtils.setAsyncContext(inherited, target);
                if (isDebug) {
                    logger.debug("Copy asyncContext from task argument. asyncContext={}", inherited);
                }
                return;
            }

            final Trace trace = traceContext.currentTraceObject();
            if (trace == null) {
                return;
            }
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            try {
                recorder.recordServiceType(serviceType);
                recorder.recordApi(methodDescriptor);
                final AsyncContext nextAsyncContext = recorder.recordNextAsyncContext();
                AsyncContextAccessorUtils.setAsyncContext(nextAsyncContext, target);
                if (isDebug) {
                    logger.debug("Record boundary asyncContext from current trace. asyncContext={}", nextAsyncContext);
                }
            } finally {
                trace.traceBlockEnd();
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        }
    }
}
