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
import com.navercorp.pinpoint.collector.sampling.tail.StatisticsTraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class ApplicationMapTraceServiceTest {

    @Test
    void insertSpanDelegatesToApplicationMapService() {
        ApplicationMapService delegate = Mockito.mock(ApplicationMapService.class);
        ApplicationMapTraceService service = new ApplicationMapTraceService(delegate);
        SpanBo spanBo = new SpanBo();

        service.insertSpan(spanBo);

        verify(delegate).insertSpan(spanBo);
    }

    @Test
    void insertSpanChunkDelegatesToApplicationMapService() {
        ApplicationMapService delegate = Mockito.mock(ApplicationMapService.class);
        ApplicationMapTraceService service = new ApplicationMapTraceService(delegate);
        SpanChunkBo chunkBo = new SpanChunkBo();

        service.insertSpanChunk(chunkBo);

        verify(delegate).insertSpanChunk(chunkBo);
    }

    @Test
    void isStatisticsTraceService() {
        org.assertj.core.api.Assertions.assertThat(StatisticsTraceService.class)
                .isAssignableFrom(ApplicationMapTraceService.class);
    }
}
