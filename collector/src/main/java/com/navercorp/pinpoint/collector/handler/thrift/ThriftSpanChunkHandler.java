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

package com.navercorp.pinpoint.collector.handler.thrift;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.thrift.SpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author emeroad
 */
@Service
public class ThriftSpanChunkHandler implements SimpleHandler<TBase<?, ?>> {

    private final Logger logger = LogManager.getLogger(getClass());

    private final TraceService traceService;

    private final AcceptedTimeService acceptedTimeService;
    private final SpanEventFilter spanEventFilter;
    private final SpanFactory spanFactory;

    public ThriftSpanChunkHandler(TraceService traceService,
                                  AcceptedTimeService acceptedTimeService,
                                  Optional<SpanEventFilter> spanEventFilter,
                                  SpanFactory spanFactory) {
        this.traceService = Objects.requireNonNull(traceService, "traceService");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.spanEventFilter = spanEventFilter.orElseGet(EmptySpanEventFilter::new);
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
    }

    @Override
    public void handleSimple(ServerRequest<TBase<?, ?>> serverRequest) {
        final TBase<?, ?> data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle simple data={}", data);
        }
        if (data instanceof TSpanChunk) {
            handleSpanChunk((TSpanChunk) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }

    private void handleSpanChunk(TSpanChunk tbase) {
        try {
            long acceptedTime = acceptedTimeService.getAcceptedTime();
            final SpanChunkBo spanChunkBo = this.spanFactory.buildSpanChunkBo(tbase, acceptedTime, spanEventFilter);
            this.traceService.insertSpanChunk(spanChunkBo);
        } catch (Exception e) {
            logger.warn("Failed to handle SpanChunk={}, Caused={}", tbase, e.getMessage(), e);
        }
    }
}