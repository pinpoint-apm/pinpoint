/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import org.apache.hc.core5.http.HttpRequest;

import java.util.Objects;

public class HttpClient5RequestWrapper implements ClientRequestWrapper {

    private final HttpRequest httpRequest;
    private final String host;

    public HttpClient5RequestWrapper(final HttpRequest httpRequest, final String host) {
        this.httpRequest = Objects.requireNonNull(httpRequest, "httpRequest");
        this.host = host;
    }

    @Override
    public String getDestinationId() {
        return host;
    }

    @Override
    public String getUrl() {
        final String requestUri = this.httpRequest.getRequestUri();
        if (requestUri != null) {
            return requestUri;
        }
        return null;
    }
}