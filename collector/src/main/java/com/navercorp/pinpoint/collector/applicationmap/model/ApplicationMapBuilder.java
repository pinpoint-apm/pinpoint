/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.applicationmap.model;

import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.ParentApplication;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUidService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Builds the write-side {@link ApplicationMapModel} by traversing a span or span chunk.
 * Pure map-construction rules only; storage is handled by the caller.
 */
public class ApplicationMapBuilder {

    private final Logger logger = LogManager.getLogger(getClass());

    private static final String MERGE_AGENT = "_";
    private static final String MERGE_QUEUE = "_";

    private final ThrottledLogger throttledLogger = ThrottledLogger.getUncountedIntervalLogger(logger);

    private final ServiceTypeRegistryService registry;

    public ApplicationMapBuilder(ServiceTypeRegistryService registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public ApplicationMapModel build(final SpanChunkBo spanChunkBo) {
        final ApplicationMapModel model = new ApplicationMapModel(spanChunkBo.getCollectorAcceptTime());

        final List<SpanEventBo> spanEventList = spanChunkBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return model;
        }
        final SpanOwner owner = spanChunkBo.getSpanOwner();
        if (logger.isDebugEnabled()) {
            logger.debug("handle insertSpanChunk {}/{}/{} size:{}", owner.getServiceName(), owner.getApplicationName(), owner.getAgentId(), spanEventList.size());
        }
        final Vertex selfVertex = getSelfVertex(spanChunkBo);
        buildSpanEventList(model, spanEventList, selfVertex, owner.getAgentId(), spanChunkBo.getEndPoint());
        return model;
    }

    public ApplicationMapModel build(final SpanBo spanBo) {
        final ApplicationMapModel model = new ApplicationMapModel(spanBo.getCollectorAcceptTime());

        final Vertex selfVertex = getSelfVertex(spanBo);

        buildAcceptorHost(model, spanBo, selfVertex);
        buildSpanStat(model, spanBo, selfVertex);
        buildSpanEventStat(model, spanBo, selfVertex);
        return model;
    }

    private Vertex getSelfVertex(BasicSpan basicSpan) {
        final ServiceType applicationServiceType = getApplicationServiceType(basicSpan);
        final SpanOwner owner = basicSpan.getSpanOwner();
        return Vertex.of(owner.getServiceUid().getUid(), owner.getApplicationName(), applicationServiceType);
    }

    private ServiceType getApplicationServiceType(BasicSpan basicSpan) {
        final int applicationServiceTypeCode = basicSpan.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }

    private void buildAcceptorHost(ApplicationMapModel model, SpanEventBo spanEvent, Vertex selfVertex) {
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
        Vertex rpcVertex = Vertex.of(selfVertex.serviceUid(), destinationId, serviceType);
        model.addAcceptorHost(new AcceptorHostRow(selfVertex, rpcVertex, endPoint));
    }

    private void buildAcceptorHost(ApplicationMapModel model, SpanBo span, Vertex selfVertex) {
        // save host application map
        // acceptor host is set at profiler module only when the span is not the kind of root span
        final SpanOwner owner = span.getSpanOwner();
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
            logger.debug("acceptorHost is null agent: {}/{}/{}", owner.getServiceName(), owner.getApplicationName(), owner.getAgentId());
            return;
        }

        final ParentApplication parentApplication = span.getParentApplication();
        if (parentApplication == null) {
            logger.debug("parentApplication is null agent: {}/{}/{}", owner.getServiceName(), owner.getApplicationName(), owner.getAgentId());
            return;
        }
        final Vertex parentVertex = getParentVertex(parentApplication);
        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());
        if (spanServiceType.isQueue()) {
            final String host = span.getEndPoint();
            if (host == null) {
                logger.debug("endPoint is null agent: {}/{}/{}", owner.getServiceName(), owner.getApplicationName(), owner.getAgentId());
                return;
            }
            model.addAcceptorHost(new AcceptorHostRow(parentVertex, selfVertex, host));
        } else {
            model.addAcceptorHost(new AcceptorHostRow(parentVertex, selfVertex, acceptorHost));
        }
    }

    private Vertex getParentVertex(ParentApplication parentApplication) {
        Objects.requireNonNull(parentApplication, "parentApplication");

        int serviceUid = getServiceUid(parentApplication.serviceName());
        String parentApplicationName = parentApplication.applicationName();
        ServiceType parentApplicationType = registry.findServiceType(parentApplication.applicationServiceType());
        return Vertex.of(serviceUid, parentApplicationName, parentApplicationType);
    }

    private int getServiceUid(String serviceName) {
        return ServiceUidService.getServiceUid(serviceName).getUid();
    }

    private void buildSpanStat(ApplicationMapModel model, SpanBo span, Vertex selfVertex) {

        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());
        final ParentApplication parentApplication = span.getParentApplication();

        if (span.isRoot()) {
            // root span
            if (spanServiceType.isQueue()) {
                // create virtual queue node
                Vertex acceptVertex = getQueueAcceptVertex(span, spanServiceType);

                model.addOutLink(new OutLinkRow(acceptVertex, span.getRemoteAddr(),
                        selfVertex, MERGE_QUEUE, span.getElapsed(), span.hasError()));

                if (logger.isDebugEnabled()) {
                    logger.debug("[InLink] root-queue {} <- {}/{}", selfVertex, acceptVertex, span.getAgentId());
                }
                model.addInLink(new InLinkRow(selfVertex,
                        acceptVertex, MERGE_QUEUE, span.getElapsed(), span.hasError()));
            } else {
                // create virtual user
                // update the span information of the current node (self)
                Vertex userVertex = Vertex.of(span.getServiceUid().getUid(), span.getApplicationName(), ServiceType.USER);
                model.addInLink(new InLinkRow(selfVertex, userVertex, MERGE_AGENT, span.getElapsed(), span.hasError()));
            }
            if (parentApplication != null) {
                logInvalidSpan(span, InvalidSpanReason.ROOT_WITH_PARENT_APP);
            }
        } else {
            // child span
            // save statistics info only when parentApplicationContext exists
            // when drawing server map based on statistics info, you must know the application name of the previous node.
            if (parentApplication != null) {
                Vertex parentVertex = getParentVertex(parentApplication);

                logger.debug("Received parent application name. parentName:{} appName:{}", parentVertex, span.getApplicationName());

                // create virtual queue node if current' span's service type is a queue AND :
                // 1. parent node's application service type is not a queue (it may have come from a queue that is traced)
                // 2. current node's application service type is not a queue (current node may be a queue that is traced)
                if (spanServiceType.isQueue()) {
                    if (!selfVertex.serviceType().isQueue() && !parentVertex.serviceType().isQueue()) {
                        // emulate virtual queue node's accept Span and record it's acceptor host
                        final Vertex queueAcceptVertex = getQueueAcceptVertex(span, spanServiceType);

                        if (logger.isDebugEnabled()) {
                            logger.debug("[Bind] child-queue {}:{} <- {}", queueAcceptVertex, span.getRemoteAddr(), parentVertex);
                        }
                        model.addAcceptorHost(new AcceptorHostRow(parentVertex, queueAcceptVertex, span.getRemoteAddr()));
                        // emulate virtual queue node's send SpanEvent

                        if (logger.isDebugEnabled()) {
                            logger.debug("[OutLink] child-queue {}:{} -> {}:{}", queueAcceptVertex, span.getRemoteAddr(), selfVertex, span.getEndPoint());
                        }
                        model.addOutLink(new OutLinkRow(queueAcceptVertex, span.getRemoteAddr(),
                                selfVertex, MERGE_QUEUE, span.getElapsed(), span.hasError()));

                        parentVertex = queueAcceptVertex;
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("child-span updateInLink child {}:{} <- parentAppName:{}", selfVertex, span.getAgentId(), parentVertex);
                }
                model.addInLink(new InLinkRow(selfVertex,
                        parentVertex, MERGE_AGENT, span.getElapsed(), span.hasError()));
            } else {
                logInvalidSpan(span, InvalidSpanReason.CHILD_WITHOUT_PARENT_APP);
            }
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.
        model.addResponseTime(new ResponseTimeRow(selfVertex, span.getAgentId(), span.getElapsed(), span.hasError()));
    }

    private void logInvalidSpan(BasicSpan span, InvalidSpanReason reason) {
        // OTel-sourced spans routinely lack Pinpoint parent-app context (upstream does not
        // propagate a pp= tracestate entry), so an invalid span is expected noise rather than
        // an anomaly here. Keep it at debug for OTel and at info for native Pinpoint spans,
        // where the same reason genuinely signals lost propagation.
        final Level level = resolveInvalidSpanLevel(span);
        if (logger.isEnabled(level)) {
            final SpanOwner owner = span.getSpanOwner();
            logger.log(level, "Invalid span found. reason:{} span {}/{}/{}", reason, owner.getServiceName(), owner.getApplicationName(), owner.getAgentId());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Invalid span found. reason:{} detailed span {}", reason, span);
        }
    }

    private Level resolveInvalidSpanLevel(BasicSpan span) {
        return span.getTraceSourceType() == TraceSourceType.OPENTELEMETRY ? Level.DEBUG : Level.INFO;
    }

    private enum InvalidSpanReason {
        ROOT_WITH_PARENT_APP,
        CHILD_WITHOUT_PARENT_APP
    }

    private @NonNull Vertex getQueueAcceptVertex(SpanBo span, ServiceType spanServiceType) {
        String applicationName = span.getAcceptorHost();
        if (applicationName == null) {
            applicationName = span.getRemoteAddr();
        }
        ServiceUid serviceUid = span.getServiceUid();
        return Vertex.of(serviceUid.getUid(), applicationName, spanServiceType);
    }

    private void buildSpanEventStat(ApplicationMapModel model, SpanBo span, Vertex selfVertex) {

        final List<SpanEventBo> spanEventList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }
        SpanOwner owner = span.getSpanOwner();
        if (logger.isDebugEnabled()) {
            logger.debug("handle insertSpanEventStat {}/{}/{} size:{}", owner.getServiceName(), owner.getApplicationName(), owner.getAgentId(), spanEventList.size());
        }

        buildSpanEventList(model, spanEventList, selfVertex, owner.getAgentId(), span.getEndPoint());
    }

    private void buildSpanEventList(ApplicationMapModel model, List<SpanEventBo> spanEventList,
                                    Vertex selfVertex, String agentId, String endPoint) {

        for (SpanEventBo spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());

            if (isAlias(spanEventType, spanEvent)) {
                buildAcceptorHost(model, spanEvent, selfVertex);
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

            Vertex outVertex = Vertex.of(selfVertex.serviceUid(), spanEventApplicationName, spanEventType);
            /*
             * save information to draw a server map based on statistics
             */
            // save the information of outLink (the spanevent that called span)
            model.addOutLink(new OutLinkRow(selfVertex, MERGE_AGENT,
                    outVertex, spanEventEndPoint, elapsed, hasException));

            // save the information of inLink (the span that spanevent called)
            model.addInLink(new InLinkRow(outVertex,
                    selfVertex, endPoint, elapsed, hasException));
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
