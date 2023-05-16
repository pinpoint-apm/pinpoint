/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventEndPointInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHandleException;

/**
 * @author jaehong.kim
 */
public class HandleExceptionInterceptor extends AsyncContextSpanEventEndPointInterceptor {
    public HandleExceptionInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(VertxConstants.VERTX_INTERNAL);

        final Throwable handleException = ArrayArgumentUtils.getArgument(args, 0, Throwable.class);
        if (handleException != null) {
            if (throwable != null) {
                // handle to two throwable(handle and catch).
                String errorMessage = buildErrorMessage(handleException, throwable);
                recorder.recordException(new VertxHandleException(errorMessage));
            } else {
                // record handle exception.
                recorder.recordException(handleException);
            }
        }
    }

    private static String buildErrorMessage(Throwable handleException, Throwable throwable) {
        String handlerMessage = StringUtils.abbreviate(handleException.getMessage(), 120);
        String throwableMessage = StringUtils.abbreviate(throwable.getMessage(), 120);
        int bufferSize = 32 + StringUtils.getLength(handlerMessage) + StringUtils.getLength(throwableMessage);

        final StringBuilder sb = new StringBuilder(bufferSize);
        sb.append("handle=");
        sb.append(handlerMessage);
        sb.append(", catch=");
        sb.append(throwableMessage);
        return sb.toString();
    }
}
