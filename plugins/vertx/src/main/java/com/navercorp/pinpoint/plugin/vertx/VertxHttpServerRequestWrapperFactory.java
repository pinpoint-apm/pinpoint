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

import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultServerRequestWrapperFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import io.vertx.core.http.HttpServerRequest;

import java.util.Map;

public class VertxHttpServerRequestWrapperFactory {
    private final DefaultServerRequestWrapperFactory factory;

    public VertxHttpServerRequestWrapperFactory(final String realIpHeaderName, final String realIpHeaderEmptyValue) {
        this.factory = new DefaultServerRequestWrapperFactory(realIpHeaderName, realIpHeaderEmptyValue);
    }

    public ServerRequestWrapper get(final HttpServerRequest request) {
        Assert.requireNonNull(request, "request must not be null");

        final String rpcName = request.path();
        final String endPoint = getEndPoint(request);
        final String remoteAddr = request.remoteAddress() != null ? request.remoteAddress().toString() : "";
        final String acceptorHost = request.uri() != null ? request.uri().toString() : "";
        final String method = request.method() != null ? request.method().name() : "";
        final String parameters = getRequestParameter(request, 64, 512);

        return factory.get(new RequestWrapper() {
            @Override
            public String getHeader(String name) {
                return request.getHeader(name);
            }
        }, rpcName, endPoint, remoteAddr, acceptorHost, method, parameters);
    }


    public String getEndPoint(final HttpServerRequest request) {
        if (request.localAddress() != null) {
            final int port = request.localAddress().port();
            if (port <= 0) {
                return request.host();
            } else {
                return request.host() + ":" + port;
            }
        }
        return null;
    }

    private String getRequestParameter(HttpServerRequest request, int eachLimit, int totalLimit) {
        if (request.params() == null) {
            return "";
        }

        final StringBuilder params = new StringBuilder(64);
        for (Map.Entry<String, String> entry : request.params().entries()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }

            String key = entry.getKey();
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            Object value = entry.getValue();
            if (value != null) {
                params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
            }
        }
        return params.toString();
    }
}