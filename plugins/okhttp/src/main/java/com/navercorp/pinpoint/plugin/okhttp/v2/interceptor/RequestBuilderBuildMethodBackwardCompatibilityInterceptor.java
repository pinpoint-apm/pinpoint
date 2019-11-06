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
package com.navercorp.pinpoint.plugin.okhttp.v2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.okhttp.EndPointUtils;
import com.navercorp.pinpoint.plugin.okhttp.v2.UrlGetter;

import java.net.URL;

/**
 * @author jaehong.kim
 */
public class RequestBuilderBuildMethodBackwardCompatibilityInterceptor extends AbstractRequestBuilderBuildMethodInterceptor {

    public RequestBuilderBuildMethodBackwardCompatibilityInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        super(traceContext, methodDescriptor, interceptorScope);
    }

    @Override
    String toHost(Object target) {
        if (target instanceof UrlGetter) {
            final URL url = ((UrlGetter) target)._$PINPOINT$_getUrl();
            return getDestinationId(url);
        }
        return null;
    }

    private String getDestinationId(URL httpUrl) {
        if (httpUrl == null || httpUrl.getHost() == null) {
            return "UnknownHttpClient";
        }
        final int port = EndPointUtils.getPort(httpUrl.getPort(), httpUrl.getDefaultPort());
        return HostAndPort.toHostAndPortString(httpUrl.getHost(), port);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
    }
}