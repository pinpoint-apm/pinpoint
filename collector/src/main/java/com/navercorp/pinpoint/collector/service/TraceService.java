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
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TraceService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TraceDao traceDao;

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final StatisticsService statisticsService;

    private final ServiceTypeRegistryService registry;

    public TraceService(TraceDao traceDao, ApplicationTraceIndexDao applicationTraceIndexDao, HostApplicationMapDao hostApplicationMapDao,
                        StatisticsService statisticsService, ServiceTypeRegistryService registry) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public void insertSpanChunk(final SpanChunkBo spanChunkBo) {
        traceDao.insertSpanChunk(spanChunkBo);
        final ServiceType applicationServiceType = getApplicationServiceType(spanChunkBo);
        final List<SpanEventBo> spanEventList = spanChunkBo.getSpanEventBoList();
        if (spanEventList != null) {
            // TODO need to batch update later.
            insertSpanEventList(spanEventList, applicationServiceType, spanChunkBo.getApplicationId(), spanChunkBo.getAgentId(), spanChunkBo.getEndPoint());
        }
    }

    private ServiceType getApplicationServiceType(SpanChunkBo spanChunk) {
        final short applicationServiceTypeCode = spanChunk.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    public void insertSpan(final SpanBo spanBo) {
        traceDao.insert(spanBo);
        applicationTraceIndexDao.insert(spanBo);
        insertAcceptorHost(spanBo);
        insertSpanStat(spanBo);
        insertSpanEventStat(spanBo);
    }

    private void insertAcceptorHost(SpanEventBo spanEvent, String applicationId, ServiceType serviceType) {
        final String endPoint = spanEvent.getEndPoint();
        if (endPoint == null) {
            logger.debug("endPoint is null. spanEvent:{}", spanEvent);
            return;
        }
        final String destinationId = spanEvent.getDestinationId();
        if (destinationId == null) {
            logger.debug("destinationId is null. spanEvent:{}", spanEvent);
            return;
        }
        hostApplicationMapDao.insert(endPoint, destinationId, spanEvent.getServiceType(), applicationId, serviceType.getCode());
    }

    private void insertAcceptorHost(SpanBo span) {
        // save host application map
        // acceptor host is set at profiler module only when the span is not the kind of root span
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
            logger.debug("acceptorHost is null {}", span);
            return;
        }
        final String spanApplicationName = span.getApplicationId();
        final short applicationServiceTypeCode = getApplicationServiceType(span).getCode();

        final String parentApplicationName = span.getParentApplicationId();
        final short parentServiceType = span.getParentApplicationServiceType();

        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());
        if (spanServiceType.isQueue()) {
            hostApplicationMapDao.insert(span.getEndPoint(), spanApplicationName, applicationServiceTypeCode, parentApplicationName, parentServiceType);
        } else {
            hostApplicationMapDao.insert(acceptorHost, spanApplicationName, applicationServiceTypeCode, parentApplicationName, parentServiceType);
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
                statisticsService.updateCaller(span.getAcceptorHost(), spanServiceType, span.getRemoteAddr(), span.getApplicationId(), applicationServiceType, span.getEndPoint(), span.getElapsed(), isError);

                statisticsService.updateCallee(span.getApplicationId(), applicationServiceType, span.getAcceptorHost(), spanServiceType, span.getAgentId(), span.getElapsed(), isError);
            } else {
                // create virtual user
                statisticsService.updateCaller(span.getApplicationId(), ServiceType.USER, span.getAgentId(), span.getApplicationId(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

                // update the span information of the current node (self)
                statisticsService.updateCallee(span.getApplicationId(), applicationServiceType, span.getApplicationId(), ServiceType.USER, span.getAgentId(), span.getElapsed(), isError);
            }
            bugCheck++;
        }

        // save statistics info only when parentApplicationContext exists
        // when drawing server map based on statistics info, you must know the application name of the previous node.
        if (span.getParentApplicationId() != null) {
            String parentApplicationName = span.getParentApplicationId();
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
                    statisticsService.updateCaller(span.getAcceptorHost(), spanServiceType, span.getRemoteAddr(), span.getApplicationId(), applicationServiceType, span.getEndPoint(), span.getElapsed(), isError);

                    parentApplicationName = span.getAcceptorHost();
                    parentApplicationType = spanServiceType;
                }
            }

            statisticsService.updateCallee(span.getApplicationId(), applicationServiceType, parentApplicationName, parentApplicationType, span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.

        statisticsService.updateResponseTime(span.getApplicationId(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

        if (bugCheck != 1) {
            logger.warn("ambiguous span found(bug). span:{}", span);
        }
    }

    private void insertSpanEventStat(SpanBo span) {

        final List<SpanEventBo> spanEventList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("handle spanEvent size:{}", spanEventList.size());
        }

        final ServiceType applicationServiceType = getApplicationServiceType(span);
        // TODO need to batch update later.
        insertSpanEventList(spanEventList, applicationServiceType, span.getApplicationId(), span.getAgentId(), span.getEndPoint());
    }

    private void insertSpanEventList(List<SpanEventBo> spanEventList, ServiceType applicationServiceType, String applicationId, String agentId, String endPoint) {

        for (SpanEventBo spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());

            if (isAlias(spanEventType, spanEvent)) {
                insertAcceptorHost(spanEvent, applicationId, applicationServiceType);
                continue;
            }

            if (!spanEventType.isRecordStatistics()) {
                continue;
            }

            final String spanEventApplicationName = spanEvent.getDestinationId();
            final String spanEventEndPoint = spanEvent.getEndPoint();

            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = spanEvent.hasException();

            if (applicationId == null || spanEventApplicationName == null) {
                logger.warn("Failed to insert statistics. Cause:SpanEvent has invalid format.(application:{}/{}[{}], spanEventApplication:{}[{}])",
                        applicationId, agentId, applicationServiceType, spanEventApplicationName, spanEventType);
                continue;
            }

            /*
             * save information to draw a server map based on statistics
             */
            // save the information of caller (the spanevent that called span)
            statisticsService.updateCaller(applicationId, applicationServiceType, agentId, spanEventApplicationName, spanEventType, spanEventEndPoint, elapsed, hasException);

            // save the information of callee (the span that spanevent called)
            statisticsService.updateCallee(spanEventApplicationName, spanEventType, applicationId, applicationServiceType, endPoint, elapsed, hasException);
        }
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