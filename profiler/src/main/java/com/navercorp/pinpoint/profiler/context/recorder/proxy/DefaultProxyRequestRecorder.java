/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.recorder.proxy;

import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;

import java.util.Objects;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class DefaultProxyRequestRecorder<T> implements ProxyRequestRecorder<T> {
    private static final Logger logger = LogManager.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ProxyRequestParser[] proxyRequestParsers;
    private final RequestAdaptor<T> requestAdaptor;
    private final ProxyRequestAnnotationFactory annotationFactory = new ProxyRequestAnnotationFactory();

    public DefaultProxyRequestRecorder(final List<ProxyRequestParser> proxyRequestParserList, final RequestAdaptor<T> requestAdaptor) {
        Objects.requireNonNull(proxyRequestParserList, "proxyRequestParserList");
        this.proxyRequestParsers = proxyRequestParserList.toArray(new ProxyRequestParser[0]);
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "requestAdaptor");
    }

    public void record(final SpanRecorder recorder, final T request) {
        if (recorder == null || request == null) {
            return;
        }

        try {
            for (ProxyRequestParser parser : proxyRequestParsers) {
                parseHeaderAndRecord(recorder, request, parser);
            }
        } catch (Exception e) {
            // for handler operations.
            if (logger.isInfoEnabled()) {
                logger.info("Failed to record proxy http header. cause={}", e.getMessage());
            }
        }
    }

    private void parseHeaderAndRecord(final SpanRecorder recorder, final T request, final ProxyRequestParser parser) {
        for (String name : parser.getHttpHeaderNameList()) {
            final String value = requestAdaptor.getHeader(request, name);
            if (StringUtils.isEmpty(value)) {
                return;
            }

            final ProxyRequestHeader header = parser.parseHeader(name, value);
            if (header.isValid()) {
                recorder.recordAttribute(annotationFactory.getAnnotationKey(), annotationFactory.getAnnotationValue(parser.getCode(), header));
                if (isDebug) {
                    logger.debug("Record proxy request header. name={}, value={}", name, value);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to parse proxy request header. name={}. value={}, cause={}", name, value, header.getCause());
                }
            }
        }
    }
}