/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorAsyncContextResolver;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

import java.util.Objects;

/**
 * Gives the two exact Reactor retry subscribers a stable async seed when the ordinary constructor
 * relay could not inherit one. The generic subscriber layer is deliberately not involved.
 * <p>
 * The before half preserves the ordinary subscriber constructor relay by copying an unambiguous
 * carrier immediately after {@code super/this}. The after half fills the remaining empty case from
 * the current trace, but only after successful construction so a failed constructor cannot leave a
 * dangling async link.
 * <p>
 * Both halves intentionally live in one interceptor: Pinpoint permits only one interceptor per
 * method, including constructors. Registering the generic copier and a second seed interceptor
 * would cause the latter to be skipped.
 */
public class RetrySubscriberSeedInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ServiceType serviceType;
    private final boolean traceRetry;

    public RetrySubscriberSeedInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, ServiceType serviceType) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
        this.traceRetry = ReactorPluginConfig.isTraceRetry(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            final AsyncContext inherited = ReactorAsyncContextResolver.findUnique(args);
            if (inherited == null) {
                return;
            }
            AsyncContextAccessorUtils.setAsyncContext(inherited, target);
            if (isDebug) {
                logger.debug("Copy retry subscriber seed from constructor argument. asyncContext={}", inherited);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!traceRetry || throwable != null) {
            return;
        }

        try {
            if (AsyncContextAccessorUtils.getAsyncContext(target) != null) {
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
                final AsyncContext seed = recorder.recordNextAsyncContext();
                AsyncContextAccessorUtils.setAsyncContext(seed, target);
                if (isDebug) {
                    logger.debug("Record retry subscriber seed from current trace. asyncContext={}", seed);
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
