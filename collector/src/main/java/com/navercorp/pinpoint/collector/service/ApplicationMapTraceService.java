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
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Always-on (100%) TraceService that records servermap statistics.
 * Previously this was invoked inside HbaseTraceService; it is split out so tail sampling
 * can keep servermap at 100% while sampling trace detail + scatter.
 * Lives in the component-scanned `service` package so it is always registered regardless
 * of whether tail sampling is enabled.
 */
@Service
public class ApplicationMapTraceService implements StatisticsTraceService {

    private final ApplicationMapService applicationMapService;

    public ApplicationMapTraceService(ApplicationMapService applicationMapService) {
        this.applicationMapService = Objects.requireNonNull(applicationMapService, "applicationMapService");
    }

    @Override
    public void insertSpan(SpanBo spanBo) {
        this.applicationMapService.insertSpan(spanBo);
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {
        this.applicationMapService.insertSpanChunk(spanChunkBo);
    }
}
