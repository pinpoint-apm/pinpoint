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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import org.apache.http.HttpHost;

import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;

/**
 * 
 * suitable target method
 * 
 * <pre>
 * org.apache.http.impl.nio.client.InternalHttpAsyncClient.execute(HttpAsyncRequestProducer, HttpAsyncResponseConsumer<T>, HttpContext, FutureCallback<T>)
 * org.apache.http.impl.nio.client.CloseableHttpAsyncClient.execute(HttpAsyncRequestProducer, HttpAsyncResponseConsumer<T>, FutureCallback<T>)
 * </pre>
 * 
 * original code of method.
 * 
 * <pre>
 * <code>
 * // org.apache.http.impl.nio.client.InternalHttpAsyncClient.execute
 * public <T> Future<T> execute(
 *     final org.apache.http.nio.protocol.HttpAsyncRequestProducer requestProducer,
 *     final org.apache.http.nio.protocol.HttpAsyncResponseConsumer<T> responseConsumer,
 *        final org.apache.http.protocol.HttpContext context,
 *        final org.apache.http.concurrent.FutureCallback<T> callback) {
 * 
 *        final Status status = getStatus();
 *        Asserts.check(status == Status.ACTIVE, "Request cannot be executed; I/O reactor status: %s", status);
 * 
 *        final BasicFuture<T> future = new BasicFuture<T>(callback);
 *        final HttpClientContext localcontext = HttpClientContext.adapt(context != null ? context : new BasicHttpContext());
 *        setupContext(localcontext);
 * 
 *        @SuppressWarnings("resource")
 *        final DefaultClientExchangeHandlerImpl<T> handler = new DefaultClientExchangeHandlerImpl<T>(
 *            this.log,
 *            requestProducer,
 *            responseConsumer,
 *            localcontext,
 *            future,
 *            this.connmgr,
 *            this.exec);
 * 
 *        try {
 *            handler.start();
 *        } catch (final Exception ex) {
 *            handler.failed(ex);
 *        }
 *        return future;
 * }
 * 
 * OR
 * 
 * // org.apache.http.impl.nio.client.CloseableHttpAsyncClient.execute
 * public <T> Future<T> execute(
 *     final HttpAsyncRequestProducer requestProducer,
 *     final HttpAsyncResponseConsumer<T> responseConsumer,
 *     final FutureCallback<T> callback) {
 *     
 *     return execute(requestProducer, responseConsumer, new BasicHttpContext(), callback);
 * }
 * </code>
 * </pre>
 * 
 * 
 * @author netspider
 * 
 */
public class AsyncInternalClientExecuteInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    public AsyncInternalClientExecuteInterceptor() {
        super(AsyncInternalClientExecuteInterceptor.class);
    }

    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        if (!(args[0] instanceof org.apache.http.nio.protocol.HttpAsyncRequestProducer)) {
            return null;
        }

        final org.apache.http.nio.protocol.HttpAsyncRequestProducer producer = (org.apache.http.nio.protocol.HttpAsyncRequestProducer) args[0];
        final HttpHost httpHost = producer.getTarget();

        return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
    }

    @Override
    protected org.apache.http.HttpRequest getHttpRequest(final Object[] args) {
        if (!(args[0] instanceof org.apache.http.nio.protocol.HttpAsyncRequestProducer)) {
            return null;
        }
        final org.apache.http.nio.protocol.HttpAsyncRequestProducer producer = (org.apache.http.nio.protocol.HttpAsyncRequestProducer) args[0];
        try {
            /**
             * FIXME Implementations other than org.apache.http.nio.protocol.BasicAsyncRequestProducer.generateRequest() can cause some trouble.
             */
            return producer.generateRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
