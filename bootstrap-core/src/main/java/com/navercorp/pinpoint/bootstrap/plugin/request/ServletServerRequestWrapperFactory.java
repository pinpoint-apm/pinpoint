/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ServletServerRequestWrapperFactory {
    public static final int PARAMETER_EACH_LIMIT = 64;
    public static final int PARAMETER_TOTAL_LIMIT = 512;

    private final RemoteAddressResolver remoteAddressResolver;

    public ServletServerRequestWrapperFactory(RemoteAddressResolver remoteAddressResolver) {
        this.remoteAddressResolver = Assert.requireNonNull(remoteAddressResolver, "remoteAddressResolver must not be null");
    }

    public ServletServerRequestWrapper get(final RequestWrapper requestWrapper, final String uri, final String serverName, final int serverPort, final String remoteAddr, final StringBuffer url, final String method, final Map<String, String[]> parameterMap) {
        final String endPoint = HostAndPort.toHostAndPortString(serverName, serverPort);

        final String remoteAddress = remoteAddressResolver.getRemoteAddress(requestWrapper, remoteAddr);
        final String acceptorHost = url != null ? NetworkUtils.getHostFromURL(url.toString()) : null;
        final String parameters = getRequestParameter(parameterMap, PARAMETER_EACH_LIMIT, PARAMETER_TOTAL_LIMIT);

        return new ServletServerRequestWrapper(requestWrapper, uri, endPoint, remoteAddress, acceptorHost, method, parameters);
    }


    private static String getRequestParameter(final Map<String, String[]> parameterMap, final int eachLimit, final int totalLimit) {
        final StringBuilder params = new StringBuilder(64);
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (params.length() != 0) {
                params.append('&');
            }
            // skip appending parameters if parameter size is bigger than totalLimit
            if (params.length() > totalLimit) {
                params.append("...");
                return params.toString();
            }
            final String key = entry.getKey();
            if (!StringUtils.hasLength(key)) {
                // skip empty or null header name
                continue;
            }
            // append key
            params.append(StringUtils.abbreviate(key, eachLimit));
            params.append('=');
            // append value
            String[] values = entry.getValue();
            if (!ArrayUtils.hasLength(values)) {
                // skip empty or null header value
                continue;
            }
            for (String value : values) {
                if (value != null) {
                    params.append(StringUtils.abbreviate(StringUtils.toString(value), eachLimit));
                }
            }
        }
        return params.toString();
    }
}