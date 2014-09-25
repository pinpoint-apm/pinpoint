package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.pair.NameIntValuePair;

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
}