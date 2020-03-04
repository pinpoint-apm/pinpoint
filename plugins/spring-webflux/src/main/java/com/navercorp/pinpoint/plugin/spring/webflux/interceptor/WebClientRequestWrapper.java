/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import org.springframework.http.client.reactive.ClientHttpRequest;

import java.net.URI;

/**
 * @author jaehong.kim
 */
public class WebClientRequestWrapper implements ClientRequestWrapper {

    private final ClientHttpRequest request;

    public WebClientRequestWrapper(final ClientHttpRequest request) {
        this.request = request;
    }

    @Override
    public String getDestinationId() {
        if (this.request != null && this.request.getURI() != null) {
            final URI uri = request.getURI();
            return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
        }
        return "Unknown";
    }

    @Override
    public String getUrl() {
        if (this.request != null && this.request.getURI() != null) {
            return this.request.getURI().toString();
        }
        return null;
    }
}