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

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;

/**
 * @author jaehong.kim
 */
public class HttpConnectionOpenMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements HttpClient3Constants {

    private FieldAccessor hostNameAccessor;
    private FieldAccessor portNumberAccessor;
    private FieldAccessor proxyHostNameAccessor;
    private FieldAccessor proxyPortNumberAccessor;

    public HttpConnectionOpenMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(FIELD_HOST_NAME) FieldAccessor hostNameAccessor, @Name(FIELD_PORT_NUMBER) FieldAccessor portNumberAccessor,
            @Name(FIELD_PROXY_HOST_NAME) FieldAccessor proxyHostNameAccessor, @Name(FIELD_PROXY_PORT_NUMBER) FieldAccessor proxyPortNumberAccessor) {
        super(traceContext, methodDescriptor);
        this.hostNameAccessor = hostNameAccessor;
        this.portNumberAccessor = portNumberAccessor;
        this.proxyHostNameAccessor = proxyHostNameAccessor;
        this.proxyPortNumberAccessor = proxyPortNumberAccessor;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ServiceType.HTTP_CLIENT_INTERNAL);

        if (hostNameAccessor.isApplicable(target) && portNumberAccessor.isApplicable(target) && proxyHostNameAccessor.isApplicable(target) && proxyPortNumberAccessor.isApplicable(target)) {
            final StringBuilder sb = new StringBuilder();
            if (proxyHostNameAccessor.get(target) != null) {
                sb.append(proxyHostNameAccessor.get(target));
                sb.append(":").append(proxyPortNumberAccessor.get(target));
            } else {
                sb.append(hostNameAccessor.get(target));
                sb.append(":").append(proxyPortNumberAccessor.get(target));
            }
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, sb.toString());
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(throwable);
    }
}