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
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class RequestTraceWriter {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ClientRequestWrapper clientRequestWrapper;

    public RequestTraceWriter(final ClientRequestWrapper clientRequestWrapper) {
        this.clientRequestWrapper = Assert.requireNonNull(clientRequestWrapper, "clientRequestWrapper must not be null");
    }

    public void write() {
        if (isDebug) {
            logger.debug("Set request header that is not to be sampled.");
        }
        this.clientRequestWrapper.setHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
    }

    // Set transaction information in the request.
    public void write(final TraceId traceId, final String applicationName, final short serverTypeCode, final String applicationNamespace) {
        Assert.requireNonNull(traceId, "traceId must not be null");

        if (isDebug) {
            logger.debug("Set request header. traceId={}, applicationName={}, serverTypeCode={}, applicationNamespace={}", traceId, applicationName, serverTypeCode, applicationNamespace);
        }
        this.clientRequestWrapper.setHeader(Header.HTTP_TRACE_ID.toString(), traceId.getTransactionId());
        this.clientRequestWrapper.setHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(traceId.getSpanId()));
        this.clientRequestWrapper.setHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(traceId.getParentSpanId()));
        this.clientRequestWrapper.setHeader(Header.HTTP_FLAGS.toString(), String.valueOf(traceId.getFlags()));
        this.clientRequestWrapper.setHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), applicationName);
        this.clientRequestWrapper.setHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(serverTypeCode));

        if (applicationNamespace != null) {
            this.clientRequestWrapper.setHeader(Header.HTTP_PARENT_APPLICATION_NAMESPACE.toString(), applicationNamespace);
        }

        final String host = this.clientRequestWrapper.getHost();
        if (host != null) {
            this.clientRequestWrapper.setHeader(Header.HTTP_HOST.toString(), host);
        }
    }
}