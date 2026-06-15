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
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.event.SpanChunkInsertEvent;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    // Throttled so an HBase outage (every span failing) does not flood the log.
    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 100);

    private final TraceDao traceDao;
    private final ScatterService scatterService;
    private final ApplicationMapService applicationMapService;
    private final SpanStorePublisher publisher;
    private final Executor grpcOtlpTraceServerExecutor;
    // Phase 1.5: the gRPC response is sent before the async span put completes, so a failure here
    // is not retryable by the client. Surface it (metric + throttled WARN) so the silent loss is
    // at least observable/alertable.
    private final Counter asyncInsertErrorCounter;

    public HbaseOtlpTraceService(TraceDao traceDao,
                                 ScatterService scatterService,
                                 ApplicationMapService applicationMapService,
                                 SpanStorePublisher spanStorePublisher,
                                 @Qualifier("grpcOtlpTraceServerExecutor") Executor grpcOtlpTraceServerExecutor,
                                 MeterRegistry meterRegistry) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.scatterService = Objects.requireNonNull(scatterService, "scatterService");
        this.applicationMapService = Objects.requireNonNull(applicationMapService, "applicationMapService");
        this.publisher = Objects.requireNonNull(spanStorePublisher, "spanStorePublisher");
        this.grpcOtlpTraceServerExecutor = Objects.requireNonNull(grpcOtlpTraceServerExecutor, "grpcSpanServerExecutor");
        this.asyncInsertErrorCounter = Counter.builder("collector.otlptrace.span.async-insert.error")
                .description("OTLP trace async span HBase insert failures (response already acked)")
                .register(Objects.requireNonNull(meterRegistry, "meterRegistry"));
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
            if (!result) {
                asyncInsertErrorCounter.increment();
                throttledLogger.warn("Failed async span insert (HBase). spanId={}", spanBo.getSpanId(), throwable);
            } else if (logger.isTraceEnabled()) {
                logger.trace("success {}", result);
            }
            publisher.publishEvent(event, result);
        }, grpcOtlpTraceServerExecutor);
    }
}
