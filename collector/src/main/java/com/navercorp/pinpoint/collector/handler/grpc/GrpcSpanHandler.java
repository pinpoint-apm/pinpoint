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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class GrpcSpanHandler implements SimpleHandler<GeneratedMessageV3> {

    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceService[] traceServices;

    private final GrpcSpanFactory spanFactory;

    public GrpcSpanHandler(TraceService[] traceServices, GrpcSpanFactory spanFactory) {
        this.traceServices = Objects.requireNonNull(traceServices, "traceServices");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");

        logger.info("TraceServices {}", Arrays.toString(traceServices));
    }

    @Override
    public void handleSimple(ServerRequest<GeneratedMessageV3> serverRequest) {
        final GeneratedMessageV3 data = serverRequest.getData();
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

        final Header agentInfo = ServerContext.getAgentInfo();
        final SpanBo spanBo = spanFactory.buildSpanBo(span, agentInfo);
        for (TraceService traceService : traceServices) {
            try {
                traceService.insertSpan(spanBo);
            } catch (Throwable e) {
                logger.warn("Failed to handle span={}", MessageFormatUtils.debugLog(span), e);
            }
        }
    }

    private String createSimpleSpanLog(PSpan span) {
        if (!isDebug) {
            return "";
        }

        StringBuilder log = new StringBuilder(64);

        PTransactionId transactionId = span.getTransactionId();
        log.append(" transactionId:");
        log.append(MessageFormatUtils.debugLog(transactionId));

        log.append(" spanId:").append(span.getSpanId());

        final List<PSpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            log.append(" spanEventSequence:");
            for (PSpanEvent pSpanEvent : spanEventList) {
                if (pSpanEvent == null) {
                    continue;
                }
                log.append(pSpanEvent.getSequence()).append(" ");
            }
        }

        return log.toString();
    }

}