/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.httpclient3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.httpclient3.HostNameGetter;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;
import com.navercorp.pinpoint.plugin.httpclient3.PortNumberGetter;
import com.navercorp.pinpoint.plugin.httpclient3.ProxyHostNameGetter;
import com.navercorp.pinpoint.plugin.httpclient3.ProxyPortNumberGetter;

/**
 * @author jaehong.kim
 */
public class HttpConnectionOpenMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public HttpConnectionOpenMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(HttpClient3Constants.HTTP_CLIENT_3_INTERNAL);

        if (target instanceof HostNameGetter && target instanceof PortNumberGetter && target instanceof ProxyHostNameGetter && target instanceof ProxyPortNumberGetter) {
            final StringBuilder sb = new StringBuilder();
            if (((ProxyHostNameGetter)target)._$PINPOINT$_getProxyHostName() != null) {
                sb.append(((ProxyHostNameGetter)target)._$PINPOINT$_getProxyHostName());
                sb.append(":").append(((ProxyPortNumberGetter)target)._$PINPOINT$_getProxyPortNumber());
            } else {
                sb.append(((HostNameGetter)target)._$PINPOINT$_getHostName());
                sb.append(":").append(((PortNumberGetter)target)._$PINPOINT$_getPortNumber());
            }
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, sb.toString());
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(throwable);
    }
}