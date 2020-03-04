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

package com.navercorp.pinpoint.plugin.grpc.interceptor.client;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.MethodNameAccessor;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.RemoteAddressAccessor;
import io.grpc.Metadata;

/**
 * @author Taejin Koo
 */
public class ClientCallStartInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final RequestTraceWriter requestTraceWriter;

    public ClientCallStartInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;

        GrpcClientHeaderAdaptor grpcClientHeaderAdaptor = new GrpcClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<Metadata>(grpcClientHeaderAdaptor, traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (ArrayUtils.getLength(args) != 2) {
            return;
        }

        if (!(args[1] instanceof Metadata)) {
            return;
        }

        Metadata metadata = (Metadata) args[1];
        if (!trace.canSampled()) {
            requestTraceWriter.write(metadata);
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();

        recorder.recordApi(descriptor);
        recorder.recordServiceType(GrpcConstants.SERVICE_TYPE);

        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        String remoteAddress = getEndPoint(target);
        recorder.recordEndPoint(remoteAddress);
        recorder.recordDestinationId(remoteAddress);

        String methodName = getMethodName(target);
        recorder.recordAttribute(AnnotationKey.HTTP_URL, combineAddressAndMethodName(remoteAddress, methodName));

        requestTraceWriter.write(metadata, nextId, remoteAddress);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }


    private String combineAddressAndMethodName(String remoteAddress, String methodName) {
        Assert.requireNonNull(remoteAddress, "remoteAddress");
        Assert.requireNonNull(methodName, "methodName");

        if (remoteAddress.startsWith("http")) {
            return remoteAddress + "/" + methodName;
        } else {
            return "http://" + remoteAddress + "/" + methodName;
        }
    }

    private String getMethodName(Object target) {
        if (target instanceof MethodNameAccessor) {
            String methodName = ((MethodNameAccessor) target)._$PINPOINT$_getMethodName();
            if (methodName != null) {
                return methodName;
            }
        }
        return GrpcConstants.UNKNOWN_METHOD;
    }

    public static String getEndPoint(Object target) {
        if (target instanceof RemoteAddressAccessor) {
            String remoteAddress = ((RemoteAddressAccessor) target)._$PINPOINT$_getRemoteAddress();
            if (remoteAddress != null) {
                return remoteAddress;
            }
        }
        return GrpcConstants.UNKNOWN_ADDRESS;
    }

}
