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

import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.collector.dao.TracesDao;
import com.navercorp.pinpoint.common.ServiceType;
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
        // TODO consider to change span.isSetErr();
        final boolean isError = span.getErr() != 0;
        int bugCheck = 0;
        if (span.getParentSpanId() == -1) {
            // FIXME this is for only testing. insert agentId instead of host

            // create virtual user
            statisticsHandler.updateCaller(span.getApplicationName(), ServiceType.USER.getCode(), span.getAgentId(), span.getApplicationName(), span.getServiceType(), span.getAgentId(), span.getElapsed(), isError);

            // update the span information of node itself
            statisticsHandler.updateCallee(span.getApplicationName(), span.getServiceType(), span.getApplicationName(), ServiceType.USER.getCode(), span.getAgentId(), span.getElapsed(), isError);
            bugCheck++;
        }

        // save statistics info only when parentApplicationContext exists
        // when drawing server map based on statistics info, you must know the application name of the previous node.
        if (span.getParentApplicationName() != null) {
            logger.debug("Received parent application name. {}", span.getParentApplicationName());

            // TODO originally you must know the serviceType of parent(previous) node.
            // Assume all parent nodes as TOMCAT
            statisticsHandler.updateCallee(span.getApplicationName(), span.getServiceType(), span.getParentApplicationName(), span.getParentApplicationType(), span.getAgentId(), span.getElapsed(), isError);
            // statisticsHandler.updateCallee(span.getParentApplicationName(), span.getParentApplicationType(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
            bugCheck++;
        }

        // record the response time of node itself (Tomcat).
        // blow code may be conflict of idea above callee key.
        // it is odd to record reversely, because of already recording the caller data at previous node.
        // the data may be different due to timeout or network error.
        statisticsHandler.updateResponseTime(span.getApplicationName(), span.getServiceType(), span.getAgentId(), span.getElapsed(), isError);

        if (bugCheck != 1) {
            logger.warn("ambiguous span found(bug). span:{}", span);
        }
    }

    private void insertSpanEventStat(TSpan span) {

        final List<TSpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return;
        }

        logger.debug("handle spanEvent size:{}", spanEventList.size());
        // TODO need to batch update later.
        for (TSpanEvent spanEvent : spanEventList) {
            ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());
            if (!serviceType.isRecordStatistics()) {
                continue;
            }

            // if terminal update statistics
            final int elapsed = spanEvent.getEndElapsed();
            final boolean hasException = SpanEventUtils.hasException(spanEvent);

            /**
             * save information to draw a server map based on statistics
             */
            // save the information of caller (the spanevent that span called )
            statisticsHandler.updateCaller(span.getApplicationName(), span.getServiceType(), span.getAgentId(), spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);

            // save the information of callee (the span that called spanevent)
            statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEvent.getServiceType(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), elapsed, hasException);
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
        final short spanServiceType = span.getServiceType();

        final String parentApplicationName = span.getParentApplicationName();
        final short parentServiceType = span.getParentApplicationType();
        hostApplicationMapDao.insert(acceptorHost, spanApplicationName, spanServiceType, parentApplicationName, parentServiceType);
    }
}
