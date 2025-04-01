/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.applicationmap.service;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.common.server.applicationmap.ServiceId.DEFAULT_SERVICE_ID;

/**
 * @author intr3p1d
 */
@Service
@Validated
public class RedisTraceService implements TraceService {
    private final Logger logger = LogManager.getLogger(getClass());

    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 10000);
    private final ApplicationMapService applicationMapService;
    private final ServiceTypeRegistryService registry;


    public RedisTraceService(
            ApplicationMapService applicationMapService,
            ServiceTypeRegistryService registry
    ) {
        this.applicationMapService = Objects.requireNonNull(applicationMapService, "applicationMapService");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        final ServiceType applicationServiceType = getApplicationServiceType(spanChunkBo);
        final List<SpanEventBo> spanEventList = spanChunkBo.getSpanEventBoList();
        if (spanEventList != null) {
            // TODO need to batch update later.
            insertSpanEventList(
                    spanEventList, 
                    applicationServiceType, spanChunkBo.getApplicationId(), spanChunkBo.getAgentId(), 
                    spanChunkBo.getEndPoint(),
                    spanChunkBo.getCollectorAcceptTime()
            );
        }
    }

    @Override
    public void insertSpan(SpanBo spanBo) {
        insertSpanStat(spanBo);
    }

    private ServiceType getApplicationServiceType(SpanChunkBo spanChunk) {
        final short applicationServiceTypeCode = spanChunk.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    private ServiceType getApplicationServiceType(SpanBo span) {
        // Check if applicationServiceType is set. If not, use span's service type.
        final short applicationServiceTypeCode = span.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    private String normalize(String spanEventApplicationName, ServiceType spanEventType) {
        if (spanEventType.getCategory() == ServiceTypeCategory.DATABASE) {
            // empty database id
            if (spanEventApplicationName == null) {
                return "UNKNOWN_DATABASE";
            }
        }
        return spanEventApplicationName;
    }


    private void insertSpanStat(SpanBo span) {
        final ServiceType applicationServiceType = getApplicationServiceType(span);
        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());

        final boolean isError = span.getErrCode() != 0;
        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {
            if (spanServiceType.isQueue()) {
                // create virtual queue node

                applicationMapService.updateBidirectional(
                        span.getCollectorAcceptTime(),
                        DEFAULT_SERVICE_ID,
                        span.getApplicationId(), applicationServiceType,
                        span.getAgentId(),
                        DEFAULT_SERVICE_ID,
                        span.getAcceptorHost(), spanServiceType,
                        span.getEndPoint(),
                        span.getElapsed(), isError
                );

            } else {
                // create virtual user
                // update the span information of the current node (self)
                applicationMapService.updateBidirectional(
                        span.getCollectorAcceptTime(),
                        DEFAULT_SERVICE_ID,
                        span.getApplicationId(), ServiceType.USER,
                        span.getAgentId(),
                        DEFAULT_SERVICE_ID,
                        span.getApplicationId(), applicationServiceType,
                        span.getAgentId(),
                        span.getElapsed(), isError
                );
            }
            bugCheck++;
        }

        // save statistics info only when parentApplicationContext exists
        // when drawing server map based on statistics info, you must know the application name of the previous node.
        if (span.getParentApplicationName() != null) {
            String parentApplicationName = span.getParentApplicationName();
            logger.debug("Received parent application name. {}", parentApplicationName);

            ServiceType parentApplicationType = registry.findServiceType(span.getParentApplicationServiceType());

            // create virtual queue node if current' span's service type is a queue AND :
            // 1. parent node's application service type is not a queue (it may have come from a queue that is traced)
            // 2. current node's application service type is not a queue (current node may be a queue that is traced)
            if (spanServiceType.isQueue()) {
                if (!applicationServiceType.isQueue() && !parentApplicationType.isQueue()) {
                    // emulate virtual queue node's accept Span and record it's acceptor host
                    applicationMapService.updateOutbound(
                            span.getCollectorAcceptTime(),
                            DEFAULT_SERVICE_ID, span.getAcceptorHost(), spanServiceType,
                            DEFAULT_SERVICE_ID, span.getApplicationId(), applicationServiceType,
                            span.getRemoteAddr(), span.getElapsed(), isError
                    );

                    parentApplicationName = span.getAcceptorHost();
                    parentApplicationType = spanServiceType;
                }
            }

            applicationMapService.updateInbound(
                    span.getCollectorAcceptTime(),
                    DEFAULT_SERVICE_ID, parentApplicationName, parentApplicationType,
                    DEFAULT_SERVICE_ID, span.getApplicationId(), applicationServiceType,
                    span.getAgentId(), span.getElapsed(), isError
            );
            bugCheck++;
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.
        applicationMapService.updateSelfResponseTime(
                span.getCollectorAcceptTime(),
                DEFAULT_SERVICE_ID, span.getApplicationId(), applicationServiceType,
                span.getElapsed(), isError
        );

        if (bugCheck != 1) {
            logger.info("ambiguous span found(bug). span:{}", span);
        }
    }


    private void insertSpanEventList(
            List<SpanEventBo> spanEventList,
            ServiceType applicationServiceType,
            String applicationId, String agentId,
            String endPoint,
            long requestTime
    ) {

        for (SpanEventBo spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());

            if (!spanEventType.isRecordStatistics()) {
                continue;
            }

            final String spanEventApplicationName = normalize(spanEvent.getDestinationId(), spanEventType);
            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = spanEvent.hasException();

            if (applicationId == null || spanEventApplicationName == null) {
                throttledLogger.info("Failed to insert statistics. Cause:SpanEvent has invalid format." +
                                "(application:{}/{}[{}], spanEventApplication:{}[{}])",
                        applicationId, agentId, applicationServiceType, spanEventApplicationName, spanEventType);
                continue;
            }

            /*
             * save information to draw a server map based on statistics
             */
            applicationMapService.updateBidirectional(
                    requestTime,
                    DEFAULT_SERVICE_ID, applicationId, applicationServiceType, endPoint,
                    DEFAULT_SERVICE_ID, spanEventApplicationName, spanEventType, agentId,
                    elapsed, hasException
            );
        }
    }

}
