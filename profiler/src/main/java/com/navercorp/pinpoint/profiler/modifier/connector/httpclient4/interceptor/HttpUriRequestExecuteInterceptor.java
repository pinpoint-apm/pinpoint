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

import java.net.URI;

import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * MethodInfo interceptor
 * <p/>
 * <p/>
 * 
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public final HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException
 * </pre>
 * @author emeroad
 */
public class HttpUriRequestExecuteInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    private static final int HTTP_URI_REQUEST_INDEX = 0;

    public HttpUriRequestExecuteInterceptor() {
        super(HttpUriRequestExecuteInterceptor.class);
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
    protected HttpRequest getHttpRequest(Object[] args) {
        return getHttpUriRequest(args);
    }

    private HttpUriRequest getHttpUriRequest(Object[] args) {
        final Object arg = args[HTTP_URI_REQUEST_INDEX];
        if (arg instanceof HttpUriRequest) {
            return (HttpUriRequest) arg;
        }
        return null;
    }

    /**
     * copy
     * org.apache.http.client.utils.URIUtils#extractHost(java.net.URI)
     * @param uri
     * @return
     */
    private NameIntValuePair<String> extractHost(final URI uri) {
       	if (uri == nu          l) {
	       	return null;
		}
        NameIntValuePair<St       ing> target = null;          		if (uri.isAbsolute()) {
			int port = uri.ge          Port(); // may be over          idden later
			String host = uri.getHost();
			if (host ==              ull) { // normal parse failed; let's do it ourselves
			             // author             ty does not seem t              care about                the valid character-set
				//                 or host names
			                host                   = uri.getAuthorit                      ();
				if (host                                        != null) {
                                                 				// Str                p off any                   leading user crede                   tials                      					i                                           t at = host.indexOf('@');
		                         		if (at >= 0) {
						i                                                                                                                                                                                                                             (host.length() > at + 1) {
							host = h                                                                                                                                              st.substring(at + 1);                      				    	} else {
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