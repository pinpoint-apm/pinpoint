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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class RequestTraceReader {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final boolean async;
    private final String applicationNamespace;


    public RequestTraceReader(final TraceContext traceContext) {
        this(traceContext, false);
    }

    public RequestTraceReader(final TraceContext traceContext, final boolean async) {
        this.traceContext = Assert.requireNonNull(traceContext, "traceContext must not be null");
        this.async = async;
        this.applicationNamespace = traceContext.getProfilerConfig().getApplicationNamespace();
    }

    // Read the transaction information from the request.
    public Trace read(final ServerRequestWrapper serverRequestWrapper) {
        Assert.requireNonNull(serverRequestWrapper, "serverRequestWrapper must not be n ull");

        // Check sampling flag from client. If the flag is false, do not sample this request.
        final boolean sampling = samplingEnable(serverRequestWrapper);
        if (!sampling) {
            // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
            // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'
            final Trace trace = this.traceContext.disableSampling();
            if (isDebug) {
                logger.debug("Remote call sampling flag found. skip trace requestUrl:{}, remoteAddr:{}", serverRequestWrapper.getRpcName(), serverRequestWrapper.getRemoteAddress());
            }
            return trace;
        }

        final TraceId traceId = populateTraceIdFromRequest(serverRequestWrapper);
        if (traceId != null) {
            // TODO Maybe we should decide to trace or not even if the sampling flag is true to prevent too many requests are traced.
            final Trace trace = continueTrace(traceId);
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID exist. continue trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, serverRequestWrapper.getRpcName(), serverRequestWrapper.getRemoteAddress());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID exist. camSampled is false. skip trace. traceId:{}, requestUrl:{}, remoteAddr:{}", traceId, serverRequestWrapper.getRpcName(), serverRequestWrapper.getRemoteAddress());
                }
            }
            return trace;
        } else {
            final Trace trace = newTrace();
            if (trace.canSampled()) {
                if (isDebug) {
                    logger.debug("TraceID not exist. start new trace. requestUrl:{}, remoteAddr:{}", serverRequestWrapper.getRpcName(), serverRequestWrapper.getRemoteAddress());
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceID not exist. camSampled is false. skip trace. requestUrl:{}, remoteAddr:{}", serverRequestWrapper.getRpcName(), serverRequestWrapper.getRemoteAddress());
                }
            }
            return trace;
        }
    }

    private boolean samplingEnable(final ServerRequestWrapper serverRequestWrapper) {
        final String samplingFlag = serverRequestWrapper.getHeader(Header.HTTP_SAMPLED.toString());
        if (isDebug) {
            logger.debug("SamplingFlag={}", samplingFlag);
        }

        return SamplingFlagUtils.isSamplingFlag(samplingFlag);
    }

    private TraceId populateTraceIdFromRequest(final ServerRequestWrapper serverRequestWrapper) {
        final String parentApplicationNamespace = serverRequestWrapper.getHeader(Header.HTTP_PARENT_APPLICATION_NAMESPACE.toString());
        // If parentApplicationNamespace is null, it is ignored for backwards compatibility.
        if (parentApplicationNamespace != null) {
            if (!this.applicationNamespace.equals(parentApplicationNamespace)) {
                // collision.
                if (isDebug) {
                    logger.debug("Collision namespace. applicationNamespace={}, parentApplicationNamespace={}", this.applicationNamespace, parentApplicationNamespace);
                }
                return null;
            }
        }

        final String transactionId = serverRequestWrapper.getHeader(Header.HTTP_TRACE_ID.toString());
        if (transactionId != null) {
            final long parentSpanId = NumberUtils.parseLong(serverRequestWrapper.getHeader(Header.HTTP_PARENT_SPAN_ID.toString()), SpanId.NULL);
            final long spanId = NumberUtils.parseLong(serverRequestWrapper.getHeader(Header.HTTP_SPAN_ID.toString()), SpanId.NULL);
            final short flags = NumberUtils.parseShort(serverRequestWrapper.getHeader(Header.HTTP_FLAGS.toString()), (short) 0);
            final TraceId id = this.traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
            return id;
        }
        return null;
    }

    private Trace continueTrace(final TraceId traceId) {
        if (this.async) {
            return this.traceContext.continueAsyncTraceObject(traceId);
        }
        return this.traceContext.continueTraceObject(traceId);
    }

    private Trace newTrace() {
        if (this.async) {
            return this.traceContext.newAsyncTraceObject();
        }
        return this.traceContext.newTraceObject();
    }
}