package com.profiler.modifier.connector.interceptors;

import com.profiler.interceptor.StaticAroundInterceptor;

/**
 * Method interceptor
 * 
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public <T> T execute(
 *            final HttpHost target, 
 *            final HttpRequest request,
 *            final ResponseHandler<? extends T> responseHandler, 
 *            final HttpContext context) 
 *            throws IOException, ClientProtocolException {
 * </pre>
 */
public class ExecuteMethodInterceptor implements StaticAroundInterceptor {

	/**
	 * <pre>
	 * args is...
	 * 
	 * org.apache.http.impl.client.AbstractHttpClient.
	 * public <T> T execute(
	 *            final HttpHost target, 
	 *            final HttpRequest request,
	 *            final ResponseHandler<? extends T> responseHandler, 
	 *            final HttpContext context) 
	 *            throws IOException, ClientProtocolException {
	 * </pre>
	 */
	@Override
	public void before(Object target, String className, String methodName, Object[] args) {
		System.out.println("\n\n\n\nHTTP BEFORE");
		// TODO : add header
	}

	@Override
	public void after(Object target, String className, String methodName, Object[] args, Object result) {
		System.out.println("\n\n\n\nHTTP AFTER");
	}
}