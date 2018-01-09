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
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class SpanChunkHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private StatisticsHandler statisticsHandler;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private SpanFactory spanFactory;

    @Override
    public void handleSimple(TBase<?, ?> tbase) {

        try {
            final SpanChunkBo spanChunkBo = newSpanChunkBo(tbase);

            traceDao.insertSpanChunk(spanChunkBo);

            final ServiceType applicationServiceType = getApplicationServiceType(spanChunkBo);
            List<SpanEventBo> spanEventList = spanChunkBo.getSpanEventBoList();
            if (spanEventList != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("SpanChunk Size:{}", spanEventList.size());
                }
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
                    // save the information of caller (the spanevent that span called)
                    statisticsHandler.updateCaller(spanChunkBo.getApplicationId(), applicationServiceType, spanChunkBo.getAgentId(), spanEvent.getDestinationId(), spanEventType, spanEvent.getEndPoint(), elapsed, hasException);

                    // save the information of callee (the span that called spanevent)
                    statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEventType, spanChunkBo.getApplicationId(), applicationServiceType, spanChunkBo.getEndPoint(), elapsed, hasException);
                }
            }
        } catch (Exception e) {
            logger.warn("SpanChunk handle error Caused:{}", e.getMessage(), e);
        }
    }

    private SpanChunkBo newSpanChunkBo(TBase<?, ?> tbase) {
        if (!(tbase instanceof TSpanChunk)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        final TSpanChunk tSpanChunk = (TSpanChunk) tbase;
        if (logger.isDebugEnabled()) {
            logger.debug("Received SpanChunk={}", tbase);
        }

        return this.spanFactory.buildSpanChunkBo(tSpanChunk);
    }

    private ServiceType getApplicationServiceType(SpanChunkBo spanChunk) {
        final short applicationServiceTypeCode = spanChunk.getApplicationServiceType();
        return registry.findServiceType(applicationServiceTypeCode);
    }
}