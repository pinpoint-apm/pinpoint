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
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;

/**
 * @author Taejin Koo
 */
public class ServerListenerInterceptor extends GrpcAsyncContextSpanEventEndPointInterceptor {

    public ServerListenerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    // BEFORE
    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        if (target instanceof AsyncContextAccessor) {
            return ((AsyncContextAccessor) target)._$PINPOINT$_getAsyncContext();
        }

        logger.info("failed to get AsyncContext");
        return null;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    // AFTER
    @Override
    protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (target instanceof AsyncContextAccessor) {
            return ((AsyncContextAccessor) target)._$PINPOINT$_getAsyncContext();
        }

        logger.info("failed to get AsyncContext");
        return null;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(GrpcConstants.SERVER_SERVICE_TYPE_INTERNAL);
    }
}
