package com.nhn.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.pair.NameIntValuePair;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

/**
 * MethodInfo interceptor
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
 */
public class HttpRequestExecuteInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    private static final int HTTP_HOST_INDEX = 0;
    private static final int HTTP_REQUEST_INDEX = 1;

    public HttpRequestExecuteInterceptor() {
        this.logger = PLoggerFactory.getLogger(this.getClass());
        this.isDebug = logger.isDebugEnabled();
    }

    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        final Object arg = args[HTTP_HOST_INDEX];
        if (arg instanceof HttpHost) {
            final HttpHost httpHost = (HttpHost) arg;
            return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
        }
        return null;
    }

    @Override
    protected HttpRequest getHttpRequest(Object[] args) {
        final Object arg = args[HTTP_REQUEST_INDEX];
        if (arg instanceof HttpRequest) {
            return (HttpRequest) arg;
        }
        return null;
    }


}