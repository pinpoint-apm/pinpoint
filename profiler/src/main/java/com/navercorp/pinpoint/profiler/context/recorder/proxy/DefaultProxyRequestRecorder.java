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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaehong.kim
 */
public class DefaultProxyRequestRecorder<T> implements ProxyRequestRecorder<T> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ProxyRequestParserLoaderService proxyRequestParserLoaderServicer;
    private final RequestAdaptor<T> requestAdaptor;
    private final ProxyRequestAnnotationFactory annotationFactory = new ProxyRequestAnnotationFactory();

    public DefaultProxyRequestRecorder(final ProxyRequestParserLoaderService proxyRequestparserLoaderService, final RequestAdaptor<T> requestAdaptor) {
        this.proxyRequestParserLoaderServicer = proxyRequestparserLoaderService;
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");
    }

    public void record(final SpanRecorder recorder, final T request) {
        if (recorder == null || request == null) {
            return;
        }

        try {
            for (ProxyRequestParser parser : proxyRequestParserLoaderServicer.getProxyRequestParserList()) {
                parseAndRecord(recorder, request, parser);
            }
        } catch (Exception e) {
            // for handler operations.
            if (logger.isInfoEnabled()) {
                logger.info("Failed to record proxy http header. cause={}", e.getMessage());
            }
        }
    }


    private void parseAndRecord(final SpanRecorder recorder, final T request, final ProxyRequestParser parser) {
        final String name = parser.getHttpHeaderName();
        final String value = requestAdaptor.getHeader(request, name);
        if (StringUtils.isEmpty(value)) {
            return;
        }

        final ProxyRequestHeader header = parser.parse(value);
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