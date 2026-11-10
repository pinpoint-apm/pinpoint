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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogramPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_GC_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_MEMORY_POOL_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_MEMORY_TYPE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.HTTP_SERVER_REQUEST_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_GC_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_THREAD_COUNT;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * One source per Inspector value: the Micrometer bridge and the JMX Insight JVM rules next to the
 * runtime-telemetry metrics must not count the JVM twice.
 */
class DuplicateSourceTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-api", "order-api-1", 1_700_000_000_000L);
    private static final String RUNTIME = "io.opentelemetry.runtime-telemetry-java8";
    private static final String JMX = "io.opentelemetry.jmx";
    private static final String BRIDGE = "io.opentelemetry.micrometer-1.5";
    private static final String TOMCAT = "io.opentelemetry.tomcat-10.0";
    private static final long T3_NS = START_NS + TimeUnit.SECONDS.toNanos(180);

    private final OtlpMetricInspectorMapper mapper = newMapper(Duration.ofMinutes(10));

    private static OtlpMetricInspectorMapper newMapper(Duration retention) {
        return new OtlpMetricInspectorMapper(MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect()),
                new SeriesStateCache(1000, retention));
    }

    private static ResourceMetrics resource(Object... scopeAndMetrics) {
        final ResourceMetrics.Builder builder = ResourceMetrics.newBuilder();
        for (int i = 0; i < scopeAndMetrics.length; i += 2) {
            builder.addScopeMetrics(ScopeMetrics.newBuilder()
                    .setScope(InstrumentationScope.newBuilder().setName((String) scopeAndMetrics[i]))
                    .addMetrics((Metric) scopeAndMetrics[i + 1]));
        }
        return builder.build();
    }

    private static Metric runtimeThreads(long timeNs) {
        return upDownCounter(JVM_THREAD_COUNT, "{thread}",
                intPoint(12, timeNs, "jvm.thread.daemon", "true", "jvm.thread.state", "runnable"),
                intPoint(16, timeNs, "jvm.thread.daemon", "false", "jvm.thread.state", "waiting"));
    }

    /** JMX Insight reports the thread count without attributes. */
    private static Metric jmxThreads(long timeNs) {
        return upDownCounter(JVM_THREAD_COUNT, "{thread}", intPoint(28, timeNs));
    }

    private static long threads(MappingResult result) {
        return result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount();
    }

    private static Metric otelHeap(long bytes, long timeNs) {
        return upDownCounter(JVM_MEMORY_USED, "By", intPoint(bytes, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"));
    }

    private static Metric micrometerHeap(long bytes, long timeNs) {
        return upDownCounter(MicrometerDialect.JVM_MEMORY_USED, "bytes",
                intPoint(bytes, timeNs, MicrometerDialect.TAG_AREA, MicrometerDialect.AREA_HEAP, MicrometerDialect.TAG_ID, "G1 Old Gen"));
    }

    private static HistogramDataPoint deltaPoint(long count, double sum, long timeNs, String... attrs) {
        return histogramPoint(count, sum, timeNs, timeNs - TimeUnit.SECONDS.toNanos(60), List.of(), List.of(), attrs);
    }

    @Test
    void runtimeTelemetryWins_andTheJmxThreadCountIsNotAdded() {
        final MappingResult result = mapper.map(AGENT, resource(JMX, jmxThreads(T1_NS), RUNTIME, runtimeThreads(T1_NS)));

        assertThat(threads(result)).isEqualTo(28);
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
    }

    @Test
    void otelDialectWins_overMicrometer_withoutRuntimeTelemetry() {
        final MappingResult result = mapper.map(AGENT, resource(BRIDGE, micrometerHeap(500, T1_NS), JMX, otelHeap(300, T1_NS)));

        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapUsed()).isEqualTo(300);
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
    }

    @Test
    void aValueOnlyAnotherSourceReports_isKept() {
        final MappingResult result = mapper.map(AGENT, resource(RUNTIME, runtimeThreads(T1_NS), BRIDGE, micrometerHeap(500, T1_NS)));

        assertThat(result.getDropped()).doesNotContainKey(InspectorDropReason.DUPLICATE_SOURCE);
        assertThat(threads(result)).isEqualTo(28);
        assertThat(result.getAgentStatBo().getJvmGcBos().get(0).getHeapUsed()).isEqualTo(500);
    }

    /** The bridge's http.server.requests measures the same requests as the server instrumentation. */
    @Test
    void serverDurationOfTwoDialects_usesTheOtelOne() {
        final Metric otel1 = histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(1, 0.1, T1_NS, List.of(), List.of(), "http.route", "/a"));
        final Metric micrometer1 = histogram(MicrometerDialect.HTTP_SERVER_REQUESTS, "ms", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(1, 500, T1_NS, List.of(), List.of(), "uri", "/a"));
        mapper.map(AGENT, resource(TOMCAT, otel1, BRIDGE, micrometer1));

        final Metric otel2 = histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(2, 0.2, T2_NS, List.of(), List.of(), "http.route", "/a"));
        final Metric micrometer2 = histogram(MicrometerDialect.HTTP_SERVER_REQUESTS, "ms", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(2, 1000, T2_NS, List.of(), List.of(), "uri", "/a"));
        final MappingResult second = mapper.map(AGENT, resource(TOMCAT, otel2, BRIDGE, micrometer2));

        assertThat(second.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
        assertThat(second.getAgentStatBo().getResponseTimeBos().get(0).getAvg()).isEqualTo(100);
    }

    /**
     * Under DELTA the OTel SDK leaves a series without recordings out of the export. The bridge's GC
     * point of such an export must not take over the GC value: the agent's old GC total would count
     * the collections of both sources.
     */
    @Test
    void ownerIsRemembered_whenItLeavesASeriesOutOfOneExport() {
        final MappingResult first = mapper.map(AGENT, resource(
                RUNTIME, otelHeap(300, T1_NS),
                RUNTIME, histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA, deltaPoint(1, 0.1, T1_NS, ATTR_GC_NAME, "G1 Old Generation")),
                BRIDGE, histogram(MicrometerDialect.JVM_GC_PAUSE, "ms", AGGREGATION_TEMPORALITY_DELTA, deltaPoint(1, 100, T1_NS, MicrometerDialect.TAG_GC, "G1 Old Generation"))));
        // runtime-telemetry sends no GC series this time; the bridge does (a skewed interval)
        final MappingResult second = mapper.map(AGENT, resource(
                RUNTIME, otelHeap(300, T2_NS),
                BRIDGE, histogram(MicrometerDialect.JVM_GC_PAUSE, "ms", AGGREGATION_TEMPORALITY_DELTA, deltaPoint(1, 100, T2_NS, MicrometerDialect.TAG_GC, "G1 Old Generation"))));
        final MappingResult third = mapper.map(AGENT, resource(
                RUNTIME, otelHeap(300, T3_NS),
                RUNTIME, histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA, deltaPoint(1, 0.1, T3_NS, ATTR_GC_NAME, "G1 Old Generation"))));

        assertThat(first.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isEqualTo(1);
        assertThat(second.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
        assertThat(second.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isEqualTo(1);
        assertThat(third.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isEqualTo(2);
    }

    @Test
    void ownerGivesWay_afterTheRetentionPeriod() {
        final OtlpMetricInspectorMapper shortMemory = newMapper(Duration.ofSeconds(30));
        shortMemory.map(AGENT, resource(RUNTIME, runtimeThreads(T1_NS)));

        final MappingResult jmxOnly = shortMemory.map(AGENT, resource(JMX, jmxThreads(T2_NS)));

        assertThat(jmxOnly.getDropped()).doesNotContainKey(InspectorDropReason.DUPLICATE_SOURCE);
        assertThat(threads(jmxOnly)).isEqualTo(28);
    }

    @Test
    void aBetterSourceTakesOver_atOnce() {
        mapper.map(AGENT, resource(JMX, jmxThreads(T1_NS)));

        final MappingResult both = mapper.map(AGENT, resource(JMX, jmxThreads(T2_NS), RUNTIME, runtimeThreads(T2_NS)));

        assertThat(both.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
        assertThat(threads(both)).isEqualTo(28);
    }

    // ---- captured output: javaagent 2.31.1 with the bridge and/or JMX Insight on ----

    private static ResourceMetrics load(String name) throws IOException {
        try (InputStream in = DuplicateSourceTest.class.getResourceAsStream("/fixtures/otel-javaagent-2.31-duplicates/" + name)) {
            assertThat(in).as(name).isNotNull();
            return ExportMetricsServiceRequest.parseFrom(in).getResourceMetrics(0);
        }
    }

    /** The same export with only the default sources: runtime-telemetry and the server instrumentation. */
    private static ResourceMetrics defaultSourcesOnly(ResourceMetrics resourceMetrics) {
        final ResourceMetrics.Builder builder = resourceMetrics.toBuilder().clearScopeMetrics();
        for (ScopeMetrics scopeMetrics : resourceMetrics.getScopeMetricsList()) {
            final String scope = scopeMetrics.getScope().getName();
            if (scope.startsWith("io.opentelemetry.runtime-telemetry") || scope.equals(TOMCAT)) {
                builder.addScopeMetrics(scopeMetrics);
            }
        }
        return builder.build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"bridge", "jmx", "both"})
    void capturedExport_mapsLikeTheDefaultSourcesAlone(String config) throws IOException {
        final OtlpMetricInspectorMapper withExtraSources = newMapper(Duration.ofMinutes(10));
        final OtlpMetricInspectorMapper defaultOnly = newMapper(Duration.ofMinutes(10));
        withExtraSources.map(AGENT, load(config + "-1.pb"));
        defaultOnly.map(AGENT, defaultSourcesOnly(load(config + "-1.pb")));
        final MappingResult result = withExtraSources.map(AGENT, load(config + "-2.pb"));
        final MappingResult expected = defaultOnly.map(AGENT, defaultSourcesOnly(load(config + "-2.pb")));

        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isPositive();
        final AgentStatBo bo = result.getAgentStatBo();
        final AgentStatBo reference = expected.getAgentStatBo();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        assertThat(String.valueOf(bo.getJvmGcBos())).isEqualTo(String.valueOf(reference.getJvmGcBos()));
        assertThat(String.valueOf(bo.getTotalThreadCountBos())).isEqualTo(String.valueOf(reference.getTotalThreadCountBos()));
        assertThat(String.valueOf(bo.getLoadedClassBos())).isEqualTo(String.valueOf(reference.getLoadedClassBos()));
        assertThat(String.valueOf(bo.getResponseTimeBos())).isEqualTo(String.valueOf(reference.getResponseTimeBos()));
        assertThat(bo.getCpuLoadBos().get(0).getJvmCpuLoad()).isEqualTo(reference.getCpuLoadBos().get(0).getJvmCpuLoad());
        // the runtime-telemetry defaults send no buffer metrics: they come from one extra source, once
        assertThat(reference.getDirectBufferBos()).isEmpty();
        assertThat(bo.getDirectBufferBos()).hasSize(1);
        assertThat(bo.getDirectBufferBos().get(0).getDirectCount()).isEqualTo(12);
    }
}
