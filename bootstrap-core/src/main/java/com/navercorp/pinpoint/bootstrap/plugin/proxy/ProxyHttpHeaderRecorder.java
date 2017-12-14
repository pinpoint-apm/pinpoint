/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.proxy;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderRecorder {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();
    private final ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
    private final boolean enable;

    public ProxyHttpHeaderRecorder(final boolean enable) {
        this.enable = enable;
    }

    public void record(final SpanRecorder recorder, final ProxyHttpHeaderHandler handler) {
        if (recorder == null || handler == null) {
            return;
        }

        if (this.enable == Boolean.FALSE) {
            if (isDebug) {
                logger.debug("Disable record proxy http header.");
            }
            return;
        }

        try {
            parseAndRecord(recorder, handler, Header.HTTP_PROXY_APP.toString(), ProxyHttpHeader.TYPE_APP);
            parseAndRecord(recorder, handler, Header.HTTP_PROXY_NGINX.toString(), ProxyHttpHeader.TYPE_NGINX);
            parseAndRecord(recorder, handler, Header.HTTP_PROXY_APACHE.toString(), ProxyHttpHeader.TYPE_APACHE);
        } catch (Exception e) {
            // for handler operations.
            if (isInfo) {
                logger.info("Failed to record proxy http header. cause={}", e.getMessage());
            }
        }
    }


    private void parseAndRecord(final SpanRecorder recorder, final ProxyHttpHeaderHandler handler, final String name, final int type) {
        final String value = handler.read(name);
        if (value == null || value.isEmpty()) {
            return;
        }

        final ProxyHttpHeader header = this.parser.parse(type, value);
        if (header.isValid()) {
            recorder.recordAttribute(header.getAnnotationKey(), header.getAnnotationValue());
            if (isDebug) {
                logger.debug("Record proxy http header. name={}, value={}", name, value);
            }
        } else {
            if (isInfo) {
                logger.info("Failed to parse proxy http header. name={}. value={}, cause={}", name, value, header.getCause());
            }
        }
    }
}