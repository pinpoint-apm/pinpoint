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

import java.net.URI;

import org.apache.http.client.methods.HttpUriRequest;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Scope;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * 
 * suitable target method
 * 
 * <pre>
 * org.apache.http.impl.nio.client.CloseableHttpAsyncClient.execute(final HttpUriRequest request, final FutureCallback<HttpResponse> callback)
 * org.apache.http.impl.nio.client.CloseableHttpAsyncClient.execute(final HttpUriRequest request, final HttpContext context, final FutureCallback<HttpResponse> callback)
 * </pre>
 * 
 * original code of method.
 * 
 * <pre>
 * <code>
 *   public Future<HttpResponse> execute(
 *            final HttpUriRequest request,
 *           final FutureCallback<HttpResponse> callback) {
 *       return execute(request, new BasicHttpContext(), callback);
 *   }
 * 
 *   public Future<HttpResponse> execute(
 *           final HttpUriRequest request,
 *           final HttpContext context,
 *           final FutureCallback<HttpResponse> callback) {
 *       final HttpHost target;
 *       try {
 *           target = determineTarget(request);
 *       } catch (final ClientProtocolException ex) {
 *           final BasicFuture<HttpResponse> future = new BasicFuture<HttpResponse>(callback);
 *           future.failed(ex);
 *           return future;
 *       }
 *       return execute(target, request, context, callback);
 *   }
 * </code>
 * </pre>
 * 
 * @author jaehong.kim
 * 
 */
@Scope(HttpClient4Constants.HTTP_CLIENT4_SCOPE)
public class CloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    private static final int HTTP_URI_REQUEST_INDEX = 0;

    public CloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor(TraceContext context, @Cached MethodDescriptor descriptor) {
        super(CloseableHttpAsyncClientExecuteMethodWithHttpUriRequestInterceptor.class, context, descriptor);
    }

    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        final HttpUriRequest httpUriRequest = getHttpUriRequest(args);
        if (httpUriRequest == null) {
            return null;
        }
        final URI uri = httpUriRequest.getURI();
        return extractHost(uri);
    }

    @Override
    protected org.apache.http.HttpRequest getHttpRequest(final Object[] args) {
        return getHttpUriRequest(args);
    }

    @Override
    Integer getStatusCode(Object result) {
        return null;
    }

    private HttpUriRequest getHttpUriRequest(Object[] args) {
        final Object arg = args[HTTP_URI_REQUEST_INDEX];
        if (arg instanceof HttpUriRequest) {
            return (HttpUriRequest) arg;
        }
        return null;
    }

    /**
     * copy org.apache.http.client.utils.URIUtils#extractHost(java.net.URI)
     * 
     * @param uri
     * @return
     */
    private NameIntValuePair<String> extractHost(final URI uri) {
        if (uri == null) {
            return null;
        }
        NameIntValuePair<String> target = null;
        if (uri.isAbsolute()) {
            int port = uri.getPort(); // may be overridden later
            String host = uri.getHost();
            if (host == null) { // normal parse failed; let's do it ourselves
                // authority does not seem to care about the valid character-set
                // for host names
                host = uri.getAuthority();
                if (host != null) {
                    // Strip off any leading user credentials
                    int at = host.indexOf('@');
                    if (at >= 0) {
                        if (host.length() > at + 1) {
                            host = host.substring(at + 1);
                        } else {
                            host = null; // @ on its own
                        }
                    }
                    // Extract the port suffix, if present
                    if (host != null) {
                        int colon = host.indexOf(':');
                        if (colon >= 0) {
                            int pos = colon + 1;
                            int len = 0;
                            for (int i = pos; i < host.length(); i++) {
                                if (Character.isDigit(host.charAt(i))) {
                                    len++;
                                } else {
                                    break;
                                }
                            }
                            if (len > 0) {
                                try {
                                    port = Integer.parseInt(host.substring(pos, pos + len));
                                } catch (NumberFormatException ignore) {
                                    // skip
                                }
                            }
                            host = host.substring(0, colon);
                        }
                    }
                }
            }
            if (host != null) {
                target = new NameIntValuePair<String>(host, port);
            }
        }
        return target;
    }

}