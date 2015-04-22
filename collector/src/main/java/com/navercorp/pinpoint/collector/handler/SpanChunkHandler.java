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

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.dao.TracesDao;
import com.navercorp.pinpoint.common.util.SpanEventUtils;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class SpanChunkHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TracesDao traceDao;

    @Autowired
    private StatisticsHandler statisticsHandler;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Override
    public void handleSimple(TBase<?, ?> tbase) {

        if (!(tbase instanceof TSpanChunk)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            TSpanChunk spanChunk = (TSpanChunk) tbase;

            if (logger.isDebugEnabled()) {
                logger.debug("Received SpanChunk={}", spanChunk);
            }

            traceDao.insertSpanChunk(spanChunk);

            final ServiceType applicationServiceType = getApplicationServiceType(spanChunk);
            List<TSpanEvent> spanEventList = spanChunk.getSpanEventList();
            if (spanEventList != null) {
                logger.debug("SpanChunk Size:{}", spanEventList.size());
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
                    // save the information of caller (the spanevent that span called)
                    statisticsHandler.updateCaller(spanChunk.getApplicationName(), applicationServiceType, spanChunk.getAgentId(), spanEvent.getDestinationId(), spanEventType, spanEvent.getEndPoint(), elapsed, hasException);

                    // save the information of callee (the span that called spanevent)
                    statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEventType, spanChunk.getApplicationName(), applicationServiceType, spanChunk.getEndPoint(), elapsed, hasException);
                }
            }
        } catch (Exception e) {
            logger.warn("SpanChunk handle error Caused:{}", e.getMessage(), e);
        }
    }
    
    private ServiceType getApplicationServiceType(TSpanChunk spanChunk) {
        // Check if applicationServiceType is set. If not, use span's service type. 
        final short applicationServiceTypeCode = spanChunk.isSetApplicationServiceType() ? spanChunk.getApplicationServiceType() : spanChunk.getServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }
}