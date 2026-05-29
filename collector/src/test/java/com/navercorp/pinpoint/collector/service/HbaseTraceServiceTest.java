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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.applicationmap.service.ApplicationMapService;
import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.event.SpanStorePublisher;
import com.navercorp.pinpoint.collector.scatter.service.ScatterService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HbaseTraceServiceTest {

    @Test
    void insertSpan_doesNotCallApplicationMapService() {
        TraceDao traceDao = Mockito.mock(TraceDao.class);
        ScatterService scatterService = Mockito.mock(ScatterService.class);
        ApplicationMapService applicationMapService = Mockito.mock(ApplicationMapService.class);
        SpanStorePublisher publisher = Mockito.mock(SpanStorePublisher.class);
        when(publisher.captureContext(any(SpanBo.class))).thenReturn(Mockito.mock(SpanInsertEvent.class));
        when(traceDao.asyncInsert(any(SpanBo.class))).thenReturn(CompletableFuture.completedFuture(null));
        Executor direct = Runnable::run;

        HbaseTraceService service = new HbaseTraceService(traceDao, scatterService, applicationMapService, publisher, direct);
        SpanBo spanBo = new SpanBo();

        service.insertSpan(spanBo);

        verify(traceDao).asyncInsert(spanBo);
        verify(scatterService).insert(spanBo);
        verify(applicationMapService, never()).insertSpan(any());
    }

    @Test
    void insertSpanChunk_doesNotCallApplicationMapService() {
        TraceDao traceDao = Mockito.mock(TraceDao.class);
        ScatterService scatterService = Mockito.mock(ScatterService.class);
        ApplicationMapService applicationMapService = Mockito.mock(ApplicationMapService.class);
        SpanStorePublisher publisher = Mockito.mock(SpanStorePublisher.class);
        when(publisher.captureContext(any(com.navercorp.pinpoint.common.server.bo.SpanChunkBo.class)))
                .thenReturn(Mockito.mock(com.navercorp.pinpoint.common.server.event.SpanChunkInsertEvent.class));
        Executor direct = Runnable::run;

        HbaseTraceService service = new HbaseTraceService(traceDao, scatterService, applicationMapService, publisher, direct);
        com.navercorp.pinpoint.common.server.bo.SpanChunkBo chunkBo = new com.navercorp.pinpoint.common.server.bo.SpanChunkBo();

        service.insertSpanChunk(chunkBo);

        verify(traceDao).insertSpanChunk(chunkBo);
        verify(applicationMapService, never()).insertSpanChunk(any());
    }
}
