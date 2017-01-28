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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;

/**
 * @author jaehong.kim
 */
public class HttpClientImplGetConnectionForRequest extends SpanEventSimpleAroundInterceptorForPlugin {
    public HttpClientImplGetConnectionForRequest(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        final int port = (Integer) args[0];
        final String host = (String) args[1];
        if (host != null) {
            // connection address(host:port)
            if (port > 0) {
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, host + ":" + port);
            } else {
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, host);
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 2) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof Integer)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. {}.", args[0]);
            }
            return false;
        }

        if (!(args[1] instanceof String)) {
            if (isDebug) {
                logger.debug("Invalid args[1] object. {}.", args[1]);
            }
            return false;
        }

        return true;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);
        recorder.recordException(throwable);
    }
}