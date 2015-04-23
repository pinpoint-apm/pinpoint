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

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.dao.TracesDao;
import com.navercorp.pinpoint.common.util.SpanEventUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class SpanHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TracesDao traceDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private StatisticsHandler statisticsHandler;

    @Autowired
    private HostApplicationMapDao hostApplicationMapDao;

    @Autowired
    private ServiceTypeRegistryService registry;

    public void handleSimple(TBase<?, ?> tbase) {

        if (!(tbase instanceof TSpan)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            final TSpan span = (TSpan) tbase;
            if (logger.isDebugEnabled()) {
                logger.debug("Received SPAN={}", span);
            }

            traceDao.insert(span);
            applicationTraceIndexDao.insert(span);

            // insert statistics info for server map
            insertAcceptorHost(span);
            insertSpanStat(span);
            insertSpanEventStat(span);
        } catch (Exception e) {
            logger.warn("Span handle error. Caused:{}. Span:{}",e.getMessage(), tbase, e);
        }
    }

    private void insertSpanStat(TSpan span) {
        final ServiceType applicationServiceType = getApplicationServiceType(span);
        // TODO consider to change span.isSetErr();
        final boolean isError = span.getErr() != 0;
        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {

            // create virtual user
            statisticsHandler.updateCaller(span.getApplicationName(), ServiceType.USER, span.getAgentId(), span.getApplicationName(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

            // update the span information of the current node (self)
            statisticsHandler.updateCallee(span.getApplicationName(), applicationServiceType, span.getApplicationName(), ServiceType.USER, span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // save statistics info only when parentApplicationContext exists
        // when drawing server map based on statistics info, you must know the application name of the previous node.
        if (span.getParentApplicationName() != null) {
            logger.debug("Received parent application name. {}", span.getParentApplicationName());

            final ServiceType parentApplicationType = registry.findServiceType(span.getParentApplicationType());
            statisticsHandler.updateCallee(span.getApplicationName(), applicationServiceType, span.getParentApplicationName(), parentApplicationType, span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // record the response time of the current node (self).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.
        
        statisticsHandler.updateResponseTime(span.getApplicationName(), applicationServiceType, span.getAgentId(), span.getElapsed(), isError);

        if (bugCheck != 1) {
            logger.warn("ambiguous span found(bug). span:{}", span);
        }
    }

    private void insertSpanEventStat(TSpan span) {

        final List<TSpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }

        final ServiceType applicationServiceType = getApplicationServiceType(span);

        logger.debug("handle spanEvent size:{}", spanEventList.size());
        // TODO need to batch update later.
        for (TSpanEvent spanEvent : spanEventList) {
            final ServiceType spanEventType = registry.findServiceType(spanEvent.getServiceType());
            if (!spanEventType.isRecordStatistics()) {
                continue;
            }

            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = SpanEventUtils.hasException(spanEvent);

            /**
             * save information to draw a server map based on statistics
             */
            // save the information of caller (the spanevent that span called )
            statisticsHandler.updateCaller(span.getApplicationName(), applicationServiceType, span.getAgentId(), spanEvent.getDestinationId(), spanEventType, spanEvent.getEndPoint(), elapsed, hasException);

            // save the information of callee (the span that called spanevent)
            statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEventType, span.getApplicationName(), applicationServiceType, span.getEndPoint(), elapsed, hasException);
        }
    }

    private void insertAcceptorHost(TSpan span) {
        // save host application map
        // acceptor host is set at profiler module only when the span is not the kind of root span
        final String acceptorHost = span.getAcceptorHost();
        if (acceptorHost == null) {
            return;
        }
        final String spanApplicationName = span.getApplicationName();
        final short applicationServiceTypeCode = getApplicationServiceType(span).getCode();

        final String parentApplicationName = span.getParentApplicationName();
        final short parentServiceType = span.getParentApplicationType();
        hostApplicationMapDao.insert(acceptorHost, spanApplicationName, applicationServiceTypeCode, parentApplicationName, parentServiceType);
    }
    
    private ServiceType getApplicationServiceType(TSpan span) {
        // Check if applicationServiceType is set. If not, use span's service type. 
        final short applicationServiceTypeCode = span.isSetApplicationServiceType() ? span.getApplicationServiceType() : span.getServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }
}
