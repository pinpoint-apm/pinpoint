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

package com.navercorp.pinpoint.otlp.trace.collector.inspector.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.collector.service.AgentStatService;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAgentInfoMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAgentStartTimeResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpResourceIdResolver;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpServiceNotFoundException;
import com.navercorp.pinpoint.otlp.trace.collector.service.HbaseOtlpAgentInfoService;
import com.navercorp.pinpoint.otlp.trace.collector.service.HbaseOtlpApplicationIndexV2Service;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import com.navercorp.pinpoint.otlp.trace.inspector.MetricVocabulary;
import com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect;
import com.navercorp.pinpoint.otlp.trace.inspector.OtlpMetricInspectorMapper;
import com.navercorp.pinpoint.otlp.trace.inspector.SeriesStateCache;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.resource.v1.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtlpMetricExportServiceTest {

    private static final long START_NS = TimeUnit.MILLISECONDS.toNanos(1_700_000_000_000L);
    private static final long T1_NS = START_NS + TimeUnit.SECONDS.toNanos(60);
    private static final IdAndName ID = new IdAndName("agent-1", "agent-1", "order-api", "DEFAULT", ServiceUidSupplier.DEFAULT);
    /** Dedup key of the session that starts at START_NS (the point start time of cpu/threads). */
    private static final String SESSION_KEY = "m:0/agent-1@1700000000000";

    private final OtlpResourceIdResolver idResolver = mock(OtlpResourceIdResolver.class);
    private final HbaseOtlpAgentInfoService agentInfoService = mock(HbaseOtlpAgentInfoService.class);
    private final HbaseOtlpApplicationIndexV2Service applicationIndexService = mock(HbaseOtlpApplicationIndexV2Service.class);
    private final AgentStatService agentStatService = mock(AgentStatService.class);
    private final Cache<String, Boolean> agentIdCache = Caffeine.newBuilder().maximumSize(100).build();
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpMetricIngestMetrics ingestMetrics = new OtlpMetricIngestMetrics(registry);

    private OtlpMetricExportService service;

    @BeforeEach
    void setUp() {
        OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(
                MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect()), new SeriesStateCache(100, Duration.ofMinutes(1)));
        service = new OtlpMetricExportService(idResolver, new OtlpAgentStartTimeResolver(registry), new OtlpAgentInfoMapper(),
                agentInfoService, applicationIndexService, agentIdCache, mapper,
                new AgentStatService[]{agentStatService}, ingestMetrics);
    }

    private static KeyValue attr(String key, String value) {
        return KeyValue.newBuilder().setKey(key).setValue(AnyValue.newBuilder().setStringValue(value)).build();
    }

    private static Metric cpu(double value) {
        return cpu(value, START_NS, T1_NS);
    }

    private static Metric cpu(double value, long startNs, long timeNs) {
        return Metric.newBuilder().setName(OtelSemconvDialect.JVM_CPU_RECENT_UTILIZATION).setUnit("1")
                .setGauge(Gauge.newBuilder().addDataPoints(NumberDataPoint.newBuilder().setAsDouble(value)
                        .setTimeUnixNano(timeNs).setStartTimeUnixNano(startNs))).build();
    }

    private static Metric threads(long value) {
        return threads(value, START_NS, T1_NS);
    }

    /** A cumulative Sum: its start_time is the one the session start is read from (gauges are not). */
    private static Metric threads(long value, long startNs, long timeNs) {
        return Metric.newBuilder().setName(OtelSemconvDialect.JVM_THREAD_COUNT).setUnit("{thread}")
                .setSum(Sum.newBuilder().setIsMonotonic(false)
                        .setAggregationTemporality(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(NumberDataPoint.newBuilder().setAsInt(value)
                                .setTimeUnixNano(timeNs).setStartTimeUnixNano(startNs))).build();
    }

    private static Metric custom() {
        return Metric.newBuilder().setName("my.app.orders").setUnit("{order}")
                .setGauge(Gauge.newBuilder().addDataPoints(NumberDataPoint.newBuilder().setAsInt(7).setTimeUnixNano(T1_NS))).build();
    }

    private static ResourceMetrics resource(Metric... metrics) {
        return ResourceMetrics.newBuilder()
                .setResource(Resource.newBuilder()
                        .addAttributes(attr("service.name", "order-api"))
                        .addAttributes(attr("service.instance.id", "agent-1"))
                        .addAttributes(attr("host.name", "host-a"))
                        .addAttributes(attr("telemetry.sdk.version", "2.31.1")))
                .addScopeMetrics(ScopeMetrics.newBuilder().addAllMetrics(List.of(metrics)))
                .build();
    }

    @Test
    void mapsAndStores_andRegistersTheAgentOnce() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);

        OtlpMetricExportResult first = service.export(List.of(resource(cpu(0.1), threads(12), custom())), OtlpTransport.GRPC);
        OtlpMetricExportResult second = service.export(List.of(resource(cpu(0.2), threads(13))), OtlpTransport.HTTP);

        assertThat(first.getStoredStats()).isEqualTo(1);
        assertThat(first.getRejectedPointCount()).isZero();
        assertThat(second.getStoredStats()).isEqualTo(1);

        ArgumentCaptor<AgentStatBo> stat = ArgumentCaptor.forClass(AgentStatBo.class);
        verify(agentStatService, times(2)).save(stat.capture());
        AgentStatBo bo = stat.getAllValues().get(0);
        assertThat(bo.getServiceName()).isEqualTo("DEFAULT");
        assertThat(bo.getApplicationName()).isEqualTo("order-api");
        assertThat(bo.getAgentId()).isEqualTo("agent-1");
        // no process.creation.time, no start-time metric: the earliest point start_time is the session start
        assertThat(bo.getStartTimestamp()).isEqualTo(1_700_000_000_000L);
        assertThat(counter(OtlpMetricIngestMetrics.AGENT_START_TIME, "source", OtlpMetricIngestMetrics.START_TIME_POINT)).isEqualTo(2);
        assertThat(bo.getCpuLoadBos().get(0).getJvmCpuLoad()).isEqualTo(0.1);
        assertThat(bo.getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(12);

        // AgentInfo once per session (serviceUid, agentId, start time)
        ArgumentCaptor<AgentInfoBo> agentInfo = ArgumentCaptor.forClass(AgentInfoBo.class);
        verify(agentInfoService, times(1)).insert(agentInfo.capture());
        verify(applicationIndexService, times(1)).insert(any(), any());
        assertThat(agentInfo.getValue().getAgentId()).isEqualTo("agent-1");
        assertThat(agentInfo.getValue().getApplicationName()).isEqualTo("order-api");
        assertThat(agentInfo.getValue().getServiceTypeCode()).isEqualTo(ServiceType.OPENTELEMETRY_SERVER.getCode());
        assertThat(agentInfo.getValue().getHostName()).isEqualTo("host-a");
        assertThat(agentInfo.getValue().getAgentVersion()).isEqualTo("2.31.1");
        assertThat(agentInfo.getValue().getStartTime()).isEqualTo(1_700_000_000_000L);
        assertThat(agentIdCache.getIfPresent(SESSION_KEY)).isTrue();

        assertThat(counter(OtlpMetricIngestMetrics.POINT_RECEIVED, "transport", "grpc")).isEqualTo(3);
        assertThat(counter(OtlpMetricIngestMetrics.POINT_MAPPED, "type", "cpuLoad")).isEqualTo(2);
        assertThat(counter(OtlpMetricIngestMetrics.POINT_DROPPED, "reason", "unknown_metric")).isEqualTo(1);
        assertThat(counter(OtlpMetricIngestMetrics.STAT_STORED, "transport", "http")).isEqualTo(1);
    }

    @Test
    void sessionAlreadyRegistered_isNotInsertedAgain() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);
        // another transport or an earlier export of this collector registered the session
        agentIdCache.put(SESSION_KEY, Boolean.TRUE);

        service.export(List.of(resource(cpu(0.1), threads(12))), OtlpTransport.GRPC);

        verify(agentInfoService, never()).insert(any());
        verify(agentStatService, times(1)).save(any());
    }

    /** A restart under the same agentId is a new session: a new start time registers a new AgentInfo row. */
    @Test
    void restartUnderTheSameAgentId_registersTheNewSession() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);
        final long restartNs = START_NS + TimeUnit.MINUTES.toNanos(35);

        service.export(List.of(resource(threads(12))), OtlpTransport.GRPC);
        service.export(List.of(resource(threads(13))), OtlpTransport.GRPC);
        service.export(List.of(resource(threads(14, restartNs, restartNs + TimeUnit.SECONDS.toNanos(60)))), OtlpTransport.GRPC);
        service.export(List.of(resource(threads(15, restartNs, restartNs + TimeUnit.SECONDS.toNanos(120)))), OtlpTransport.GRPC);

        ArgumentCaptor<AgentInfoBo> agentInfo = ArgumentCaptor.forClass(AgentInfoBo.class);
        verify(agentInfoService, times(2)).insert(agentInfo.capture());
        assertThat(agentInfo.getAllValues()).extracting(AgentInfoBo::getStartTime)
                .containsExactly(1_700_000_000_000L, TimeUnit.NANOSECONDS.toMillis(restartNs));
        verify(applicationIndexService, times(2)).insert(any(), any());
        assertThat(agentIdCache.getIfPresent(SESSION_KEY)).isTrue();
        assertThat(agentIdCache.getIfPresent("m:0/agent-1@" + TimeUnit.NANOSECONDS.toMillis(restartNs))).isTrue();
    }

    /**
     * Without any start time the key is {@code @0}: one registration, no churn. The row gets the
     * first export's point time (as the trace path gets its span start time), not 0.
     */
    @Test
    void noStartTime_registersOncePerAgent_withTheFirstPointTime() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);

        service.export(List.of(resource(cpu(0.1, 0, T1_NS))), OtlpTransport.GRPC);
        service.export(List.of(resource(cpu(0.2, 0, T1_NS + TimeUnit.SECONDS.toNanos(60)))), OtlpTransport.GRPC);

        ArgumentCaptor<AgentInfoBo> agentInfo = ArgumentCaptor.forClass(AgentInfoBo.class);
        verify(agentInfoService, times(1)).insert(agentInfo.capture());
        assertThat(agentInfo.getValue().getStartTime()).isEqualTo(TimeUnit.NANOSECONDS.toMillis(T1_NS));
        assertThat(agentIdCache.getIfPresent("m:0/agent-1@0")).isTrue();
        assertThat(counter(OtlpMetricIngestMetrics.AGENT_START_TIME, "source", OtlpMetricIngestMetrics.START_TIME_NONE)).isEqualTo(2);
    }

    @Test
    void resourceWithoutIdentity_isRejectedAndReported() {
        when(idResolver.resolve(anyMap())).thenThrow(new IllegalArgumentException("not found applicationName"));

        OtlpMetricExportResult result = service.export(List.of(resource(cpu(0.1), threads(12))), OtlpTransport.GRPC);

        assertThat(result.getStoredStats()).isZero();
        assertThat(result.getRejectedPoints()).containsEntry(OtlpMetricResourceRejectReason.INVALID_RESOURCE, 2L);
        assertThat(result.getRejectedMessage()).isEqualTo("invalid resource (2)");
        assertThat(OtlpMetricResponseMapper.toResponse(result).getPartialSuccess().getRejectedDataPoints()).isEqualTo(2);
        verify(agentStatService, never()).save(any());
        assertThat(counter(OtlpMetricIngestMetrics.RESOURCE_REJECTED, "reason", "invalid_resource")).isEqualTo(2);
    }

    @Test
    void unknownService_isRejectedAsServiceNotFound() {
        when(idResolver.resolve(anyMap())).thenThrow(new OtlpServiceNotFoundException("no-such-service"));

        OtlpMetricExportResult result = service.export(List.of(resource(cpu(0.1))), OtlpTransport.HTTP);

        assertThat(result.getRejectedPoints()).containsEntry(OtlpMetricResourceRejectReason.SERVICE_NOT_FOUND, 1L);
    }

    @Test
    void resourceWithOnlyUnknownMetrics_storesNothing_andIsACleanSuccess() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);

        OtlpMetricExportResult result = service.export(List.of(resource(custom())), OtlpTransport.GRPC);

        assertThat(result.getStoredStats()).isZero();
        assertThat(result.getRejectedPointCount()).isZero();
        assertThat(OtlpMetricResponseMapper.toResponse(result).hasPartialSuccess()).isFalse();
        verify(agentStatService, never()).save(any());
        verify(agentInfoService, never()).insert(any());
    }

    @Test
    void storeFailure_isCountedNotRetried() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);
        doThrow(new RuntimeException("kafka down")).when(agentStatService).save(any());

        OtlpMetricExportResult result = service.export(List.of(resource(cpu(0.1))), OtlpTransport.GRPC);

        assertThat(result.getStoredStats()).isZero();
        assertThat(result.getRejectedPointCount()).isZero();
        assertThat(counter(OtlpMetricIngestMetrics.INSERT_ERROR, "op", "agentStat")).isEqualTo(1);
    }

    @Test
    void agentInfoFailure_isNotCached_soTheNextExportRetries() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);
        doThrow(new RuntimeException("hbase down")).doNothing().when(agentInfoService).insert(any());

        service.export(List.of(resource(cpu(0.1), threads(12))), OtlpTransport.GRPC);
        assertThat(agentIdCache.getIfPresent(SESSION_KEY)).isNull();
        service.export(List.of(resource(cpu(0.2), threads(13))), OtlpTransport.GRPC);

        verify(agentInfoService, times(2)).insert(any());
        assertThat(agentIdCache.getIfPresent(SESSION_KEY)).isTrue();
        assertThat(counter(OtlpMetricIngestMetrics.INSERT_ERROR, "op", "agentInfo")).isEqualTo(1);
    }

    /** Micrometer's process.start.time beats the point start time (SDK start) for the session start. */
    @Test
    void startTimeMetric_isPreferredOverPointStartTime() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);
        final long jvmStart = 1_700_000_000_000L - 2_500;
        final Metric processStart = Metric.newBuilder().setName("process.start.time").setUnit("milliseconds")
                .setGauge(Gauge.newBuilder().addDataPoints(NumberDataPoint.newBuilder().setAsDouble(jvmStart)
                        .setTimeUnixNano(T1_NS).setStartTimeUnixNano(START_NS))).build();

        service.export(List.of(resource(cpu(0.1), processStart)), OtlpTransport.GRPC);

        ArgumentCaptor<AgentStatBo> stat = ArgumentCaptor.forClass(AgentStatBo.class);
        verify(agentStatService).save(stat.capture());
        assertThat(stat.getValue().getStartTimestamp()).isEqualTo(jvmStart);
        ArgumentCaptor<AgentInfoBo> agentInfo = ArgumentCaptor.forClass(AgentInfoBo.class);
        verify(agentInfoService).insert(agentInfo.capture());
        assertThat(agentInfo.getValue().getStartTime()).isEqualTo(jvmStart);
        assertThat(counter(OtlpMetricIngestMetrics.AGENT_START_TIME, "source", OtlpMetricIngestMetrics.START_TIME_METRIC)).isEqualTo(1);
        assertThat(counter(OtlpMetricIngestMetrics.POINT_ACCEPTED, "dialect", "micrometer")).isEqualTo(1);
        assertThat(counter(OtlpMetricIngestMetrics.POINT_ACCEPTED, "dialect", "otel")).isEqualTo(1);
    }

    @Test
    void registrationStartTime_knownStartTime_thenEarliestPointTime_thenNow() {
        assertThat(OtlpMetricExportService.registrationStartTime(1_600_000_000_000L, List.of(threads(1)))).isEqualTo(1_600_000_000_000L);
        Metric later = cpu(0.1, 0, T1_NS + TimeUnit.SECONDS.toNanos(5));
        assertThat(OtlpMetricExportService.registrationStartTime(0, List.of(later, threads(1))))
                .isEqualTo(TimeUnit.NANOSECONDS.toMillis(T1_NS));

        long before = System.currentTimeMillis();
        long now = OtlpMetricExportService.registrationStartTime(0, List.of());
        assertThat(now).isBetween(before, System.currentTimeMillis());
    }

    @Test
    void earliestPointStartTime_overPointTypes() {
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(cpu(0.1), threads(1)))).isEqualTo(1_700_000_000_000L);
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(custom()))).isEqualTo(-1);
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of())).isEqualTo(-1);
    }

    @Test
    void earliestPointStartTime_skipsDeltaPoints() {
        // a DELTA point's start_time is the start of its export interval: it moves every export and must
        // not become the agent start time (it would churn AgentInfo and the series-state keys)
        final long intervalStartNs = T1_NS - 10_000_000_000L;
        final Metric deltaCounter = Metric.newBuilder().setName(OtelSemconvDialect.JVM_CLASS_UNLOADED).setUnit("{class}")
                .setSum(Sum.newBuilder().setIsMonotonic(true).setAggregationTemporality(AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA)
                        .addDataPoints(NumberDataPoint.newBuilder().setAsInt(3).setTimeUnixNano(T1_NS).setStartTimeUnixNano(intervalStartNs))).build();
        final Metric deltaHistogram = Metric.newBuilder().setName(OtelSemconvDialect.JVM_GC_DURATION).setUnit("s")
                .setHistogram(io.opentelemetry.proto.metrics.v1.Histogram.newBuilder().setAggregationTemporality(AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA)
                        .addDataPoints(io.opentelemetry.proto.metrics.v1.HistogramDataPoint.newBuilder().setCount(1).setSum(0.01)
                                .setTimeUnixNano(T1_NS).setStartTimeUnixNano(intervalStartNs))).build();

        // only DELTA points: no usable start time
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(deltaCounter, deltaHistogram))).isEqualTo(-1);
        // a cumulative point next to them still gives the SDK start
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(deltaCounter, threads(1), deltaHistogram))).isEqualTo(1_700_000_000_000L);
    }

    @Test
    void earliestPointStartTime_skipsGauges() {
        // a gauge has no temporality, and under a DELTA reader its start_time is the previous collection:
        // it moves every export and must not become the session's start time
        final Metric movingGauge = cpu(0.1, T1_NS - 10_000_000_000L, T1_NS);

        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(movingGauge))).isEqualTo(-1);
        // the cumulative sum next to it gives the SDK start, although the gauge's start_time is... later
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(movingGauge, threads(1)))).isEqualTo(1_700_000_000_000L);
        // ...or earlier
        final Metric earlierGauge = cpu(0.1, START_NS - 10_000_000_000L, T1_NS);
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(earlierGauge, threads(1)))).isEqualTo(1_700_000_000_000L);
    }

    @Test
    void earliestPointStartTime_skipsTimesOutsideTheSanityWindow() {
        // not an epoch time (a monotonic clock, a unit mistake)
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(cumulativeThreads(1_000_000L)))).isEqualTo(-1);
        // ahead of this collector's clock
        final long tomorrowNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()) + TimeUnit.DAYS.toNanos(1);
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(List.of(cumulativeThreads(tomorrowNs)))).isEqualTo(-1);
        // a valid one next to them is used
        assertThat(OtlpMetricExportService.earliestPointStartTimeMs(
                List.of(cumulativeThreads(1_000_000L), threads(1), cumulativeThreads(tomorrowNs)))).isEqualTo(1_700_000_000_000L);
    }

    @Test
    void gaugeOnlyResource_isRegisteredOnce_althoughItsStartTimeMoves() {
        when(idResolver.resolve(anyMap())).thenReturn(ID);

        service.export(List.of(resource(cpu(0.1, T1_NS - 10_000_000_000L, T1_NS))), OtlpTransport.HTTP);
        service.export(List.of(resource(cpu(0.2, T1_NS, T1_NS + 10_000_000_000L))), OtlpTransport.HTTP);

        // no usable start time: one registration, not one per export
        verify(agentInfoService, times(1)).insert(any());
        assertThat(counter(OtlpMetricIngestMetrics.AGENT_START_TIME, "source", OtlpMetricIngestMetrics.START_TIME_NONE)).isEqualTo(2);
    }

    private static Metric cumulativeThreads(long startNs) {
        return Metric.newBuilder().setName(OtelSemconvDialect.JVM_THREAD_COUNT).setUnit("{thread}")
                .setSum(Sum.newBuilder().setIsMonotonic(false)
                        .setAggregationTemporality(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(NumberDataPoint.newBuilder().setAsInt(1)
                                .setTimeUnixNano(T1_NS).setStartTimeUnixNano(startNs))).build();
    }

    private double counter(String name, String tagKey, String tagValue) {
        return registry.get(name).tag(tagKey, tagValue).counters().stream().mapToDouble(c -> c.count()).sum();
    }
}
