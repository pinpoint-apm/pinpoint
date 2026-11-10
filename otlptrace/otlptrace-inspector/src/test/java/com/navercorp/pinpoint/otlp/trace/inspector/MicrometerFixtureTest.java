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

package com.navercorp.pinpoint.otlp.trace.inspector;

import com.navercorp.pinpoint.common.server.bo.JvmGcType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real {@code micrometer-registry-otlp} 1.15 output (see {@code fixtures/micrometer-1.15/README.md}):
 * the dialect must read what the registry actually sends, not what its sources suggest.
 */
class MicrometerFixtureTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "x.mm-inspector-probe", "probe-1", 0);

    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(
            MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect()),
            new SeriesStateCache(1000, Duration.ofMinutes(10)));

    private static ResourceMetrics load(String name) throws IOException {
        try (InputStream in = MicrometerFixtureTest.class.getResourceAsStream("/fixtures/micrometer-1.15/" + name)) {
            assertThat(in).as(name).isNotNull();
            final ExportMetricsServiceRequest request = ExportMetricsServiceRequest.parseFrom(in);
            assertThat(request.getResourceMetricsCount()).isEqualTo(1);
            return request.getResourceMetrics(0);
        }
    }

    private static List<Metric> metrics(ResourceMetrics resourceMetrics) {
        final List<Metric> metrics = new ArrayList<>();
        for (ScopeMetrics scopeMetrics : resourceMetrics.getScopeMetricsList()) {
            metrics.addAll(scopeMetrics.getMetricsList());
        }
        return metrics;
    }

    private static String attribute(ResourceMetrics resourceMetrics, String key) {
        for (KeyValue keyValue : resourceMetrics.getResource().getAttributesList()) {
            if (keyValue.getKey().equals(key)) {
                return keyValue.getValue().getStringValue();
            }
        }
        return null;
    }

    @Test
    void resource_isMicrometer_withServiceNameAndInstanceId() throws IOException {
        final ResourceMetrics resource = load("cumulative-1.pb");
        assertThat(attribute(resource, "telemetry.sdk.name")).isEqualTo("io.micrometer");
        assertThat(attribute(resource, "telemetry.sdk.language")).isEqualTo("java");
        assertThat(attribute(resource, "service.name")).isEqualTo("x.mm-inspector-cap-cum");
        assertThat(attribute(resource, "service.instance.id")).hasSize(36);
        // what Micrometer does NOT send: the trace path's host/pid/runtime keys
        assertThat(attribute(resource, "host.name")).isNull();
        assertThat(attribute(resource, "process.pid")).isNull();
    }

    @Test
    void cumulativeExport_mapsTheJvmFamilies_inMilliseconds() throws IOException {
        final List<Metric> metrics = metrics(load("cumulative-1.pb"));
        final MappingResult result = mapper.map(AGENT, metrics);

        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        assertThat(result.getDropped(InspectorDropReason.UNSUPPORTED_TYPE)).isZero();
        assertThat(result.getAccepted(MicrometerDialect.NAME)).isGreaterThan(15);
        assertThat(result.getAccepted(OtelSemconvDialect.NAME)).isZero();
        assertThat(result.getMapped(AgentStatType.JVM_GC)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.JVM_GC_DETAILED)).isZero();
        assertThat(result.getMapped(AgentStatType.CPU_LOAD)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.TOTAL_THREAD)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.LOADED_CLASS)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.DIRECT_BUFFER)).isEqualTo(1);
        // first cumulative export: no response time baseline yet
        assertThat(result.getMapped(AgentStatType.RESPONSE_TIME)).isZero();
        assertThat(result.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isGreaterThan(0);

        final AgentStatBo bo = result.getAgentStatBo();
        final JvmGcBo gc = bo.getJvmGcBos().get(0);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.G1);
        assertThat(gc.getHeapUsed()).isBetween(1_000_000L, 2_000_000_000L);
        assertThat(gc.getHeapMax()).isGreaterThan(gc.getHeapUsed());
        assertThat(gc.getNonHeapUsed()).isBetween(1_000_000L, 1_000_000_000L);
        // a young JVM: old GC pause time is well under a minute when read as milliseconds
        // (read as seconds x1000 it would be hours)
        assertThat(gc.getGcOldCount()).isGreaterThanOrEqualTo(0);
        assertThat(gc.getGcOldTime()).isBetween(0L, 60_000L);
        assertThat(bo.getCpuLoadBos().get(0).getJvmCpuLoad()).isBetween(0d, 1d);
        assertThat(bo.getCpuLoadBos().get(0).getSystemCpuLoad()).isBetween(0d, 1d);
        assertThat(bo.getTotalThreadCountBos().get(0).getTotalThreadCount()).isBetween(10L, 500L);
        assertThat(bo.getLoadedClassBos().get(0).getLoadedClassCount()).isBetween(5_000L, 50_000L);
        assertThat(bo.getDirectBufferBos().get(0).getDirectCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void secondCumulativeExport_yieldsResponseTime_withTheSlowestSeriesMeanAsMax() throws IOException {
        mapper.map(AGENT, metrics(load("cumulative-1.pb")));
        final MappingResult second = mapper.map(AGENT, metrics(load("cumulative-2.pb")));

        assertThat(second.getMapped(AgentStatType.RESPONSE_TIME)).isEqualTo(1);
        final ResponseTimeBo responseTime = second.getAgentStatBo().getResponseTimeBos().get(0);
        // /ok, /sleep (120 ms), /throw: the mean over the interval is tens of millis
        assertThat(responseTime.getAvg()).isBetween(1L, 500L);
        // no buckets, cumulative: each uri series' max falls back to its mean and the point keeps the
        // largest, i.e. /sleep's ~120 ms (the series are one point even though Micrometer stamps each
        // Timer with its own clock read)
        assertThat(responseTime.getMax()).isGreaterThan(responseTime.getAvg());
        assertThat(responseTime.getMax()).isBetween(100L, 5_000L);
    }

    @Test
    void deltaExport_yieldsResponseTimeAtOnce_withTheMaxField() throws IOException {
        final MappingResult result = mapper.map(AGENT, metrics(load("delta.pb")));

        assertThat(result.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isZero();
        assertThat(result.getMapped(AgentStatType.RESPONSE_TIME)).isEqualTo(1);
        final ResponseTimeBo responseTime = result.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isBetween(1L, 500L);
        // delta carries max: /sleep makes it clearly above the mean
        assertThat(responseTime.getMax()).isGreaterThan(responseTime.getAvg());
        assertThat(responseTime.getMax()).isBetween(100L, 5_000L);
        // GC counters under DELTA are re-accumulated, never negative
        assertThat(result.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void cumulativeHistogramExport_approximatesMaxByBucketBound() throws IOException {
        mapper.map(AGENT, metrics(load("cumulative-histogram-1.pb")));
        final MappingResult second = mapper.map(AGENT, metrics(load("cumulative-histogram-2.pb")));

        final ResponseTimeBo responseTime = second.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isBetween(1L, 500L);
        // bucket upper bound of the slowest observed request (>= /sleep's 120 ms), above the mean
        assertThat(responseTime.getMax()).isGreaterThanOrEqualTo(responseTime.getAvg());
        assertThat(responseTime.getMax()).isBetween(100L, 5_000L);
    }

    @Test
    void processStartTime_isPresent_andBeforeTheExport() throws IOException {
        final ResourceMetrics resource = load("cumulative-1.pb");
        final long startTime = mapper.findProcessStartTime(metrics(resource));
        assertThat(startTime).isNotEqualTo(OtlpMetricInspectorMapper.NO_START_TIME);
        // the capture happened on 2026-11-02 KST; the JVM started that day, seconds before the first export
        assertThat(startTime).isBetween(1_793_600_000_000L, 1_793_700_000_000L);
        long firstPointTime = Long.MAX_VALUE;
        for (Metric metric : metrics(resource)) {
            if (metric.hasGauge() && metric.getGauge().getDataPointsCount() > 0) {
                firstPointTime = Math.min(firstPointTime, metric.getGauge().getDataPoints(0).getTimeUnixNano() / 1_000_000);
            }
        }
        assertThat(startTime).isLessThan(firstPointTime);
        assertThat(firstPointTime - startTime).isLessThan(Duration.ofMinutes(2).toMillis());
    }
}
