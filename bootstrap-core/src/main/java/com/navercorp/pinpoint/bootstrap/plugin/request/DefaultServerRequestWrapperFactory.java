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

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class DefaultServerRequestWrapperFactory {
    private final String realIpHeaderName;
    private final String realIpHeaderEmptyValue;

    public DefaultServerRequestWrapperFactory(String realIpHeaderName, String realIpHeaderEmptyValue) {
        this.realIpHeaderName = realIpHeaderName;
        this.realIpHeaderEmptyValue = realIpHeaderEmptyValue;
    }

    public ServerRequestWrapper get(final RequestWrapper requestWrapper, final String rpcName, final String endPoint, final String remoteAddr, final String acceptorHost, final String method, final String parameters) {
        final DefaultServerRequestWrapper serverRequestTrace = new DefaultServerRequestWrapper();
        // header
        serverRequestTrace.setParentApplicationName(getHeader(requestWrapper, Header.HTTP_PARENT_APPLICATION_NAME.toString()));
        serverRequestTrace.setHost(getHeader(requestWrapper, Header.HTTP_HOST.toString()));
        serverRequestTrace.setType(getHeader(requestWrapper, Header.HTTP_PARENT_APPLICATION_TYPE.toString()));
        serverRequestTrace.setSamplingFlag(getHeader(requestWrapper, Header.HTTP_SAMPLED.toString()));
        serverRequestTrace.setParentApplicationNamespace(getHeader(requestWrapper, Header.HTTP_PARENT_APPLICATION_NAMESPACE.toString()));
        serverRequestTrace.setTransactionId(getHeader(requestWrapper, Header.HTTP_TRACE_ID.toString()));
        serverRequestTrace.setParentSpanId(NumberUtils.parseLong(getHeader(requestWrapper, Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL));
        serverRequestTrace.setSpanId(NumberUtils.parseLong(getHeader(requestWrapper, Header.HTTP_SPAN_ID.toString()), SpanId.NULL));
        serverRequestTrace.setFlags(NumberUtils.parseShort(getHeader(requestWrapper, Header.HTTP_FLAGS.toString()), (short) 0));
        final Map<String, String> proxyHeaderMap = new HashMap<String, String>();
        proxyHeaderMap.put(Header.HTTP_PROXY_APP.toString(), getHeader(requestWrapper, Header.HTTP_PROXY_APP.toString()));
        proxyHeaderMap.put(Header.HTTP_PROXY_NGINX.toString(), getHeader(requestWrapper, Header.HTTP_PROXY_NGINX.toString()));
        proxyHeaderMap.put(Header.HTTP_PROXY_APACHE.toString(), getHeader(requestWrapper, Header.HTTP_PROXY_APACHE.toString()));
        serverRequestTrace.setProxyHeaderMap(proxyHeaderMap);

        // information
        serverRequestTrace.setRpcName(rpcName);
        serverRequestTrace.setEndPoint(endPoint);
        serverRequestTrace.setRemoteAddress(getRemoteAddress(this.realIpHeaderName, this.realIpHeaderEmptyValue, requestWrapper, remoteAddr));
        serverRequestTrace.setAcceptorHost(acceptorHost);
        serverRequestTrace.setMethod(method);
        serverRequestTrace.setParameters(parameters);
        return serverRequestTrace;
    }

    private String getHeader(final RequestWrapper requestWrapper, final String name) {
        try {
            return requestWrapper.getHeader(name);
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getRemoteAddress(final String realIpHeaderName, final String realIpHeaderEmptyValue, final RequestWrapper requestWrapper, final String remoteAddr) {
        if (!StringUtils.hasLength(realIpHeaderName)) {
            return remoteAddr;
        }
        final String realIp = requestWrapper.getHeader(realIpHeaderName);
        if (StringUtils.isEmpty(realIp)) {
            return remoteAddr;
        }

        if (realIpHeaderEmptyValue != null && realIpHeaderEmptyValue.equalsIgnoreCase(realIp)) {
            return remoteAddr;
        }

        final int firstIndex = realIp.indexOf(',');
        if (firstIndex == -1) {
            return realIp;
        } else {
            return realIp.substring(0, firstIndex);
        }
    }
}
