/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.request.ServerRequest;

import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class GrpcSpanHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceService traceService;

    private final GrpcSpanFactory spanFactory;

    @Autowired
    public GrpcSpanHandler(TraceService traceService, GrpcSpanFactory spanFactory) {
        this.traceService = Objects.requireNonNull(traceService, "traceService");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof PSpan) {
            handleSpan((PSpan) data);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private void handleSpan(PSpan span) {
        if (isDebug) {
            logger.debug("Handle PSpan={}", createSimpleSpanLog(span));
        }

        try {
            Header agentInfo = ServerContext.getAgentInfo();
            final SpanBo spanBo = spanFactory.buildSpanBo(span, agentInfo);
            traceService.insertSpan(spanBo);
        } catch (Exception e) {
            logger.warn("Failed to handle span={}", MessageFormatUtils.debugLog(span), e);
        }
    }

    private String createSimpleSpanLog(PSpan span) {
        if (!isDebug) {
            return "";
        }

        StringBuilder log = new StringBuilder();

        PTransactionId transactionId = span.getTransactionId();
        log.append(" transactionId:");
        log.append(MessageFormatUtils.debugLog(transactionId));

        log.append(" spanId:").append(span.getSpanId());

        StringBuilder spanEventSequenceLog = new StringBuilder();
        List<PSpanEvent> spanEventList = span.getSpanEventList();
        for (PSpanEvent pSpanEvent : spanEventList) {
            if (pSpanEvent == null) {
                continue;
            }
            spanEventSequenceLog.append(pSpanEvent.getSequence()).append(" ");
        }

        log.append(" spanEventSequence:").append(spanEventSequenceLog.toString());

        return log.toString();
    }

}