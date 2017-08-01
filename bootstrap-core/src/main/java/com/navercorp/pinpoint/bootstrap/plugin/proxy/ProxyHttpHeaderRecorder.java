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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderRecorder {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final boolean isInfo = logger.isInfoEnabled();
    private final ProxyHttpHeaderParser parser = new ProxyHttpHeaderParser();
    private final TraceContext traceContext;
    private final boolean enable;
    private final List<String> httpHeaderNames;
    private final boolean httpHeaderHidden;

    public ProxyHttpHeaderRecorder(final TraceContext traceContext) {
        Assert.requireNonNull(traceContext, "traceContext must not be null");
        this.traceContext = traceContext;
        final ProfilerConfig config = traceContext.getProfilerConfig();
        this.enable = config.isProxyHttpHeaderEnable();
        this.httpHeaderNames = config.getProxyHttpHeaderNames();
        this.httpHeaderHidden = config.isProxyHttpHeaderHidden();
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
            for (String name : this.httpHeaderNames) {
                final String value = handler.read(name);
                if (value == null || value.isEmpty()) {
                    continue;
                }

                if (this.httpHeaderHidden) {
                    handler.remove(name);
                    if (isDebug) {
                        logger.debug("Remove proxy http header. name={}, value={}", name, value);
                    }
                }

                final ProxyHttpHeader header = this.parser.parse(value);
                if (header.isValid()) {
                    final int cacheId = this.traceContext.cacheString(name);
                    header.setName(cacheId);
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
        } catch (Exception e) {
            // for handler operations.
            if (isInfo) {
                logger.info("Failed to record proxy http header. cause={}", e.getMessage());
            }
        }
    }
}