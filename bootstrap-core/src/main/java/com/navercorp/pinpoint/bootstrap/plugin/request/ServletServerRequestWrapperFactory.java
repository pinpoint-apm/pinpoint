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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ServletServerRequestWrapperFactory {
    public static final int PARAMETER_EACH_LIMIT = 64;
    public static final int PARAMETER_TOTAL_LIMIT = 512;

    private DefaultServerRequestWrapperFactory factory;

    public ServletServerRequestWrapperFactory(String realIpHeaderName, String realIpHeaderEmptyValue) {
        this.factory = new DefaultServerRequestWrapperFactory(realIpHeaderName, realIpHeaderEmptyValue);
    }

    public ServerRequestWrapper get(final RequestWrapper requestWrapper, final String uri, final String serverName, final int serverPort, final String remoteAddr, final StringBuffer url, final String method, final Map<String, String[]> parameterMap) {
        // information
        final String rpcName = uri;
        final String endPoint = HostAndPort.toHostAndPortString(serverName, serverPort);
        String acceptorHost = null;
        if (url != null) {
            acceptorHost = NetworkUtils.getHostFromURL(url.toString());
        }
        final String parameters = getRequestParameter(parameterMap, PARAMETER_EACH_LIMIT, PARAMETER_TOTAL_LIMIT);
        return this.factory.get(requestWrapper, rpcName, endPoint, remoteAddr, acceptorHost, method, parameters);
    }

    private static String getRequestParameter(Map<String, String[]> parameterMap, int eachLimit, int totalLimit) {
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