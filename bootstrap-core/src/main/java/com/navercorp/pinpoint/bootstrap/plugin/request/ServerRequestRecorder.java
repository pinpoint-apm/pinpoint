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

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ServerRequestRecorder<T> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final RequestAdaptor<T> requestAdaptor;

    private static final String REQUEST_COOKIE_HEADER = "Cookie";

    private final List<String> recordRequestHeaderList = new ArrayList<String>();
    private final List<String> recordRequestCookieList = new ArrayList<String>();

    public ServerRequestRecorder(RequestAdaptor<T> requestAdaptor) {
        this(requestAdaptor, "", "");
    }

    public ServerRequestRecorder(RequestAdaptor<T> requestAdaptor, String recordRequestHeaders, String recordRequestCookies) {
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");
        List<String> headerList = StringUtils.tokenizeToStringList(recordRequestHeaders, ",");
        if (!headerList.isEmpty()) {
            this.recordRequestHeaderList.addAll(headerList);
        }
        List<String> cookieList = StringUtils.tokenizeToStringList(recordRequestCookies, ",");
        if (!cookieList.isEmpty()) {
            this.recordRequestCookieList.addAll(cookieList);
        }
    }


    // Records the server's request information.
    public void record(final SpanRecorder recorder, final T request) {
        if (recorder == null || request == null) {
            return;
        }
        final String rpcName = requestAdaptor.getRpcName(request);
        recorder.recordRpcName(rpcName);
        if (isDebug) {
            logger.debug("Record rpcName={}", rpcName);
        }

        final String endPoint = requestAdaptor.getEndPoint(request);
        recorder.recordEndPoint(endPoint);
        if (isDebug) {
            logger.debug("Record endPoint={}", endPoint);
        }

        final String remoteAddress = requestAdaptor.getRemoteAddress(request);
        recorder.recordRemoteAddress(remoteAddress);
        if (isDebug) {
            logger.debug("Record remoteAddress={}", remoteAddress);
        }

        recordRequestHeader(recorder, request);
        recordRequestCookie(recorder, request);

        if (!recorder.isRoot()) {
            recordParentInfo(recorder, request);
        }
    }

    private void recordRequestHeader(final SpanRecorder recorder, final T request) {
        if (recordRequestHeaderList.isEmpty()) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>(recordRequestHeaderList.size());
        for (String headerName : recordRequestHeaderList) {
            String val = requestAdaptor.getHeader(request, headerName);
            if (val != null) {
                map.put(headerName, val);
            }
        }
        if (!map.isEmpty()) {
            recorder.recordAttribute(AnnotationKey.HTTP_REQUEST_HEADER, buildMapString(map));
        }
    }

    private void recordRequestCookie(final SpanRecorder recorder, final T request) {
        if (recordRequestCookieList.isEmpty()) {
            return;
        }
        String cookieStr = requestAdaptor.getHeader(request, REQUEST_COOKIE_HEADER);
        Map<String, String> cookieAccepted = filterCookie(cookieStr);
        if (!cookieAccepted.isEmpty()) {
            recorder.recordAttribute(AnnotationKey.HTTP_COOKIE, buildMapString(cookieAccepted));
        }
    }

    private Map<String, String> filterCookie(String cookieStr) {
        List<String> cookieList = StringUtils.tokenizeToStringList(cookieStr, ";");
        if (cookieList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<String, String>(cookieList.size());
        for (String kv : cookieList) {
            String[] kvData = kv.split("=", 2);
            if (kvData.length != 2) {
                continue;
            }
            String k = kvData[0];
            String v = kvData[1];
            if (recordRequestCookieList.contains(k)) {
                map.put(k, v);
            }
        }
        return map;
    }

    private String buildMapString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        int total = map.size();
        int cnt = 0;
        for (String k : map.keySet()) {
            cnt++;
            sb.append(k);
            sb.append('=');
            sb.append(map.get(k));
            boolean isLast = cnt == total;
            if (!isLast) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    private void recordParentInfo(final SpanRecorder recorder, final T request) {
        final String parentApplicationName = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_NAME.toString());
        if (parentApplicationName != null) {
            String host = requestAdaptor.getHeader(request, Header.HTTP_HOST.toString());
            if (host == null) {
                host = requestAdaptor.getAcceptorHost(request);
            }
            recorder.recordAcceptorHost(host);
            if (isDebug) {
                logger.debug("Record acceptorHost={}", host);
            }

            final String type = requestAdaptor.getHeader(request, Header.HTTP_PARENT_APPLICATION_TYPE.toString());
            final short parentApplicationType = NumberUtils.parseShort(type, ServiceType.UNDEFINED.getCode());
            recorder.recordParentApplication(parentApplicationName, parentApplicationType);
            if (isDebug) {
                logger.debug("Record parentApplicationName={}, parentApplicationType={}", parentApplicationName, parentApplicationType);
            }
        } else {
            if (isDebug) {
                logger.debug("Not found parentApplication");
            }
        }
    }
}