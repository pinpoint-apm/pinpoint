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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;


/**
 * InstrumentMethod interceptor
 * <p/>
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public <T> T execute(
 *            final HttpHost target,
 *            final HttpRequest request,
 *            final ResponseHandler<? extends T> responseHandler,
 *            final HttpContext context)
 *            throws IOException, ClientProtocolException {
 * </pre>
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 */
public class HttpClientExecuteMethodWithHttpRequestInterceptor extends AbstractHttpClientExecuteMethodInterceptor {

    private static final int HTTP_HOST_INDEX = 0;
    private static final int HTTP_REQUEST_INDEX = 1;

    
    public HttpClientExecuteMethodWithHttpRequestInterceptor(boolean isHasCallbackParam, TraceContext context, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        super(HttpClientExecuteMethodWithHttpRequestInterceptor.class, isHasCallbackParam, context, methodDescriptor, interceptorScope);
    }
    
    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        final Object arg = args[HTTP_HOST_INDEX];
        if (arg != null && arg instanceof HttpHost) {
            final HttpHost httpHost = (HttpHost) arg;
            return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
        }
        return null;
    }

    @Override
    protected HttpRequest getHttpRequest(Object[] args) {
        final Object arg = args[HTTP_REQUEST_INDEX];
        if (arg != null && arg instanceof HttpRequest) {
            return (HttpRequest) arg;
        }
        return null;
    }
}