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

package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.common.util.Assert;
import java.util.Optional;

/**
 * @author lopiter
 */
public class AkkaHttpServerRequestTrace implements ServerRequestTrace {
    private final HttpRequest request;

    public AkkaHttpServerRequestTrace(final HttpRequest request) {
        this.request = Assert.requireNonNull(request, "");
    }

    @Override
    public String getHeader(String name) {
        return getHeaderValue(request, name);
    }

    @Override
    public String getRpcName() {
        throw new UnsupportedOperationException("not implements yet");
    }

    @Override
    public String getEndPoint() {
        throw new UnsupportedOperationException("not implements yet");
    }

    @Override
    public String getRemoteAddress() {
        throw new UnsupportedOperationException("not implements yet");
    }

    @Override
    public String getAcceptorHost() {
        throw new UnsupportedOperationException("not implements yet");
    }


    private String getHeaderValue(final HttpRequest request, final String name) {
        return getHeaderValue(request, name, null);
    }

    private String getHeaderValue(final HttpRequest request, final String name, String defaultValue) {
        if (request == null) {
            return defaultValue;
        }

        Optional<HttpHeader> optional = request.getHeader(name);
        if (optional == null) {
            return defaultValue;
        }

        HttpHeader header = optional.orElse(null);
        if (header == null) {
            return defaultValue;
        }

        String value = header.value();
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}