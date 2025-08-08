/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.lettuce.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;

public class RedisPubSubListenerInterceptor implements AroundInterceptor {
    // scope name, must be unique.
    private static final String SCOPE_NAME = "##LETTUCE_PUBSUB_LISTENER_TRACE";
    private static final RedisPubSubListenerMethodDescriptor METHOD_DESCRIPTOR = new RedisPubSubListenerMethodDescriptor();

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final String path;

    public RedisPubSubListenerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.path = toPath(methodDescriptor);
        traceContext.cacheApi(METHOD_DESCRIPTOR);
    }

    private String toPath(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return "/RedisPubSubListener";
        }
        final StringBuilder path = new StringBuilder("/");
        if (StringUtils.hasLength(methodDescriptor.getClassName())) {
            path.append(methodDescriptor.getClassName()).append("/");
        }

        if (StringUtils.hasLength(methodDescriptor.getMethodName())) {
            path.append(methodDescriptor.getMethodName());
        }
        return path.toString().replace('.', '/');
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            if (isDebug) {
                logger.debug("Not found trace. Create trace.");
            }

            // create user include trace for standalone entry point.
            trace = createTrace();
            if (trace == null) {
                return;
            }
        } else {
            if (isDebug) {
                logger.debug("Found trace {}, sampled={}.", trace, trace.canSampled());
            }
        }

        // entry scope(default & disable trace).
        entryTraceScope(trace);

        // check sampled.
        if (!trace.canSampled()) {
            // skip
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope(default & disable trace).
        if (!leaveTraceScope(trace)) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to leave scope of user include trace. trace={}, sampled={}", trace, trace.canSampled());
            }
            // delete unstable trace.
            deleteTrace(trace);
            return;
        }

        // check sampled.
        if (!trace.canSampled()) {
            if (isTraceDestination(trace)) {
                deleteTrace(trace);
            }
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
            if (isTraceDestination(trace)) {
                deleteTrace(trace);
            }
        }
    }

    private Trace createTrace() {
        final Trace trace = traceContext.newTraceObject();
        if (isDebug) {
            logger.debug("New trace {} and sampled {}", trace, trace.canSampled());
        }
        // add user scope.
        TraceScope oldScope = trace.addScope(SCOPE_NAME);
        if (oldScope != null) {
            // delete corrupted trace.
            if (logger.isInfoEnabled()) {
                logger.info("Duplicated trace scope={}.", oldScope.getName());
            }
            deleteTrace(trace);
            return null;
        }

        if (trace.canSampled()) {
            // record root span.
            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(ServiceType.STAND_ALONE);
            recorder.recordApi(METHOD_DESCRIPTOR);
            recorder.recordRemoteAddress("LOCAL");
            recorder.recordRpcName(this.path);
            recorder.recordEndPoint("/");
        }
        return trace;
    }

    private void deleteTrace(final Trace trace) {
        if (isDebug) {
            logger.debug("Delete trace={}, sampled={}", trace, trace.canSampled());
        }
        traceContext.removeTraceObject();
        trace.close();
    }

    private void entryTraceScope(final Trace trace) {
        ScopeUtils.entryScope(trace, SCOPE_NAME);
        if (isDebug) {
            logger.debug("Try enter trace scope={}", SCOPE_NAME);
        }
    }

    private boolean leaveTraceScope(final Trace trace) {
        if (ScopeUtils.leaveScope(trace, SCOPE_NAME)) {
            if (isDebug) {
                logger.debug("Leave trace scope={}", SCOPE_NAME);
            }
            return true;
        }
        return false;
    }


    private boolean isTraceDestination(final Trace trace) {
        return ScopeUtils.isEndScope(trace, SCOPE_NAME);
    }
}