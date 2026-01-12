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

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.collector.applicationmap.service.ApplicationMapService;
import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.event.SpanStorePublisher;
import com.navercorp.pinpoint.collector.scatter.service.ScatterService;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.event.SpanChunkInsertEvent;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class HbaseOtlpTraceService implements TraceService {
    private final Logger logger = LogManager.getLogger(getClass());

    private final TraceDao traceDao;
    private final ScatterService scatterService;
    private final ApplicationMapService applicationMapService;
    private final SpanStorePublisher publisher;
    private final Executor grpcOtlpTraceServerExecutor;

    public HbaseOtlpTraceService(TraceDao traceDao,
                                 ScatterService scatterService,
                                 ApplicationMapService applicationMapService,
                                 SpanStorePublisher spanStorePublisher,
                                 @Qualifier("grpcOtlpTraceServerExecutor") Executor grpcOtlpTraceServerExecutor) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.scatterService = Objects.requireNonNull(scatterService, "scatterService");
        this.applicationMapService = Objects.requireNonNull(applicationMapService, "applicationMapService");
        this.publisher = Objects.requireNonNull(spanStorePublisher, "spanStorePublisher");
        this.grpcOtlpTraceServerExecutor = Objects.requireNonNull(grpcOtlpTraceServerExecutor, "grpcSpanServerExecutor");
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        SpanChunkInsertEvent event = publisher.captureContext(spanChunkBo);
        traceDao.insertSpanChunk(spanChunkBo);
        applicationMapService.insertSpanChunk(spanChunkBo);
        publisher.publishEvent(event, true);
    }

    @Override
    public void insertSpan(SpanBo spanBo) {
        SpanInsertEvent event = publisher.captureContext(spanBo);
        CompletableFuture<Void> future = traceDao.asyncInsert(spanBo);
        scatterService.insert(spanBo);
        applicationMapService.insertSpan(spanBo);

        future.whenCompleteAsync((unused, throwable) -> {
            final boolean result = throwable == null;
            if (logger.isTraceEnabled()) {
                logger.trace("success {}", result);
            }
            publisher.publishEvent(event, result);
        }, grpcOtlpTraceServerExecutor);
    }
}
