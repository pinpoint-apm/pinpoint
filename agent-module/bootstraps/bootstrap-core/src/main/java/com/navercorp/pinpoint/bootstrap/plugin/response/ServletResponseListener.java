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

package com.navercorp.pinpoint.bootstrap.plugin.response;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.RequestId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author yjqg6666
 */
public class ServletResponseListener<RESP> {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final ServerResponseHeaderRecorder<RESP> serverResponseHeaderRecorder;
    private final ResponseAdaptor<RESP> responseAdaptor;


    private final boolean responseTraceId;
    private final String responseTraceIdHeaderName;
    private final boolean responseRequestId;
    private final String responseRequestIdHeaderName;

    public ServletResponseListener(final TraceContext traceContext,
                                   final ServerResponseHeaderRecorder<RESP> serverResponseHeaderRecorder,
                                   final ResponseAdaptor<RESP> responseAdaptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.serverResponseHeaderRecorder = Objects.requireNonNull(serverResponseHeaderRecorder, "serverResponseHeaderRecorder");
        this.responseAdaptor = Objects.requireNonNull(responseAdaptor, "responseAdaptor");

        final ProfilerConfig config = traceContext.getProfilerConfig();
        this.responseTraceId = config.readBoolean("profiler.http.response.traceId.enable", false);
        this.responseTraceIdHeaderName = config.readString("profiler.http.response.traceId.headerName", "X-Trace-Id");
        this.responseRequestId = config.readBoolean("profiler.http.response.requestId.enable", false);
        this.responseRequestIdHeaderName = config.readString("profiler.http.response.requestId.headerName", "X-Request-Id");
    }


    public void initialized(RESP response, final ServiceType serviceType, final MethodDescriptor methodDescriptor) {
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(serviceType, "serviceType");
        Objects.requireNonNull(methodDescriptor, "methodDescriptor");

        if (isDebug) {
            // An error may occur when the response variable is output to the log.
            logger.debug("Initialized responseEvent. serviceType={}, methodDescriptor={}", serviceType, methodDescriptor);
        }

        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        if (responseTraceId) {
            setResponseTraceIdHeader(trace, response);
        }
        if (responseRequestId) {
            setResponseRequestIdHeader(trace, response);
        }
    }

    public void destroyed(RESP response, final Throwable throwable, final int statusCode) {
        Objects.requireNonNull(response, "response");

        if (isDebug) {
            // An error may occur when the response variable is output to the log.
            logger.debug("Destroyed responseEvent. throwable={}, statusCode={}", throwable, statusCode);
        }

        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (trace.canSampled()) {
            final SpanRecorder spanRecorder = trace.getSpanRecorder();
            this.serverResponseHeaderRecorder.recordHeader(spanRecorder, response);
        }
    }

    private void setResponseTraceIdHeader(Trace trace, RESP response) {
        if (trace == null || !trace.canSampled()) {
            return;
        }
        TraceId traceId = trace.getTraceId();
        if (traceId == null) {
            return;
        }
        String txId = traceId.getTransactionId();
        try {
            this.responseAdaptor.setHeader(response, this.responseTraceIdHeaderName, txId);
        } catch (Exception e) {
            logger.warn("Set trace id header failed, pTxId:{}", txId, e);
        }
    }

    private void setResponseRequestIdHeader(Trace trace, RESP response) {
        if (trace == null) {
            return;
        }
        final RequestId requestId = trace.getRequestId();
        if (requestId == null || !requestId.isSet()) {
            return;
        }
        final String reqId = requestId.toId();
        try {
            this.responseAdaptor.setHeader(response, this.responseRequestIdHeaderName, reqId);
        } catch (Exception e) {
            logger.warn("Set request id header failed, pReqId:{}", reqId, e);
        }
    }
}