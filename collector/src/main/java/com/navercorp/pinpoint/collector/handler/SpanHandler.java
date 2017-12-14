/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler;

import java.util.List;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.thrift.dto.TSpan;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class SpanHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private StatisticsHandler statisticsHandler;

    @Autowired
    private HostApplicationMapDao hostApplicationMapDao;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private SpanFactory spanFactory;

    public void handleSimple(TBase<?, ?> tbase) {

        if (!(tbase instanceof TSpan)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            final TSpan tSpan = (TSpan) tbase;
            if (logger.isDebugEnabled()) {
                logger.debug("Received SPAN={}", tSpan);
            }


            final SpanBo spanBo = spanFactory.buildSpanBo(tSpan);

            traceDao.insert(spanBo);
            applicationTraceIndexDao.insert(tSpan);

            // insert statistics info for server map
            insertAcceptorHost(spanBo);
            insertSpanStat(spanBo);
            insertSpanEventStat(spanBo);
        } catch (Exception e) {
            logger.warn("Span handle error. Caused:{}. Span:{}",e.getMessage(), tbase, e);
        }
    }


    private void insertSpanStat(SpanBo span) {
        final ServiceType applicationServiceType = getApplicationServiceType(span);
        final ServiceType spanServiceType = registry.findServiceType(span.getServiceType());

        final boolean isError = span.getErrCode() != 0;
        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {
            if (spanServiceType.isQueue()) {
                // create virtual queue node
                statisticsHandler.updateCaller(span.getAcceptorHost(), spanServiceType, span.getRemoteAddr(), span.getApplicationId(), applicationServiceType, span.getEndPoint(), span.getElapsed(), isError);

                statisticsHandler.updateCallee(span.getApplicationId(), applicationServiceType, span.getAcceptorHost(), spanServiceType, span.getAgentId(), span.getElapsed(), isError);
            } else {
                // create virtual user
                statisticsHandler.updateCaller(span.getApplicationId(), ServiceType.USER, span.getAgentId(), span.getApplicationId(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

                // update the span information of the current node (self)
                statisticsHandler.updateCallee(span.getApplicationId(), applicationServiceType, span.getApplicationId(), ServiceType.USER, span.getAgentId(), span.getElapsed(), isError);
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
                    statisticsHandler.updateCaller(span.getAcceptorHost(), spanServiceType, span.getRemoteAddr(), span.getApplicationId(), applicationServiceType, span.getEndPoint(), span.getElapsed(), isError);

                    parentApplicationName = span.getAcceptorHost();
                    parentApplicationType = spanServiceType;
                }
            }

            statisticsHandler.updateCallee(span.getApplicationId(), applicationServiceType, parentApplicationName, parentApplicationType, span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.
        
        statisticsHandler.updateResponseTime(span.getApplicationId(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

        if (bugCheck != 1) {
            logger.warn("ambiguous span found(bug). span:{}", span);
        }
    }

    private void insertSpanEventStat(SpanBo span) {

        final List<SpanEventBo> spanEventList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }

        final ServiceType applicationServiceType = getApplicationServiceType(span);

        logger.debug("handle spanEvent size:{}", spanEventList.size());
        // TODO need to batch update later.
        for (SpanEventBo spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());
            if (!spanEventType.isRecordStatistics()) {
                continue;
            }

            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = spanEvent.hasException();

            /*
             * save information to draw a server map based on statistics
             */
            // save the information of caller (the spanevent that called span)
            statisticsHandler.updateCaller(span.getApplicationId(), applicationServiceType, span.getAgentId(), spanEvent.getDestinationId(), spanEventType, spanEvent.getEndPoint(), elapsed, hasException);

            // save the information of callee (the span that spanevent called)
            statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEventType, span.getApplicationId(), applicationServiceType, span.getEndPoint(), elapsed, hasException);
        }
    }

    private void insertAcceptorHost(SpanBo span) {
        // save host application map
        // acceptor host is set at profiler module only when the span is not the kind of root span
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
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
}
