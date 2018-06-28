/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.vertx.core.http.HttpClientRequest;

/**
 * @author jaehong.kim
 */
public class VertxHttpClientRequestWrapper implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpClientRequest httpClientRequest;
    private final HttpRequest httpRequest;
    private final String host;

    public VertxHttpClientRequestWrapper(final HttpClientRequest httpClientRequest) {
        this.httpClientRequest = Assert.requireNonNull(httpClientRequest, "request must not be null");
        this.httpRequest = null;
        this.host = null;
    }

    public VertxHttpClientRequestWrapper(final HttpRequest httpRequest, final String host) {
        this.httpRequest = Assert.requireNonNull(httpRequest, "httpRequest must not be null");
        this.httpClientRequest = null;
        this.host = host;
    }

    @Override
    public void setHeader(String name, String value) {
        if (this.httpClientRequest != null) {
            this.httpClientRequest.putHeader(name, value);
            if (isDebug) {
                logger.debug("Set header {}={}", name, value);
            }
        } else {
            final HttpHeaders headers = this.httpRequest.headers();
            if (headers != null) {
                headers.set(name, value);
                if (isDebug) {
                    logger.debug("Set header {}={}", name, value);
                }
            }
        }
    }

    @Override
    public String getHost() {
        if (this.httpClientRequest != null) {
            throw new UnsupportedOperationException("Must be used only in the HttpClientImplDoRequestInterceptor class");
        }
        return this.host;
    }

    @Override
    public String getDestinationId() {
        if (this.httpClientRequest != null) {
            throw new UnsupportedOperationException("Must be used only in the HttpClientImplDoRequestInterceptor class");
        }
        if (this.host != null) {
            return this.host;
        }
        return "Unknown";
    }

    @Override
    public String getUrl() {
        if (this.httpClientRequest != null) {
            throw new UnsupportedOperationException("Must be used only in the HttpClientImplDoRequestInterceptor class");
        }
        return this.httpRequest.uri();
    }

    @Override
    public String getEntityValue() {
        if (this.httpClientRequest != null) {
            throw new UnsupportedOperationException("Must be used only in the HttpClientImplDoRequestInterceptor class");
        }
        return null;
    }

    @Override
    public String getCookieValue() {
        if (this.httpClientRequest != null) {
            throw new UnsupportedOperationException("Must be used only in the HttpClientImplDoRequestInterceptor class");
        }
        final HttpHeaders headers = this.httpRequest.headers();
        if (headers != null) {
            final String cookie = headers.get("Cookie");
            if (StringUtils.hasLength(cookie)) {
                return cookie;
            }
        }
        return null;
    }
}
