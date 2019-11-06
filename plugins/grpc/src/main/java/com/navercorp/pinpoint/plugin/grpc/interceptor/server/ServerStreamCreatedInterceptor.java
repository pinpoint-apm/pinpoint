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

package com.navercorp.pinpoint.plugin.grpc.interceptor.server;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import com.navercorp.pinpoint.plugin.grpc.descriptor.GrpcServerCallMethodDescritpro;

/**
 * @author Taejin Koo
 */
public class ServerStreamCreatedInterceptor implements AroundInterceptor {

    private static final GrpcServerCallMethodDescritpro GRPC_SERVER_CALL_METHOD_DESCRIPTOR = new GrpcServerCallMethodDescritpro();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final ServerRequestRecorder<GrpcServerStreamRequest> serverRequestRecorder;
    private final RequestTraceReader<GrpcServerStreamRequest> requestTraceReader;

    public ServerStreamCreatedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;

        final RequestAdaptor<GrpcServerStreamRequest> requestAdaptor = new GrpcServerStreamRequestAdaptor();
        this.serverRequestRecorder = new ServerRequestRecorder<GrpcServerStreamRequest>(requestAdaptor);
        this.requestTraceReader = new RequestTraceReader(traceContext, requestAdaptor, true);

        traceContext.cacheApi(GRPC_SERVER_CALL_METHOD_DESCRIPTOR);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (traceContext.currentTraceObject() != null) {
            return;
        }

        GrpcServerStreamRequest request = GrpcServerStreamRequest.create(args);
        if (request == null) {
            return;
        }

        final Trace trace = createTrace(request);
        if (trace == null || !trace.canSampled()) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(GrpcConstants.SERVER_SERVICE_TYPE_INTERNAL);

        final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
        if (args[0] instanceof AsyncContextAccessor) {
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            logger.debug("Set closeable-AsyncContext {}", asyncContext);
        }
    }

    private Trace createTrace(final GrpcServerStreamRequest request) {
        Trace trace = requestTraceReader.read(request);
        if (trace.canSampled()) {
            SpanRecorder spanRecorder = trace.getSpanRecorder();
            spanRecorder.recordServiceType(GrpcConstants.SERVER_SERVICE_TYPE);
            spanRecorder.recordApi(GRPC_SERVER_CALL_METHOD_DESCRIPTOR);

            this.serverRequestRecorder.record(spanRecorder, request);
        }

        return trace;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            deleteTrace(trace);
            return;
        }

        if (!GrpcServerStreamRequest.validate(args)) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }

}
