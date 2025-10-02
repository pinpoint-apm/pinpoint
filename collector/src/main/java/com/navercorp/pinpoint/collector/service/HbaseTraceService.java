/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.applicationmap.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.applicationmap.service.LinkService;
import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.event.SpanStorePublisher;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.event.SpanChunkInsertEvent;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Trace service implementation for HBase storage.
 */
@Service
public class HbaseTraceService implements TraceService {
    private final Logger logger = LogManager.getLogger(getClass());

    private static final String MERGE_AGENT = "_";
    private static final String MERGE_QUEUE = "_";

    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 10000);

    private final TraceDao traceDao;

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final HostApplicationMapDao hostApplicationMapDao;

    private final LinkService linkService;

    private final ServiceTypeRegistryService registry;

    private final SpanStorePublisher publisher;
    private final Executor grpcSpanServerExecutor;

    public HbaseTraceService(TraceDao traceDao,
                             ApplicationTraceIndexDao applicationTraceIndexDao,
                             HostApplicationMapDao hostApplicationMapDao,
                             LinkService linkService,
                             ServiceTypeRegistryService registry,
                             SpanStorePublisher spanStorePublisher,
                             @Qualifier("grpcSpanServerExecutor") Executor grpcSpanServerExecutor) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.hostApplicationMapDao = Objects.requireNonNull(hostApplicationMapDao, "hostApplicationMapDao");
        this.linkService = Objects.requireNonNull(linkService, "statisticsService");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.publisher = Objects.requireNonNull(spanStorePublisher, "spanStorePublisher");
        this.grpcSpanServerExecutor = Objects.requireNonNull(grpcSpanServerExecutor, "grpcSpanServerExecutor");
    }

    @Override
    public void insertSpanChunk(final SpanChunkBo spanChunkBo) {
        SpanChunkInsertEvent event = publisher.captureContext(spanChunkBo);
        traceDao.insertSpanChunk(spanChunkBo);

        Vertex selfVertex = getSelfVertex(spanChunkBo);

        final List<SpanEventBo> spanEventList = spanChunkBo.getSpanEventBoList();
        if (spanEventList != null) {
            // TODO need to batch update later.
            insertSpanEventList(spanEventList, selfVertex, spanChunkBo.getAgentId(), spanChunkBo.getEndPoint(), spanChunkBo.getCollectorAcceptTime());
        }

        // TODO should be able to tell whether the span chunk is successfully inserted
        publisher.publishEvent(event, true);
    }

    private Vertex getSelfVertex(BasicSpan basicSpan) {
        final ServiceType applicationServiceType = getApplicationServiceType(basicSpan);
        return Vertex.of(basicSpan.getApplicationName(), applicationServiceType);
    }

    private ServiceType getApplicationServiceType(BasicSpan basicSpan) {
        final int applicationServiceTypeCode = basicSpan.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    @Override
    public void insertSpan(final SpanBo spanBo) {
        SpanInsertEvent event = publisher.captureContext(spanBo);
        CompletableFuture<Void> future = traceDao.asyncInsert(spanBo);
        applicationTraceIndexDao.insert(spanBo);

        final Vertex selfVertex = getSelfVertex(spanBo);

        insertAcceptorHost(spanBo, selfVertex);
        insertSpanStat(spanBo, selfVertex);
        insertSpanEventStat(spanBo, selfVertex);

        future.whenCompleteAsync((unused, throwable) -> {
            final boolean result = throwable == null;
            if (logger.isTraceEnabled()) {
                logger.trace("success {}", result);
            }
            publisher.publishEvent(event, result);
        }, grpcSpanServerExecutor);
    }

    private void insertAcceptorHost(long requestTime, SpanEventBo spanEvent, Vertex selfVertex) {
        final String endPoint = spanEvent.getEndPoint();
        if (endPoint == null) {
            logger.debug("endPoint is null. appName:{} spanEvent:{}", selfVertex, spanEvent);
            return;
        }
        final String destinationId = spanEvent.getDestinationId();
        if (destinationId == null) {
            logger.debug("destinationId is null. appName:{} spanEvent:{}", selfVertex, spanEvent);
            return;
        }
        ServiceType serviceType = registry.findServiceType(spanEvent.getServiceType());
        Vertex rpcVertex = Vertex.of(destinationId, serviceType);
        hostApplicationMapDao.insert(requestTime, selfVertex.applicationName(), selfVertex.serviceType().getCode(), rpcVertex, endPoint);
    }

    private void insertAcceptorHost(SpanBo span, Vertex selfVertex) {
        // save host application map
        // acceptor host is set at profiler module only when the span is not the kind of root span
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
            logger.debug("acceptorHost is null agent: {}/{}", span.getApplicationName(), span.getAgentName());
            return;
        }

        final String parentApplicationName = span.getParentApplicationName();
        if (parentApplicationName == null) {
            logger.debug("parentApplicationName is null agent: {}/{}", span.getApplicationName(), span.getAgentName());
            return;
        }
        final int parentServiceType = span.getParentApplicationServiceType();
        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());
        if (spanServiceType.isQueue()) {
            final String host = span.getEndPoint();
            if (host == null) {
                logger.debug("endPoint is null agent: {}/{}", span.getApplicationName(), span.getAgentName());
                return;
            }
            hostApplicationMapDao.insert(span.getCollectorAcceptTime(), parentApplicationName, parentServiceType, selfVertex, host);
        } else {
            hostApplicationMapDao.insert(span.getCollectorAcceptTime(), parentApplicationName, parentServiceType, selfVertex, acceptorHost);
        }
    }

    private Vertex getParentVertex(SpanBo span) {
        String parentApplicationName = span.getParentApplicationName();
        ServiceType parentApplicationType = registry.findServiceType(span.getParentApplicationServiceType());
        return Vertex.of(parentApplicationName, parentApplicationType);
    }

    private void insertSpanStat(SpanBo span, Vertex selfVertex) {

        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());

        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {
            if (spanServiceType.isQueue()) {
                // create virtual queue node
                String applicationName = span.getAcceptorHost();
                if (applicationName == null) {
                    applicationName = span.getRemoteAddr();
                }
                Vertex acceptVertex = Vertex.of(applicationName, spanServiceType);
                linkService.updateOutLink(span.getCollectorAcceptTime(), acceptVertex, span.getRemoteAddr(),
                        selfVertex, MERGE_QUEUE, span.getElapsed(), span.hasError());

                if (logger.isDebugEnabled()) {
                    logger.debug("[InLink] root-queue {} <- {}/{}", selfVertex, acceptVertex, span.getAgentId());
                }
                linkService.updateInLink(span.getCollectorAcceptTime(), selfVertex,
                        acceptVertex, MERGE_QUEUE, span.getElapsed(), span.hasError());
            } else {
                // create virtual user
//                linkService.updateOutLink(span.getCollectorAcceptTime(), Link.of(span.getApplicationName(), ServiceType.USER), MERGE_AGENT,
//                        spanLink, MERGE_AGENT, span.getElapsed(), span.hasError());

                // update the span information of the current node (self)
                Vertex userVertex = Vertex.of(span.getApplicationName(), ServiceType.USER);
                linkService.updateInLink(span.getCollectorAcceptTime(), selfVertex, userVertex, MERGE_AGENT, span.getElapsed(), span.hasError());
            }
            bugCheck++;
        }

        // save statistics info only when parentApplicationContext exists
        // when drawing server map based on statistics info, you must know the application name of the previous node.
        if (span.getParentApplicationName() != null) {

            Vertex parentVertex = getParentVertex(span);
            logger.debug("Received parent application name. parentName:{} appName:{}", parentVertex, span.getApplicationName());

            // create virtual queue node if current' span's service type is a queue AND :
            // 1. parent node's application service type is not a queue (it may have come from a queue that is traced)
            // 2. current node's application service type is not a queue (current node may be a queue that is traced)
            if (spanServiceType.isQueue()) {
                if (!selfVertex.serviceType().isQueue() && !parentVertex.serviceType().isQueue()) {
                    // emulate virtual queue node's accept Span and record it's acceptor host
                    String applicationName = span.getAcceptorHost();
                    if (applicationName == null) {
                        applicationName = span.getRemoteAddr();
                    }
                    final Vertex queueAcceptVertex = Vertex.of(applicationName, spanServiceType);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[Bind] child-queue {}/{} <- {}", queueAcceptVertex, span.getRemoteAddr(), parentVertex);
                    }
                    hostApplicationMapDao.insert(span.getCollectorAcceptTime(), parentVertex.applicationName(), parentVertex.serviceType().getCode(), queueAcceptVertex, span.getRemoteAddr());
                    // emulate virtual queue node's send SpanEvent

                    if (logger.isDebugEnabled()) {
                        logger.debug("[OutLink] child-queue {}/{} -> {}/{}", queueAcceptVertex, span.getRemoteAddr(),
                                selfVertex, span.getEndPoint());
                    }
                    linkService.updateOutLink(span.getCollectorAcceptTime(), queueAcceptVertex, span.getRemoteAddr(),
                            selfVertex, MERGE_QUEUE, span.getElapsed(), span.hasError());

                    parentVertex = queueAcceptVertex;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("child-span updateInLink child:{}/{} <- parentAppName:{}",
                        selfVertex, span.getAgentId(), parentVertex);
            }
            linkService.updateInLink(span.getCollectorAcceptTime(), selfVertex,
                    parentVertex, MERGE_AGENT, span.getElapsed(), span.hasError());
            bugCheck++;
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.

        linkService.updateResponseTime(span.getCollectorAcceptTime(), selfVertex, span.getAgentId(), span.getElapsed(), span.hasError());

        if (bugCheck != 1) {
            logger.info("ambiguous span found(bug). span {}/{}", span.getApplicationName(), span.getAgentName());
            if (logger.isDebugEnabled()) {
                logger.debug("ambiguous span found(bug). detailed span {}", span);
            }
        }
    }

    private void insertSpanEventStat(SpanBo span, Vertex selfVertex) {

        final List<SpanEventBo> spanEventList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("handle spanEvent {}/{} size:{}", span.getApplicationName(), span.getAgentId(), spanEventList.size());
        }

        // TODO need to batch update later.
        insertSpanEventList(spanEventList, selfVertex, span.getAgentId(), span.getEndPoint(), span.getCollectorAcceptTime());
    }

    private void insertSpanEventList(List<SpanEventBo> spanEventList, Vertex selfVertex,
                                     String agentId, String endPoint, long requestTime) {

        for (SpanEventBo spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());

            if (isAlias(spanEventType, spanEvent)) {
                insertAcceptorHost(requestTime, spanEvent, selfVertex);
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

            if (spanEventApplicationName == null) {
                throttledLogger.info("Failed to insert statistics. Cause:SpanEvent has invalid format " +
                                "selfApplication:{}/{}, spanEventApplication:{}/{}",
                        selfVertex, agentId, spanEventApplicationName, spanEventType);
                continue;
            }

            Vertex spanEventVertex = Vertex.of(spanEventApplicationName, spanEventType);
            /*
             * save information to draw a server map based on statistics
             */
            // save the information of outLink (the spanevent that called span)
            linkService.updateOutLink(requestTime, selfVertex, MERGE_AGENT,
                    spanEventVertex, spanEventEndPoint, elapsed, hasException);

            // save the information of inLink (the span that spanevent called)
            linkService.updateInLink(requestTime, spanEventVertex,
                    selfVertex, endPoint, elapsed, hasException);
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