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
import com.navercorp.pinpoint.collector.sampler.Sampler;
import com.navercorp.pinpoint.collector.sampler.SpanSamplerFactory;
import com.navercorp.pinpoint.collector.service.ApplicationInfoService;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.hbase.RequestNotPermittedException;
import com.navercorp.pinpoint.common.profiler.logging.LogSampler;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.grpc.BindAttribute;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
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
    private final LogSampler infoLog = new LogSampler(1000);
    private final LogSampler warnLog = new LogSampler(100);
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceService[] traceServices;

    private final GrpcSpanFactory spanFactory;

    private final AcceptedTimeService acceptedTimeService;
    private final ApplicationInfoService applicationInfoService;

    private final Sampler<BasicSpan> sampler;

    public GrpcSpanHandler(
            TraceService[] traceServices,
            GrpcSpanFactory spanFactory,
            AcceptedTimeService acceptedTimeService,
            SpanSamplerFactory spanSamplerFactory,
            ApplicationInfoService applicationInfoService) {
        this.traceServices = Objects.requireNonNull(traceServices, "traceServices");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
        this.sampler = spanSamplerFactory.createBasicSpanSampler();

        logger.info("TraceServices {}", Arrays.toString(traceServices));
    }

    @Override
    public void handleSimple(ServerRequest<GeneratedMessageV3> serverRequest) {
        final GeneratedMessageV3 data = serverRequest.getData();
        if (data instanceof PSpan span) {
            handleSpan(span);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }

    private void handleSpan(PSpan span) {
        if (isDebug) {
            logger.debug("Handle PSpan={}", createSimpleSpanLog(span));
        }

        final Header header = ServerContext.getAgentInfo();
        final ApplicationId applicationId = this.applicationInfoService.getApplicationId(header.getApplicationName());
        final BindAttribute attribute = BindAttribute.of(header, applicationId, acceptedTimeService.getAcceptedTime());
        final SpanBo spanBo = spanFactory.buildSpanBo(span, attribute);
        if (!sampler.isSampling(spanBo)) {
            if (isDebug) {
                logger.debug("unsampled PSpan={}", createSimpleSpanLog(span));
            } else {
                infoLog.log(() -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("unsampled PSpan={}", createSimpleSpanLog(span));
                    }
                });
            }
            return;
        }
        for (TraceService traceService : traceServices) {
            try {
                traceService.insertSpan(spanBo);
            } catch (RequestNotPermittedException notPermitted) {
                warnLog.log((c) -> logger.warn("Failed to handle Span RequestNotPermitted:{} {}", notPermitted.getMessage(), c));
            } catch (Throwable e) {
                logger.warn("Failed to handle Span={}", MessageFormatUtils.debugLog(span), e);
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