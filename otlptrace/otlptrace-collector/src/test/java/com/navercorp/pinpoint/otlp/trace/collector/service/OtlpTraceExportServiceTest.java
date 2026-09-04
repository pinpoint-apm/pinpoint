/*
 * Copyright 2026 NAVER Corp.
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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperData;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpUriStatSpan;
import com.navercorp.pinpoint.uristat.collector.dao.UriStatDao;
import com.navercorp.pinpoint.uristat.collector.model.UriStat;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceRejectReason;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The uriStat hook is an optional statistics side channel: it must receive the mapper's
 * collected spans when enabled, do nothing when the bean is absent (flags off), and — the
 * contract stated on the hook itself — a uriStat insert failure must never fail the trace
 * export (it is counted and logged only).
 */
class OtlpTraceExportServiceTest {

    private static final String INSERT_ERROR_METRIC = "collector.otlptrace.insert.error";

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private static OtlpTraceMapperData dataWithUriStatSpan() {
        OtlpTraceMapperData data = new OtlpTraceMapperData();
        data.addUriStatSpan(new OtlpUriStatSpan("svc", "app", "agent", "/a", 1_000L, 100, false));
        return data;
    }

    private OtlpTraceExportService newService(OtlpTraceMapperData mapperData, OtlpUriStatService uriStatService) {
        OtlpTraceMapper mapper = mock(OtlpTraceMapper.class);
        when(mapper.map(anyList())).thenReturn(mapperData);
        return new OtlpTraceExportService(
                new TraceService[0],
                mock(HbaseOtlpAgentInfoService.class),
                mock(HbaseOtlpApplicationIndexV2Service.class),
                mapper,
                Optional.empty(),
                Optional.ofNullable(uriStatService),
                Caffeine.newBuilder().maximumSize(16).build(),
                new OtlpTraceIngestMetrics(meterRegistry),
                meterRegistry);
    }

    private double count(String name, String... tags) {
        return meterRegistry.get(name).tags(tags).counter().count();
    }

    private static ResourceSpans resourceSpansWithSpans(int spanCount) {
        ScopeSpans.Builder scope = ScopeSpans.newBuilder();
        for (int i = 0; i < spanCount; i++) {
            scope.addSpans(Span.newBuilder().setName("s" + i));
        }
        return ResourceSpans.newBuilder().addScopeSpans(scope).build();
    }

    @Test
    void ingestMetrics_receivedCountsRawSpans_storedCountsMappedBos_perTransport() {
        OtlpTraceMapperData mapperData = new OtlpTraceMapperData();
        mapperData.addSpanBo(new com.navercorp.pinpoint.common.server.bo.SpanBo());
        mapperData.addSpanBo(new com.navercorp.pinpoint.common.server.bo.SpanBo());
        mapperData.addSpanChunkBo(new com.navercorp.pinpoint.common.server.bo.SpanChunkBo());
        OtlpTraceExportService service = newService(mapperData, null);

        // 3 + 4 raw spans over two ResourceSpans, sent over HTTP
        service.export(List.of(resourceSpansWithSpans(3), resourceSpansWithSpans(4)), OtlpTraceIngestMetrics.Transport.HTTP);

        assertThat(count(OtlpTraceIngestMetrics.SPAN_RECEIVED, "transport", "http")).isEqualTo(7.0);
        assertThat(count(OtlpTraceIngestMetrics.SPAN_STORED, "transport", "http", "type", "span")).isEqualTo(2.0);
        assertThat(count(OtlpTraceIngestMetrics.SPAN_STORED, "transport", "http", "type", "spanChunk")).isEqualTo(1.0);
        // the other transport stays untouched
        assertThat(count(OtlpTraceIngestMetrics.SPAN_RECEIVED, "transport", "grpc")).isZero();
    }

    @Test
    void ingestMetrics_rejectedSpans_countedPerReason() {
        OtlpTraceMapperData mapperData = new OtlpTraceMapperData();
        mapperData.getRejectedSpan().addCount(OtlpTraceRejectReason.INVALID_ID, 2);
        mapperData.getRejectedSpan().addCount(OtlpTraceRejectReason.ORPHAN, 5);
        mapperData.getRejectedSpan().addCount(OtlpTraceRejectReason.MAPPING_ERROR, 1);
        OtlpTraceExportService service = newService(mapperData, null);

        OtlpTraceExportResult result = service.export(List.of(resourceSpansWithSpans(8)), OtlpTraceIngestMetrics.Transport.GRPC);

        assertThat(count(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "grpc", "reason", "invalid_id")).isEqualTo(2.0);
        assertThat(count(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "grpc", "reason", "orphan")).isEqualTo(5.0);
        assertThat(count(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "grpc", "reason", "mapping_error")).isEqualTo(1.0);
        assertThat(count(OtlpTraceIngestMetrics.SPAN_REJECTED, "transport", "grpc", "reason", "unsampled")).isZero();
        // the client-facing total is unchanged by the metric split
        assertThat(result.clientRejected().count()).isEqualTo(8);
    }

    private double uriStatErrorCount(MeterRegistry registry) {
        return registry.get(INSERT_ERROR_METRIC).tag("op", "uriStat").counter().count();
    }

    @Test
    void uriStatEnabled_receivesMapperCollectedSpans() {
        List<UriStat> inserted = new ArrayList<>();
        UriStatDao captureDao = inserted::addAll;
        OtlpTraceExportService service = newService(dataWithUriStatSpan(),
                new OtlpUriStatService(captureDao, () -> "tenant1"));

        OtlpTraceExportResult result = service.export(List.of(), OtlpTraceIngestMetrics.Transport.GRPC);

        assertThat(result.serverErrorCount()).isZero();
        assertThat(inserted).hasSize(1);
        assertThat(inserted.get(0).getUri()).isEqualTo("/a");
        assertThat(uriStatErrorCount(meterRegistry)).isZero();
    }

    @Test
    void uriStatInsertFailure_doesNotFailExport_countsAndMovesOn() {
        UriStatDao throwingDao = data -> {
            throw new IllegalStateException("kafka down");
        };
        OtlpTraceExportService service = newService(dataWithUriStatSpan(),
                new OtlpUriStatService(throwingDao, () -> "tenant1"));

        OtlpTraceExportResult result = service.export(List.of(), OtlpTraceIngestMetrics.Transport.GRPC);

        // spans are already stored at this point: the export succeeds and the failure is
        // NOT counted toward serverErrorCount — only the uriStat error counter moves.
        assertThat(result.serverErrorCount()).isZero();
        assertThat(result.serverMessage()).isEmpty();
        assertThat(uriStatErrorCount(meterRegistry)).isEqualTo(1.0);
    }

    @Test
    void uriStatDisabled_absentBean_exportsWithoutTouchingUriStat() {
        OtlpTraceExportService service = newService(dataWithUriStatSpan(), null);

        OtlpTraceExportResult result = service.export(List.of(), OtlpTraceIngestMetrics.Transport.GRPC);

        assertThat(result.serverErrorCount()).isZero();
        assertThat(uriStatErrorCount(meterRegistry)).isZero();
    }
}
