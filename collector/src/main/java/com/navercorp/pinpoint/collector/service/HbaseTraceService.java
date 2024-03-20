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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.event.SpanStorePublisher;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.event.SpanChunkInsertEvent;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import jakarta.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Validated
public class HbaseTraceService implements TraceService {
    private final Logger logger = LogManager.getLogger(getClass());

    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 10000);

    private final TraceDao traceDao;

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final StatisticsService statisticsService;

    private final ServiceTypeRegistryService registry;

    private final SpanStorePublisher publisher;
    private final Executor grpcSpanServerExecutor;

    public HbaseTraceService(TraceDao traceDao,
                             ApplicationTraceIndexDao applicationTraceIndexDao,
                             HostApplicationMapDao hostApplicationMapDao,
                             StatisticsService statisticsService,
                             ServiceTypeRegistryService registry,
                             SpanStorePublisher spanStorePublisher,
                             @Qualifier("grpcSpanServerExecutor") Executor grpcSpanServerExecutor) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.publisher = Objects.requireNonNull(spanStorePublisher, "spanStorePublisher");
        this.grpcSpanServerExecutor = Objects.requireNonNull(grpcSpanServerExecutor, "grpcSpanServerExecutor");
    }

    @Override
    public void insertSpanChunk(@Valid final SpanChunkBo spanChunkBo) {
        SpanChunkInsertEvent event = publisher.captureContext(spanChunkBo);
        traceDao.insertSpanChunk(spanChunkBo);
        final ServiceType applicationServiceType = getApplicationServiceType(spanChunkBo);
        final List<SpanEventBo> spanEventList = spanChunkBo.getSpanEventBoList();
        if (spanEventList != null) {
            // TODO need to batch update later.
            insertSpanEventList(spanEventList, applicationServiceType, spanChunkBo.getApplicationName(), spanChunkBo.getAgentId(), spanChunkBo.getEndPoint());
        }

        // TODO should be able to tell whether the span chunk is successfully inserted
        publisher.publishEvent(event, true);
    }

    private ServiceType getApplicationServiceType(SpanChunkBo spanChunk) {
        final short applicationServiceTypeCode = spanChunk.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    @Override
    public void insertSpan(@Valid final SpanBo spanBo) {
        SpanInsertEvent event = publisher.captureContext(spanBo);
        CompletableFuture<Void> future = traceDao.asyncInsert(spanBo);
        applicationTraceIndexDao.insert(spanBo);
        insertAcceptorHost(spanBo);
        insertSpanStat(spanBo);
        insertSpanEventStat(spanBo);

        future.whenCompleteAsync((unused, throwable) -> {
            final boolean result = throwable == null;
            logger.trace("success {}", result);
            publisher.publishEvent(event, result);
        }, grpcSpanServerExecutor);
    }

    private void insertAcceptorHost(SpanEventBo spanEvent, String applicationName, ServiceType serviceType) {
        final String endPoint = spanEvent.getEndPoint();
        if (endPoint == null) {
            logger.debug("endPoint is null. spanEvent:{}", spanEvent);
            return;
        }
        final String destinationAppName = spanEvent.getDestinationId();
        if (destinationAppName == null) {
            logger.debug("destinationId is null. spanEvent:{}", spanEvent);
            return;
        }
        hostApplicationMapDao.insert(endPoint, destinationAppName, spanEvent.getServiceType(), applicationName, serviceType.getCode());
    }

    private void insertAcceptorHost(SpanBo span) {
        // save host application map
        // acceptor host is set at profiler module only when the span is not the kind of root span
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
            logger.debug("acceptorHost is null {}", span);
            return;
        }
        final short applicationServiceTypeCode = getApplicationServiceType(span).getCode();

        final String parentApplicationName = span.getParentApplicationName();
        final short parentServiceType = span.getParentApplicationServiceType();

        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());
        if (spanServiceType.isQueue()) {
            hostApplicationMapDao.insert(span.getEndPoint(), span.getApplicationName(), applicationServiceTypeCode, parentApplicationName, parentServiceType);
        } else {
            hostApplicationMapDao.insert(acceptorHost, span.getApplicationName(), applicationServiceTypeCode, parentApplicationName, parentServiceType);
        }
    }

    private ServiceType getApplicationServiceType(SpanBo span) {
        // Check if applicationServiceType is set. If not, use span's service type.
        final short applicationServiceTypeCode = span.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    private void insertSpanStat(SpanBo span) {
        final ServiceType applicationServiceType = getApplicationServiceType(span);
        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());

        final boolean isError = span.getErrCode() != 0;
        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {
            if (spanServiceType.isQueue()) {
                // create virtual queue node
                statisticsService.updateCaller(span.getAcceptorHost(), spanServiceType, span.getRemoteAddr(), span.getApplicationName(), applicationServiceType, span.getEndPoint(), span.getElapsed(), isError);

                statisticsService.updateCallee(span.getApplicationName(), applicationServiceType, span.getAcceptorHost(), spanServiceType, span.getAgentId(), span.getElapsed(), isError);
            } else {
                // create virtual user
                statisticsService.updateCaller(span.getApplicationName(), ServiceType.USER, span.getAgentId(), span.getApplicationName(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

                // update the span information of the current node (self)
                statisticsService.updateCallee(span.getApplicationName(), applicationServiceType, span.getApplicationName(), ServiceType.USER, span.getAgentId(), span.getElapsed(), isError);
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
                    hostApplicationMapDao.insert(span.getRemoteAddr(), span.getAcceptorHost(), spanServiceType.getCode(), parentApplicationName, parentApplicationType.getCode());
                    // emulate virtual queue node's send SpanEvent
                    statisticsService.updateCaller(span.getAcceptorHost(), spanServiceType, span.getRemoteAddr(), span.getApplicationName(), applicationServiceType, span.getEndPoint(), span.getElapsed(), isError);

                    parentApplicationType = spanServiceType;
                }
            }

            statisticsService.updateCallee(span.getApplicationName(), applicationServiceType, parentApplicationName, parentApplicationType, span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.

        statisticsService.updateResponseTime(span.getApplicationName(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

        if (bugCheck != 1) {
            logger.info("ambiguous span found(bug). span:{}", span);
        }
    }

    private void insertSpanEventStat(SpanBo span) {

        final List<SpanEventBo> spanEventList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("handle spanEvent {}/{} size:{}", span.getApplicationName(), span.getAgentId(), spanEventList.size());
        }

        final ServiceType applicationServiceType = getApplicationServiceType(span);
        // TODO need to batch update later.
        insertSpanEventList(spanEventList, applicationServiceType, span.getApplicationName(), span.getAgentId(), span.getEndPoint());
    }

    private void insertSpanEventList(List<SpanEventBo> spanEventList, ServiceType applicationServiceType, String applicationName, String agentId, String endPoint) {

        for (SpanEventBo spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());

            if (isAlias(spanEventType, spanEvent)) {
                insertAcceptorHost(spanEvent, applicationName, applicationServiceType);
                continue;
            }

            if (!spanEventType.isRecordStatistics()) {
                continue;
            }

            final String spanEventApplicationName = normalize(spanEvent.getDestinationId(), spanEventType);
            final String spanEventEndPoint = spanEvent.getEndPoint();

            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = spanEvent.hasException();

            if (applicationName == null || spanEventApplicationName == null) {
                throttledLogger.info("Failed to insert statistics. Cause:SpanEvent has invalid format." +
                                "(application:{}/{}[{}], spanEventApplication:{}[{}])",
                        applicationName, agentId, applicationServiceType, spanEventApplicationName, spanEventType);
                continue;
            }

            /*
             * save information to draw a server map based on statistics
             */
            // save the information of caller (the spanevent that called span)
            statisticsService.updateCaller(applicationName, applicationServiceType, agentId, spanEventApplicationName, spanEventType, spanEventEndPoint, elapsed, hasException);

            // save the information of callee (the span that spanevent called)
            statisticsService.updateCallee(spanEventApplicationName, spanEventType, applicationName, applicationServiceType, endPoint, elapsed, hasException);
        }
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

    private boolean isAlias(ServiceType spanEventType, SpanEventBo forDebugEvent) {
        if (!spanEventType.isAlias()) {
            return false;
        }
        if (spanEventType.isRecordStatistics()) {
            logger.error("ServiceType with ALIAS should NOT have RECORD_STATISTICS {}", forDebugEvent);
            return false;
        }
        return true;
    }
}