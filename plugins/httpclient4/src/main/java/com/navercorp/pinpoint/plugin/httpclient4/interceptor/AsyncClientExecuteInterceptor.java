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

import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;

/**
 * 
 * suitable target method
 * <pre>
 * org.apache.http.impl.nio.client.CloseableHttpAsyncClient.execute(HttpHost, HttpRequest, HttpContext, FutureCallback<HttpResponse>)
 * </pre>
 * 
 * original code of method.
 * <pre>
 * <code>
 * public Future<HttpResponse> execute(
 *     final HttpHost target,
 *     final HttpRequest request,
 *     final HttpContext context,
 *     final FutureCallback<HttpResponse> callback) {
 *     
 *     return execute(
 *         HttpAsyncMethods.create(target, request),
 *         HttpAsyncMethods.createConsumer(),
 *         context,
 *         callback);
 * }
 * </code>
 * </pre>
 * 
 * @author netspider
 * 
 */
public class AsyncClientExecuteInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    public AsyncClientExecuteInterceptor() {
        super(AsyncClientExecuteInterceptor.class);
    }

    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        if (args[0] instanceof org.apache.http.HttpHost) {
            final org.apache.http.HttpHost httpHost = (org.apache.http.HttpHost) args[0];
            return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
        } else {
            return null;
        }
    }

    @Override
    protected org.apache.http.HttpRequest getHttpRequest(final Object[] args) {
        if (args[1] instanceof org.apache.http.HttpRequest) {
            return (org.apache.http.HttpRequest) args[1];
        } else {
            return null;
        }
    }

    @Override
    Integer getStatusCode(Object result) {
        return null;
    }
}