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
import com.navercorp.pinpoint.bootstrap.context.RequestId;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * @author jaehong.kim
 */
public class RequestIdReader<T> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final RequestAdaptor<T> requestAdaptor;
    private final boolean enabled;
    private final String externalHeader;
    private final boolean externalHeaderEnabled;


    public RequestIdReader(final TraceContext traceContext, RequestAdaptor<T> requestAdaptor) {
        this.requestAdaptor = Objects.requireNonNull(requestAdaptor, "requestAdaptor");
        this.enabled = traceContext.getProfilerConfig().readBoolean("profiler.requestId.enable", false);
        this.externalHeader = traceContext.getProfilerConfig().readString("profiler.requestId.externalHeader", null);
        this.externalHeaderEnabled = StringUtils.hasText(this.externalHeader);
    }


    /**
     * Read the Request ID information from the request.
     *
     * @param request request
     * @return request id
     */
    public RequestId read(T request) {
        Objects.requireNonNull(request, "request");
        if (!enabled) {
            logger.debug("Config profiler.requestId.enabled is disabled, skip");
            return new DefaultRequestId();
        }

        String requestId = requestAdaptor.getHeader(request, Header.HTTP_REQUEST_ID.toString());
        if (isDebug) {
            logger.debug("Get request id {} from request header.", requestId);
        }
        if (externalHeaderEnabled && !StringUtils.hasText(requestId)) {
            requestId = requestAdaptor.getHeader(request, externalHeader);
            if (isDebug) {
                logger.debug("Get request id {} from external request header:{}.", requestId, externalHeader);
            }
        }
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString();
            if (isDebug) {
                logger.debug("Generate a request id {} for the request.", requestId);
            }
        }
        return new DefaultRequestId(requestId);
    }

}