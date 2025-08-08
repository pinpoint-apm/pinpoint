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
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import io.grpc.Metadata;

/**
 * @author Taejin Koo
 */
public class ClientCallStartInterceptor extends SpanEventBlockSimpleAroundInterceptorForPlugin {
    private final ClientRequestRecorder<ClientRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter requestTraceWriter;

    public ClientCallStartInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        ClientRequestAdaptor<ClientRequestWrapper> clientRequestAdaptor = ClientRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientRequestRecorder<>(Boolean.FALSE, clientRequestAdaptor);

        GrpcClientHeaderAdaptor grpcClientHeaderAdaptor = new GrpcClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(grpcClientHeaderAdaptor, traceContext);
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(Trace trace, Object target, Object[] args) {
        final Metadata metadata = ArrayArgumentUtils.getArgument(args, 1, Metadata.class);
        if (metadata == null) {
            return false;
        }

        if (requestTraceWriter.isNested(metadata)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            requestTraceWriter.write(metadata);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        Metadata metadata = ArrayArgumentUtils.getArgument(args, 1, Metadata.class);
        if (metadata == null) {
            return;
        }

        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());

        ClientRequestWrapper clientRequestWrapper = new GrpcClientRequestWrapper(target);
        requestTraceWriter.write(metadata, nextId, clientRequestWrapper.getDestinationId());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordServiceType(GrpcConstants.SERVICE_TYPE);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final ClientRequestWrapper clientRequestWrapper = new GrpcClientRequestWrapper(target);
        clientRequestRecorder.record(recorder, clientRequestWrapper, throwable);
    }
}
