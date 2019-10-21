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
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class DefaultTraceHeaderReader<T> implements TraceHeaderReader<T> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final RequestAdaptor<T> requestAdaptor;


    public DefaultTraceHeaderReader(RequestAdaptor<T> requestAdaptor) {
        this.requestAdaptor = Assert.requireNonNull(requestAdaptor, "requestAdaptor");
    }

    // Read the transaction information from the request.
    @Override
    public TraceHeader read(T request) {
        Assert.requireNonNull(request, "request");

        // Check sampling flag from client. If the flag is false, do not sample this request.
        final boolean sampling = samplingEnable(request);
        if (!sampling) {
            return DisableTraceHeader.INSTANCE;
        }

        final String transactionId = requestAdaptor.getHeader(request, Header.HTTP_TRACE_ID.toString());
        // TODO miss validation check
        if (transactionId == null) {
            return NewTraceHeader.INSTANCE;
        }
        final String parentSpanIdStr = requestAdaptor.getHeader(request, Header.HTTP_PARENT_SPAN_ID.toString());
        if (parentSpanIdStr == null) {
            return NewTraceHeader.INSTANCE;
        }
        final long parentSpanId = NumberUtils.parseLong(parentSpanIdStr, SpanId.NULL);
//        if (parentSpanId == SpanId.NULL) {
//            throw new IllegalArgumentException();
//        }
        final String spanIdStr = requestAdaptor.getHeader(request, Header.HTTP_SPAN_ID.toString());
        if (spanIdStr == null) {
            return NewTraceHeader.INSTANCE;
        }
        final long spanId = NumberUtils.parseLong(spanIdStr, SpanId.NULL);
//        if (spanId  == SpanId.NULL) {
//            throw new IllegalArgumentException();
//        }
        final short flags = NumberUtils.parseShort(requestAdaptor.getHeader(request, Header.HTTP_FLAGS.toString()), (short) 0);
        return new ContinueTraceHeader(transactionId, parentSpanId, spanId, flags);
    }

    private boolean samplingEnable(final T request) {
        final String samplingFlag = requestAdaptor.getHeader(request, Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag={}", samplingFlag);
        }

        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

}