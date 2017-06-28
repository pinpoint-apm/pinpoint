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
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
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

        final HttpUrlData httpUrlData = getHostAndPort(args);
        if (httpUrlData.host != null) {
            // connection address(host:port)
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, HostAndPort.toHostAndPortString(httpUrlData.host, httpUrlData.port));
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 3) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return false;
        }
        return true;
    }

    private HttpUrlData getHostAndPort(final Object[] args) {
        HttpUrlData httpUrlData = new HttpUrlData();
        if (args.length == 3) {
            // 3.3.x
            // int port, String host, Waiter waiter
            if (args[0] instanceof Integer) {
                httpUrlData.port = (Integer) args[0];
            }

            if (args[1] instanceof String) {
                httpUrlData.host = (String) args[1];
            }
        } else if(args.length == 4) {
            // 3.4.0, 3.4.1
            // boolean ssl, int port, String host, Waiter waiter
            if (args[1] instanceof Integer) {
                httpUrlData.port = (Integer) args[1];
            }

            if (args[2] instanceof String) {
                httpUrlData.host = (String) args[2];
            }
        } else if (args.length == 5) {
            // 3.4.2
            // String peerHost, boolean ssl, int port, String host, Waiter waiter
            if (args[2] instanceof Integer) {
                httpUrlData.port = (Integer) args[2];
            }

            if (args[3] instanceof String) {
                httpUrlData.host = (String) args[3];
            }
        }
        return httpUrlData;
    }


    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);
        recorder.recordException(throwable);
    }

    private class HttpUrlData {
        private String host;
        private int port = -1;
    }

}