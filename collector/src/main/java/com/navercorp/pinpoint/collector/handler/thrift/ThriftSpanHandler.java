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
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.filter.EmptySpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.thrift.SpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class ThriftSpanHandler implements SimpleHandler<TBase<?, ?>> {

    private final Logger logger = LogManager.getLogger(getClass());

    private final TraceService traceService;

    private final AcceptedTimeService acceptedTimeService;
    private final SpanEventFilter spanEventFilter;
    private final SpanFactory spanFactory;

    public ThriftSpanHandler(TraceService traceService,
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

        if (data instanceof TSpan) {
            handleSpan((TSpan) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }

    private void handleSpan(TSpan tSpan) {
        try {
            long acceptedTime = acceptedTimeService.getAcceptedTime();
            final SpanBo spanBo = spanFactory.buildSpanBo(tSpan, acceptedTime, spanEventFilter);
            traceService.insertSpan(spanBo);
        } catch (Exception e) {
            logger.warn("Failed to handle Span={}, Caused:{}", tSpan, e.getMessage(), e);
        }
    }
}