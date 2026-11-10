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
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.gauge;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogramPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.HTTP_SERVER_REQUESTS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_BUFFER_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_BUFFER_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_CLASSES_LOADED;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_CLASSES_UNLOADED;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_GC_CONCURRENT_PHASE_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_GC_PAUSE;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_MEMORY_MAX;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.JVM_THREADS_LIVE;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.PROCESS_CPU_USAGE;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.PROCESS_FILES_OPEN;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.PROCESS_START_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.MicrometerDialect.SYSTEM_CPU_USAGE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Micrometer's OTLP registry output (Spring Boot Actuator): Micrometer binder names and tags,
 * gauges for everything but timers, timers as histograms in milliseconds spelled "milliseconds",
 * {@code max} only under DELTA, buckets only when percentile histograms are on.
 */
class MicrometerDialectTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "boot-api", "boot-api-1", 0);
    private static final String MS = "milliseconds";
    private static final long JVM_START_MS = 1_700_000_000_000L - 2_500;

    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(
            MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect()),
            new SeriesStateCache(1000, Duration.ofMinutes(10)));

    /** What JvmMemoryMetrics / JvmGcMetrics / ProcessorMetrics / ... export on a G1 JVM. */
    private static List<Metric> bootActuatorExport(long timeNs) {
        final List<Metric> metrics = new ArrayList<>();
        metrics.add(gauge(JVM_MEMORY_USED, "bytes",
                doublePoint(100, timeNs, "area", "heap", "id", "G1 Eden Space"),
                doublePoint(10, timeNs, "area", "heap", "id", "G1 Survivor Space"),
                doublePoint(300, timeNs, "area", "heap", "id", "G1 Old Gen"),
                doublePoint(50, timeNs, "area", "nonheap", "id", "Metaspace"),
                doublePoint(5, timeNs, "area", "nonheap", "id", "Compressed Class Space"),
                doublePoint(6, timeNs, "area", "nonheap", "id", "CodeHeap 'non-nmethods'")));
        metrics.add(gauge(JVM_MEMORY_MAX, "bytes",
                doublePoint(-1, timeNs, "area", "heap", "id", "G1 Eden Space"),
                doublePoint(-1, timeNs, "area", "heap", "id", "G1 Survivor Space"),
                doublePoint(1000, timeNs, "area", "heap", "id", "G1 Old Gen"),
                doublePoint(-1, timeNs, "area", "nonheap", "id", "Metaspace"),
                doublePoint(1024, timeNs, "area", "nonheap", "id", "Compressed Class Space"),
                doublePoint(100, timeNs, "area", "nonheap", "id", "CodeHeap 'non-nmethods'")));
        metrics.add(histogram(JVM_GC_PAUSE, MS, AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(5, 250, timeNs, List.of(), List.of(), "gc", "G1 Young Generation", "action", "end of minor GC", "cause", "G1 Evacuation Pause"),
                histogramPoint(1, 30, timeNs, List.of(), List.of(), "gc", "G1 Young Generation", "action", "end of minor GC", "cause", "Metadata GC Threshold"),
                histogramPoint(1, 500, timeNs, List.of(), List.of(), "gc", "G1 Old Generation", "action", "end of major GC", "cause", "System.gc()")));
        metrics.add(histogram(JVM_GC_CONCURRENT_PHASE_TIME, MS, AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(2, 100, timeNs, List.of(), List.of(), "gc", "G1 Concurrent GC", "action", "end of concurrent GC pause", "cause", "No GC")));
        metrics.add(gauge(PROCESS_CPU_USAGE, "", doublePoint(0.12, timeNs)));
        metrics.add(gauge(SYSTEM_CPU_USAGE, "", doublePoint(0.34, timeNs)));
        metrics.add(gauge(JVM_THREADS_LIVE, "threads", doublePoint(33, timeNs)));
        metrics.add(gauge("jvm.threads.states", "threads",
                doublePoint(20, timeNs, "state", "runnable"), doublePoint(13, timeNs, "state", "waiting")));
        metrics.add(gauge(JVM_CLASSES_LOADED, "classes", doublePoint(12_000, timeNs)));
        metrics.add(counter(JVM_CLASSES_UNLOADED, "classes", AGGREGATION_TEMPORALITY_CUMULATIVE, doublePoint(7, timeNs)));
        metrics.add(gauge(JVM_BUFFER_COUNT, "buffers",
                doublePoint(4, timeNs, "id", "direct"), doublePoint(1, timeNs, "id", "mapped"),
                doublePoint(0, timeNs, "id", "mapped - 'non-volatile memory'")));
        metrics.add(gauge(JVM_BUFFER_MEMORY_USED, "bytes",
                doublePoint(4096, timeNs, "id", "direct"), doublePoint(0, timeNs, "id", "mapped"),
                doublePoint(0, timeNs, "id", "mapped - 'non-volatile memory'")));
        metrics.add(gauge(PROCESS_FILES_OPEN, "files", doublePoint(42, timeNs)));
        metrics.add(gauge(PROCESS_START_TIME, MS, doublePoint(JVM_START_MS, timeNs)));
        metrics.add(gauge("process.uptime", MS, doublePoint(60_000, timeNs)));
        metrics.add(gauge("tomcat.threads.busy", "threads", doublePoint(2, timeNs)));
        return metrics;
    }

    @Test
    void bootActuatorExport_mapsEveryInspectorFamily() {
        final MappingResult result = mapper.map(AGENT, bootActuatorExport(T1_NS));

        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        // the third buffer pool x2, the young collector's two jvm.gc.pause series, the concurrent phase
        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(5);
        // jvm.threads.states x2, process.uptime, tomcat.threads.busy
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(4);
        assertThat(result.getAccepted(MicrometerDialect.NAME)).isGreaterThan(20);
        assertThat(result.getAccepted(OtelSemconvDialect.NAME)).isZero();

        final AgentStatBo bo = result.getAgentStatBo();
        final JvmGcBo gc = bo.getJvmGcBos().get(0);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.G1);
        assertThat(gc.getHeapUsed()).isEqualTo(410);
        assertThat(gc.getHeapMax()).isEqualTo(1000);      // -1 pools left out
        assertThat(gc.getNonHeapUsed()).isEqualTo(61);
        assertThat(gc.getNonHeapMax()).isEqualTo(-1);      // Metaspace max=-1: no non-heap max, as the agent
        // the old generation only (young pauses and the concurrent phase are not charted), ms as ms
        assertThat(gc.getGcOldCount()).isEqualTo(1);
        assertThat(gc.getGcOldTime()).isEqualTo(500);
        assertThat(bo.getJvmGcDetailedBos()).isEmpty();

        final CpuLoadBo cpu = bo.getCpuLoadBos().get(0);
        assertThat(cpu.getJvmCpuLoad()).isEqualTo(0.12);
        assertThat(cpu.getSystemCpuLoad()).isEqualTo(0.34);   // default binder, not experimental here

        assertThat(bo.getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(33);
        final LoadedClassBo classes = bo.getLoadedClassBos().get(0);
        assertThat(classes.getLoadedClassCount()).isEqualTo(12_000);
        assertThat(classes.getUnloadedClassCount()).isEqualTo(7);
        final DirectBufferBo buffer = bo.getDirectBufferBos().get(0);
        assertThat(buffer.getDirectCount()).isEqualTo(4);
        assertThat(buffer.getDirectMemoryUsed()).isEqualTo(4096);
        assertThat(buffer.getMappedCount()).isEqualTo(1);
        assertThat(bo.getFileDescriptorBos().get(0).getOpenFileDescriptorCount()).isEqualTo(42);
        assertThat(result.getMapped(AgentStatType.RESPONSE_TIME)).isZero();
    }

    @Test
    void processStartTime_isTheJvmStart() {
        assertThat(mapper.findProcessStartTime(bootActuatorExport(T1_NS))).isEqualTo(JVM_START_MS);
        assertThat(mapper.findProcessStartTime(List.of(gauge(JVM_THREADS_LIVE, "threads", doublePoint(1, T1_NS))))).isEqualTo(OtlpMetricInspectorMapper.NO_START_TIME);
        // an implausible value (epoch 0, far future) is not trusted
        assertThat(mapper.findProcessStartTime(List.of(gauge(PROCESS_START_TIME, MS, doublePoint(0, T1_NS))))).isEqualTo(OtlpMetricInspectorMapper.NO_START_TIME);
        assertThat(mapper.findProcessStartTime(List.of(gauge(PROCESS_START_TIME, MS, doublePoint(4e12, T1_NS))))).isEqualTo(OtlpMetricInspectorMapper.NO_START_TIME);
        // seconds-valued variant is converted by its unit
        assertThat(mapper.findProcessStartTime(List.of(gauge(PROCESS_START_TIME, "s", doublePoint(JVM_START_MS / 1000d, T1_NS))))).isEqualTo(JVM_START_MS);
    }

    @Test
    void cumulativeTimerWithoutBuckets_responseTimeMaxFallsBackToTheMean() {
        final Metric first = histogram(HTTP_SERVER_REQUESTS, MS, AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(10, 1000, T1_NS, List.of(), List.of(), "method", "GET", "uri", "/a", "status", "200", "outcome", "SUCCESS"));
        final Metric second = histogram(HTTP_SERVER_REQUESTS, MS, AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(16, 1600, T2_NS, List.of(), List.of(), "method", "GET", "uri", "/a", "status", "200", "outcome", "SUCCESS"));

        assertThat(mapper.map(AGENT, List.of(first)).getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isEqualTo(1);
        final ResponseTimeBo responseTime = mapper.map(AGENT, List.of(second)).getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isEqualTo(100);
        assertThat(responseTime.getMax()).isEqualTo(100);
    }

    @Test
    void deltaTimer_carriesMax() {
        final Metric delta = Metric.newBuilder(histogram(HTTP_SERVER_REQUESTS, MS, AGGREGATION_TEMPORALITY_DELTA,
                        histogramPoint(4, 400, T1_NS, List.of(), List.of(), "uri", "/a")))
                .build();
        final Metric withMax = delta.toBuilder().setHistogram(delta.getHistogram().toBuilder()
                .setDataPoints(0, delta.getHistogram().getDataPoints(0).toBuilder().setMax(350))).build();

        final ResponseTimeBo responseTime = mapper.map(AGENT, List.of(withMax)).getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isEqualTo(100);
        assertThat(responseTime.getMax()).isEqualTo(350);
    }

    /** Micrometer 1.17 with the OTel memory conventions switched on but the default GC binder. */
    @Test
    void mixedConventionsInOneResource_resolvePerName() {
        final MappingResult result = mapper.map(AGENT, List.of(
                gauge(JVM_MEMORY_USED, "By",
                        doublePoint(100, T1_NS, OtelSemconvDialect.ATTR_MEMORY_TYPE, "heap", OtelSemconvDialect.ATTR_MEMORY_POOL_NAME, "G1 Eden Space")),
                gauge(OtelSemconvDialect.JVM_MEMORY_LIMIT, "By",
                        doublePoint(1000, T1_NS, OtelSemconvDialect.ATTR_MEMORY_TYPE, "heap", OtelSemconvDialect.ATTR_MEMORY_POOL_NAME, "G1 Old Gen")),
                histogram(JVM_GC_PAUSE, MS, AGGREGATION_TEMPORALITY_CUMULATIVE,
                        histogramPoint(1, 500, T1_NS, List.of(), List.of(), "gc", "G1 Old Generation"))));

        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapUsed()).isEqualTo(100);
        assertThat(gc.getHeapMax()).isEqualTo(1000);
        assertThat(gc.getGcOldTime()).isEqualTo(500);
        assertThat(result.getAccepted(OtelSemconvDialect.NAME)).isEqualTo(2);
        assertThat(result.getAccepted(MicrometerDialect.NAME)).isEqualTo(1);
    }

    @Test
    void sharedName_isAttributedByItsAttributeKeys() {
        final MetricVocabulary vocabulary = mapper.getVocabulary();
        assertThat(vocabulary.resolve(JVM_MEMORY_USED, java.util.Map.of("area", "heap")).dialect().name()).isEqualTo(MicrometerDialect.NAME);
        assertThat(vocabulary.resolve(JVM_MEMORY_USED, java.util.Map.of(OtelSemconvDialect.ATTR_MEMORY_TYPE, "heap")).dialect().name()).isEqualTo(OtelSemconvDialect.NAME);
        // no key at all: first dialect in order
        assertThat(vocabulary.resolve(JVM_MEMORY_USED, java.util.Map.of()).dialect().name()).isEqualTo(OtelSemconvDialect.NAME);
        assertThat(vocabulary.resolve(JVM_GC_PAUSE, java.util.Map.of()).dialect().name()).isEqualTo(MicrometerDialect.NAME);
        assertThat(vocabulary.resolve("no.such.metric", java.util.Map.of())).isNull();
        assertThat(vocabulary.dialectNames()).containsExactly("otel", "micrometer");
    }

    @Test
    void otelOnlyVocabulary_ignoresMicrometerNames() {
        final OtlpMetricInspectorMapper otelOnly = new OtlpMetricInspectorMapper(new OtelSemconvDialect(), new SeriesStateCache(10, Duration.ofMinutes(1)));
        final MappingResult result = otelOnly.map(AGENT, List.of(gauge(PROCESS_CPU_USAGE, "", doublePoint(0.1, T1_NS))));
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(1);
        assertThat(otelOnly.findProcessStartTime(List.of(gauge(PROCESS_START_TIME, MS, doublePoint(JVM_START_MS, T1_NS))))).isEqualTo(OtlpMetricInspectorMapper.NO_START_TIME);
    }

    @Test
    void conflictingRoles_areRejectedAtConstruction() {
        final MetricDialect rogue = new MetricDialect() {
            @Override public String name() { return "rogue"; }
            @Override public MetricRole roleOf(String metricName) { return JVM_GC_PAUSE.equals(metricName) ? MetricRole.CPU_JVM : null; }
            @Override public java.util.Set<String> metricNames() { return java.util.Set.of(JVM_GC_PAUSE); }
            @Override public double defaultDurationMillisPerUnit() { return 1; }
            @Override public String memoryTypeKey() { return "x"; }
            @Override public String memoryTypeHeap() { return "x"; }
            @Override public String memoryTypeNonHeap() { return "x"; }
            @Override public String memoryPoolKey() { return "x"; }
            @Override public String gcNameKey() { return "x"; }
            @Override public MetricVocabulary.MemoryType unlabeledMemoryType() { return null; }
            @Override public String unlabeledGcName() { return null; }
            @Override public String bufferPoolKey() { return "x"; }
            @Override public String bufferPoolDirect() { return "x"; }
            @Override public String bufferPoolMapped() { return "x"; }
            @Override public List<String> connectionPoolNameKeys() { return List.of(); }
            @Override public List<String> connectionStateKeys() { return List.of(); }
            @Override public String connectionStateUsed() { return "x"; }
        };
        assertThatThrownBy(() -> MetricVocabulary.of(new MicrometerDialect(), rogue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(JVM_GC_PAUSE);
        assertThatThrownBy(() -> new MetricVocabulary(List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void startTimeMetric_isAcceptedNotDropped_andProducesNoChart() {
        final MappingResult result = mapper.map(AGENT, List.of(gauge(PROCESS_START_TIME, MS, doublePoint(JVM_START_MS, T1_NS))));
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getDropped()).isEmpty();
        assertThat(result.getAccepted(MicrometerDialect.NAME)).isEqualTo(1);
        assertThat(START_NS).isLessThan(T1_NS);
    }
}
