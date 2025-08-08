/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.okhttp.v3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;
import com.navercorp.pinpoint.plugin.okhttp.v3.RouteGetter;
import okhttp3.Address;
import okhttp3.Route;

/**
 * @author jaehong.kim
 */
public class RealConnectionConnectMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean markError;

    public RealConnectionConnectMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        this.markError = OkHttpPluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(markError, throwable);

        if (target instanceof RouteGetter) {
            final Route route = ((RouteGetter) target)._$PINPOINT$_getRoute();
            if (route != null) {
                final String hostAndPort = getHostAndPort(route);
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, hostAndPort);
            }
        }
    }

    private String getHostAndPort(Route route) {
        final Address address = route.address();
        return HostAndPort.toHostAndPortString(address.url().host(), address.url().port());
    }
}
