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
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.TreeMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.exponentialHistogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.gauge;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogramPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_BUFFER_POOL_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_GC_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_MEMORY_POOL_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_MEMORY_TYPE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.HTTP_SERVER_REQUEST_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_BUFFER_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_BUFFER_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_CLASS_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_CLASS_UNLOADED;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_CPU_RECENT_UTILIZATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_FILE_DESCRIPTOR_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_GC_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_MEMORY_LIMIT;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_SYSTEM_CPU_UTILIZATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.JVM_THREAD_COUNT;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpMetricInspectorMapperTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-api", "order-api-1", 1_700_000_000_000L);
    private static final long T1_MS = TimeUnit.NANOSECONDS.toMillis(T1_NS);
    private static final long T2_MS = TimeUnit.NANOSECONDS.toMillis(T2_NS);
    private static final List<Double> BOUNDS = List.of(0.05, 0.1, 0.25, 0.5);

    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(new OtelSemconvDialect(),
            new SeriesStateCache(1000, Duration.ofMinutes(10)));

    private static Metric memoryUsedG1(long timeNs) {
        return upDownCounter(JVM_MEMORY_USED, "By",
                intPoint(100, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Eden Space"),
                intPoint(10, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Survivor Space"),
                intPoint(300, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"),
                intPoint(50, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "Metaspace"),
                intPoint(5, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "Compressed Class Space"),
                intPoint(1, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'non-nmethods'"),
                intPoint(2, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'profiled nmethods'"),
                intPoint(3, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'non-profiled nmethods'"));
    }

    private static Metric memoryLimitG1(long timeNs) {
        // G1 reports no limit for eden/survivor: only the old gen carries the heap max
        return upDownCounter(JVM_MEMORY_LIMIT, "By",
                intPoint(1000, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"),
                intPoint(1024, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "Compressed Class Space"),
                intPoint(100, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'non-nmethods'"),
                intPoint(100, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'profiled nmethods'"),
                intPoint(100, timeNs, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'non-profiled nmethods'"));
    }

    private static Metric gcDurationG1(long timeNs) {
        return histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(5, 0.25, timeNs, List.of(), List.of(), ATTR_GC_NAME, "G1 Young Generation", "jvm.gc.action", "end of minor GC"),
                histogramPoint(1, 0.5, timeNs, List.of(), List.of(), ATTR_GC_NAME, "G1 Old Generation", "jvm.gc.action", "end of major GC"),
                histogramPoint(2, 0.1, timeNs, List.of(), List.of(), ATTR_GC_NAME, "G1 Concurrent GC", "jvm.gc.action", "end of concurrent GC pause"));
    }

    @Test
    void g1Runtime_mapsJvmGc() {
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS), memoryLimitG1(T1_NS), gcDurationG1(T1_NS)));

        assertThat(result.getReceivedPoints()).isEqualTo(16);
        // young and concurrent collectors have no chart (the agent counts the old bean only)
        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(2);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        assertThat(result.getMapped(AgentStatType.JVM_GC)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.JVM_GC_DETAILED)).isZero();

        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getServiceName()).isEqualTo("DEFAULT");
        assertThat(bo.getApplicationName()).isEqualTo("order-api");
        assertThat(bo.getAgentId()).isEqualTo("order-api-1");
        assertThat(bo.getStartTimestamp()).isEqualTo(1_700_000_000_000L);

        final JvmGcBo gc = bo.getJvmGcBos().get(0);
        assertThat(gc.getDataPoint().getTimestamp()).isEqualTo(T1_MS);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.G1);
        assertThat(gc.getHeapUsed()).isEqualTo(410);
        assertThat(gc.getHeapMax()).isEqualTo(1000);
        assertThat(gc.getNonHeapUsed()).isEqualTo(61);
        // Metaspace reports no limit, so like MemoryMXBean (and the agent) the non-heap max is unknown
        assertThat(gc.getNonHeapMax()).isEqualTo(-1);
        // the old generation only: young and concurrent are left out as the agent does; seconds -> millis
        assertThat(gc.getGcOldCount()).isEqualTo(1);
        assertThat(gc.getGcOldTime()).isEqualTo(500);
        assertThat(bo.getJvmGcDetailedBos()).isEmpty();
    }

    @Test
    void nonHeapMax_isTheSum_whenEveryNonHeapPoolReportsALimit() {
        final Metric limits = upDownCounter(JVM_MEMORY_LIMIT, "By",
                intPoint(1000, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"),
                intPoint(2048, T1_NS, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "Metaspace"),
                intPoint(1024, T1_NS, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "Compressed Class Space"),
                intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'non-nmethods'"),
                intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'profiled nmethods'"),
                intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "CodeHeap 'non-profiled nmethods'"));
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS), limits));

        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapMax()).isEqualTo(1000);
        assertThat(gc.getNonHeapMax()).isEqualTo(3372);
    }

    @Test
    void zgcCycles_areNotCounted_onlyMajorPauses() {
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter(JVM_MEMORY_USED, "By", intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "ZGC Old Generation")),
                histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                        histogramPoint(4, 0.4, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "ZGC Major Cycles"),
                        histogramPoint(12, 0.12, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "ZGC Major Pauses"),
                        histogramPoint(40, 2.0, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "ZGC Minor Cycles"),
                        histogramPoint(120, 0.6, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "ZGC Minor Pauses"))));

        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(3);
        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.ZGC);
        assertThat(gc.getGcOldCount()).isEqualTo(12);
        assertThat(gc.getGcOldTime()).isEqualTo(120);
    }

    @Test
    void generationalZgc_heapMaxIsTheSharedHeap_notTheSumOfThePools() {
        // JDK 21+ generational ZGC: both generations report the whole heap (-Xmx) as their limit
        final long heap = 512L * 1024 * 1024;
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter(JVM_MEMORY_USED, "By",
                        intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "ZGC Young Generation"),
                        intPoint(300, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "ZGC Old Generation")),
                upDownCounter(JVM_MEMORY_LIMIT, "By",
                        intPoint(heap, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "ZGC Young Generation"),
                        intPoint(heap, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "ZGC Old Generation"))));

        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapUsed()).isEqualTo(400);
        assertThat(gc.getHeapMax()).isEqualTo(heap);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.ZGC);
    }

    @Test
    void memoryWithoutLimits_reportsUncollectedMax() {
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS)));

        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapMax()).isEqualTo(-1);
        assertThat(gc.getNonHeapMax()).isEqualTo(-1);
        // no collector series: the G1 heap pools name the family
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.G1);
        assertThat(gc.getGcOldCount()).isZero();
    }

    @Test
    void cpuThreadsClassesBuffersFileDescriptors() {
        final List<Metric> metrics = List.of(
                gauge(JVM_CPU_RECENT_UTILIZATION, "1", doublePoint(0.12, T1_NS)),
                upDownCounter(JVM_THREAD_COUNT, "{thread}",
                        intPoint(5, T1_NS, "jvm.thread.daemon", "true", "jvm.thread.state", "runnable"),
                        intPoint(7, T1_NS, "jvm.thread.daemon", "false", "jvm.thread.state", "waiting")),
                upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(1234, T1_NS)),
                counter(JVM_CLASS_UNLOADED, "{class}", AGGREGATION_TEMPORALITY_CUMULATIVE, intPoint(3, T1_NS)),
                upDownCounter(JVM_BUFFER_COUNT, "{buffer}",
                        intPoint(4, T1_NS, ATTR_BUFFER_POOL_NAME, "direct"),
                        intPoint(1, T1_NS, ATTR_BUFFER_POOL_NAME, "mapped")),
                upDownCounter(JVM_BUFFER_MEMORY_USED, "By",
                        intPoint(4096, T1_NS, ATTR_BUFFER_POOL_NAME, "direct"),
                        intPoint(0, T1_NS, ATTR_BUFFER_POOL_NAME, "mapped")),
                upDownCounter(JVM_FILE_DESCRIPTOR_COUNT, "{file_descriptor}", intPoint(42, T1_NS)));

        final MappingResult result = mapper.map(AGENT, metrics);
        assertThat(result.getDropped()).isEmpty();
        final AgentStatBo bo = result.getAgentStatBo();

        final CpuLoadBo cpu = bo.getCpuLoadBos().get(0);
        assertThat(cpu.getJvmCpuLoad()).isEqualTo(0.12);
        assertThat(cpu.getSystemCpuLoad()).isEqualTo(-1);   // experimental metric not emitted

        assertThat(bo.getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(12);

        final LoadedClassBo loaded = bo.getLoadedClassBos().get(0);
        assertThat(loaded.getLoadedClassCount()).isEqualTo(1234);
        assertThat(loaded.getUnloadedClassCount()).isEqualTo(3);

        final DirectBufferBo buffer = bo.getDirectBufferBos().get(0);
        assertThat(buffer.getDirectCount()).isEqualTo(4);
        assertThat(buffer.getDirectMemoryUsed()).isEqualTo(4096);
        assertThat(buffer.getMappedCount()).isEqualTo(1);
        assertThat(buffer.getMappedMemoryUsed()).isZero();

        assertThat(bo.getFileDescriptorBos().get(0).getOpenFileDescriptorCount()).isEqualTo(42);
        assertThat(bo.getJvmGcBos()).isEmpty();
        assertThat(bo.getResponseTimeBos()).isEmpty();
    }

    @Test
    void systemCpu_whenExperimentalMetricPresent() {
        final MappingResult result = mapper.map(AGENT, List.of(
                gauge(JVM_CPU_RECENT_UTILIZATION, "1", doublePoint(0.12, T1_NS)),
                gauge(JVM_SYSTEM_CPU_UTILIZATION, "1", doublePoint(0.34, T1_NS))));

        assertThat(result.getAgentStatBo().getCpuLoadBos().get(0).getSystemCpuLoad()).isEqualTo(0.34);
    }

    @Test
    void cumulativeHttpHistogram_firstExportHasNoBaseline_secondExportYieldsIntervalAverageAndMax() {
        final HistogramDataPoint a1 = histogramPoint(10, 1.0, T1_NS, BOUNDS, List.of(5L, 3L, 2L, 0L, 0L), "http.route", "/a", "http.request.method", "GET");
        final HistogramDataPoint b1 = histogramPoint(2, 0.1, T1_NS, BOUNDS, List.of(2L, 0L, 0L, 0L, 0L), "http.route", "/b", "http.request.method", "GET");
        final MappingResult first = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, a1, b1)));

        assertThat(first.isEmpty()).isTrue();
        assertThat(first.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isEqualTo(2);
        assertThat(first.getMapped(AgentStatType.RESPONSE_TIME)).isZero();

        // /a: +6 requests, +0.6s, highest grown bucket is (0.1, 0.25]; /b: unchanged
        final HistogramDataPoint a2 = histogramPoint(16, 1.6, T2_NS, BOUNDS, List.of(8L, 5L, 3L, 0L, 0L), "http.route", "/a", "http.request.method", "GET");
        final HistogramDataPoint b2 = histogramPoint(2, 0.1, T2_NS, BOUNDS, List.of(2L, 0L, 0L, 0L, 0L), "http.route", "/b", "http.request.method", "GET");
        final MappingResult second = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, a2, b2)));

        assertThat(second.getDropped()).isEmpty();
        assertThat(second.getMapped(AgentStatType.RESPONSE_TIME)).isEqualTo(1);
        final ResponseTimeBo responseTime = second.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getDataPoint().getTimestamp()).isEqualTo(T2_MS);
        assertThat(responseTime.getAvg()).isEqualTo(100);
        assertThat(responseTime.getMax()).isEqualTo(250);
    }

    @Test
    void cumulativeHttpHistogram_noRequestsInInterval_isZero() {
        final HistogramDataPoint p1 = histogramPoint(2, 0.1, T1_NS, BOUNDS, List.of(2L, 0L, 0L, 0L, 0L), "http.route", "/b");
        final HistogramDataPoint p2 = histogramPoint(2, 0.1, T2_NS, BOUNDS, List.of(2L, 0L, 0L, 0L, 0L), "http.route", "/b");
        mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, p1)));
        final MappingResult second = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, p2)));

        final ResponseTimeBo responseTime = second.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isZero();
        assertThat(responseTime.getMax()).isZero();
    }

    @Test
    void cumulativeHttpHistogram_processRestart_resetsBaseline() {
        final HistogramDataPoint p1 = histogramPoint(10, 1.0, T1_NS, BOUNDS, List.of(5L, 3L, 2L, 0L, 0L), "http.route", "/a");
        mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, p1)));

        final long restartedStart = START_NS + TimeUnit.SECONDS.toNanos(90);
        final HistogramDataPoint p2 = histogramPoint(3, 0.3, T2_NS, restartedStart, BOUNDS, List.of(1L, 1L, 1L, 0L, 0L), "http.route", "/a");
        final MappingResult afterRestart = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, p2)));

        assertThat(afterRestart.isEmpty()).isTrue();
        assertThat(afterRestart.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isEqualTo(1);
    }

    @Test
    void deltaHttpHistogram_isUsedDirectly_withExactMax() {
        final HistogramDataPoint point = HistogramDataPoint.newBuilder(
                        histogramPoint(4, 0.4, T1_NS, BOUNDS, List.of(1L, 1L, 1L, 1L, 0L), "http.route", "/a"))
                .setMax(0.3).build();
        final MappingResult result = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA, point)));

        final ResponseTimeBo responseTime = result.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isEqualTo(100);
        assertThat(responseTime.getMax()).isEqualTo(300);
    }

    @Test
    void deltaGcDuration_isAccumulatedToRunningTotal() {
        // DELTA start_time = start of the interval, so it advances export by export
        final Metric export1 = histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(2, 0.1, T1_NS, START_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"));
        final Metric export2 = histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(3, 0.2, T2_NS, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"));

        mapper.map(AGENT, List.of(memoryUsedG1(T1_NS), export1));
        // an export without any old-collector point (the OTel SDK sends none under DELTA when no GC ran)
        // repeats the total instead of falling back to zero
        final long quietNs = T1_NS + TimeUnit.SECONDS.toNanos(10);
        final MappingResult quiet = mapper.map(AGENT, List.of(memoryUsedG1(quietNs)));
        assertThat(quiet.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isEqualTo(2);
        assertThat(quiet.getAgentStatBo().getJvmGcBos().get(0).getGcOldTime()).isEqualTo(100);

        final MappingResult second = mapper.map(AGENT, List.of(memoryUsedG1(T2_NS), export2));

        final JvmGcBo gc = second.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.PARALLEL);
        assertThat(gc.getGcOldCount()).isEqualTo(5);
        assertThat(gc.getGcOldTime()).isEqualTo(300);
    }

    @Test
    void deltaTotals_fillACycleWithTheTotalAsItStoodThen() {
        // an earlier export left the totals at 2 collections / 100 ms and 2 unloaded classes
        final long t0Ns = START_NS + TimeUnit.SECONDS.toNanos(30);
        mapper.map(AGENT, List.of(memoryUsedG1(t0Ns), upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(10, t0Ns)),
                histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                        histogramPoint(2, 0.1, t0Ns, START_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep")),
                counter(JVM_CLASS_UNLOADED, "{class}", AGGREGATION_TEMPORALITY_DELTA, intPoint(2, t0Ns))));

        // a batch of two cycles: nothing recorded in the t1 interval, one collection and 3 unloads in t2
        final MappingResult result = mapper.map(AGENT, List.of(
                memoryUsedG1(T1_NS), memoryUsedG1(T2_NS),
                upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(10, T1_NS), intPoint(10, T2_NS)),
                histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                        histogramPoint(1, 0.05, T2_NS, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep")),
                counter(JVM_CLASS_UNLOADED, "{class}", AGGREGATION_TEMPORALITY_DELTA, intPoint(3, T2_NS))));

        final List<JvmGcBo> gc = result.getAgentStatBo().getJvmGcBos();
        assertThat(gc).extracting(JvmGcBo::getGcOldCount).containsExactly(2L, 3L);
        assertThat(gc).extracting(JvmGcBo::getGcOldTime).containsExactly(100L, 150L);
        assertThat(result.getAgentStatBo().getLoadedClassBos()).extracting(LoadedClassBo::getUnloadedClassCount)
                .containsExactly(2L, 5L);
    }

    @Test
    void deltaTotals_areNotFilledBackIntoACycleBeforeTheFirstIncrement() {
        // first export of the agent: t1 has no old GC yet, t2 has the first one
        final MappingResult result = mapper.map(AGENT, List.of(
                memoryUsedG1(T1_NS), memoryUsedG1(T2_NS),
                histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                        histogramPoint(1, 0.05, T2_NS, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"))));

        assertThat(result.getAgentStatBo().getJvmGcBos()).extracting(JvmGcBo::getGcOldCount).containsExactly(0L, 1L);
    }

    @Test
    void deltaTotals_neverFallInTimeOrder_whenAnIncrementArrivesAfterALaterExport() {
        // e2e: the export carrying an old collection was held by the upstream queue while the next, quiet
        // export went through; stored as 1, 2, 1, 2 the web drew two phantom collections
        final long t0 = START_NS + TimeUnit.SECONDS.toNanos(10);
        final long t1 = START_NS + TimeUnit.SECONDS.toNanos(20);
        final long t2 = START_NS + TimeUnit.SECONDS.toNanos(30);
        final long t3 = START_NS + TimeUnit.SECONDS.toNanos(40);
        final Map<Long, Long> stored = new TreeMap<>();
        final BiConsumer<Long, MappingResult> keep = (timeNs, result) ->
                stored.put(TimeUnit.NANOSECONDS.toMillis(timeNs), result.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount());

        keep.accept(t0, mapper.map(AGENT, List.of(memoryUsedG1(t0), histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(1, 0.01, t0, START_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep")))));
        keep.accept(t2, mapper.map(AGENT, List.of(memoryUsedG1(t2))));
        keep.accept(t1, mapper.map(AGENT, List.of(memoryUsedG1(t1), histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(1, 0.01, t1, t0, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep")))));
        keep.accept(t3, mapper.map(AGENT, List.of(memoryUsedG1(t3))));
        // the retried t2 export stores the same row again
        keep.accept(t2, mapper.map(AGENT, List.of(memoryUsedG1(t2))));

        // the late collection shows at t3 instead of t1; nothing falls, the sum is right
        assertThat(stored.values()).containsExactly(1L, 1L, 1L, 2L);
    }

    @Test
    void deltaIncrement_arrivingAfterALaterZeroPoint_isNotLost() {
        // Micrometer DELTA sends a count-0 point every export: the t2 zero point arrives before the t1
        // collection (upstream retry); the collection must still count, at the next cycle
        final long t1 = START_NS + TimeUnit.SECONDS.toNanos(20);
        final long t2 = START_NS + TimeUnit.SECONDS.toNanos(30);
        final long t3 = START_NS + TimeUnit.SECONDS.toNanos(40);
        mapper.map(AGENT, List.of(memoryUsedG1(t2), histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(0, 0, t2, t1, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"))));
        mapper.map(AGENT, List.of(memoryUsedG1(t1), histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(1, 0.01, t1, START_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"))));
        final MappingResult third = mapper.map(AGENT, List.of(memoryUsedG1(t3), histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(0, 0, t3, t2, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"))));

        assertThat(third.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isEqualTo(1);
        assertThat(third.getAgentStatBo().getJvmGcBos().get(0).getGcOldTime()).isEqualTo(10);
    }

    @Test
    void gcType_ofACycleWithoutCollectorSeries_comesFromTheHeapPools() {
        // OTel DELTA leaves a collector out of an export in which it collected nothing
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS)));

        assertThat(result.getAgentStatBo().getJvmGcBos().get(0).getGcType()).isEqualTo(JvmGcType.G1);
    }

    @Test
    void gcType_ofTheCollectorSeries_winsOverTheHeapPools() {
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS), histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(1, 0.01, T1_NS, START_NS, List.of(), List.of(), ATTR_GC_NAME, "PS MarkSweep"))));

        assertThat(result.getAgentStatBo().getJvmGcBos().get(0).getGcType()).isEqualTo(JvmGcType.PARALLEL);
    }

    @Test
    void deltaClassUnloaded_isAccumulatedToRunningTotal() {
        mapper.map(AGENT, List.of(upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(10, T1_NS)),
                counter(JVM_CLASS_UNLOADED, "{class}", AGGREGATION_TEMPORALITY_DELTA, intPoint(2, T1_NS))));
        // export without the unloaded series: the last total is repeated
        final long quietNs = T1_NS + TimeUnit.SECONDS.toNanos(10);
        final MappingResult quiet = mapper.map(AGENT, List.of(upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(10, quietNs))));
        assertThat(quiet.getAgentStatBo().getLoadedClassBos().get(0).getUnloadedClassCount()).isEqualTo(2);
        final MappingResult second = mapper.map(AGENT, List.of(upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(10, T2_NS)),
                counter(JVM_CLASS_UNLOADED, "{class}", AGGREGATION_TEMPORALITY_DELTA, intPoint(3, T2_NS))));

        assertThat(second.getAgentStatBo().getLoadedClassBos().get(0).getUnloadedClassCount()).isEqualTo(5);
    }

    @Test
    void unknownMetric_isDroppedAndCounted() {
        final MappingResult result = mapper.map(AGENT, List.of(
                gauge("my.app.orders.pending", "{order}", intPoint(7, T1_NS), intPoint(8, T1_NS, "shard", "b"))));

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getReceivedPoints()).isEqualTo(2);
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(2);
    }

    @Test
    void unsupportedDataType_isDroppedAndCounted() {
        final MappingResult result = mapper.map(AGENT, List.of(
                exponentialHistogram(HTTP_SERVER_REQUEST_DURATION),
                gauge(JVM_GC_DURATION, "s", doublePoint(0.1, T1_NS, ATTR_GC_NAME, "G1 Young Generation"))));

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getDropped(InspectorDropReason.UNSUPPORTED_TYPE)).isEqualTo(2);
    }

    @Test
    void missingRequiredAttribute_isInvalidPoint() {
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter(JVM_MEMORY_USED, "By", intPoint(100, T1_NS, ATTR_MEMORY_POOL_NAME, "G1 Eden Space")),
                upDownCounter(JVM_BUFFER_COUNT, "{buffer}", intPoint(4, T1_NS))));

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(2);
    }

    /** JDK 14+ exposes a third buffer pool; its series is skipped while direct/mapped are mapped. */
    @Test
    void bufferPoolWithoutInspectorChart_isIgnoredSeries() {
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter(JVM_BUFFER_COUNT, "{buffer}",
                        intPoint(4, T1_NS, ATTR_BUFFER_POOL_NAME, "direct"),
                        intPoint(1, T1_NS, ATTR_BUFFER_POOL_NAME, "mapped"),
                        intPoint(0, T1_NS, ATTR_BUFFER_POOL_NAME, "mapped - 'non-volatile memory'")),
                upDownCounter(JVM_BUFFER_MEMORY_USED, "By",
                        intPoint(4096, T1_NS, ATTR_BUFFER_POOL_NAME, "direct"),
                        intPoint(0, T1_NS, ATTR_BUFFER_POOL_NAME, "mapped - 'non-volatile memory'"))));

        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(2);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        final DirectBufferBo buffer = result.getAgentStatBo().getDirectBufferBos().get(0);
        assertThat(buffer.getDirectCount()).isEqualTo(4);
        assertThat(buffer.getMappedCount()).isEqualTo(1);
        assertThat(buffer.getDirectMemoryUsed()).isEqualTo(4096);
        assertThat(buffer.getMappedMemoryUsed()).isEqualTo(-1);
    }

    @Test
    void repeatedSeriesInOneExport_becomeOneStatPointPerCycle() {
        // an upstream batch of two collection cycles: the cpu series appears twice, threads once
        final MappingResult result = mapper.map(AGENT, List.of(
                gauge(JVM_CPU_RECENT_UTILIZATION, "1", doublePoint(0.2, T2_NS), doublePoint(0.1, T1_NS)),
                upDownCounter(JVM_THREAD_COUNT, "{thread}", intPoint(40, T1_NS))));

        final List<CpuLoadBo> cpu = result.getAgentStatBo().getCpuLoadBos();
        assertThat(cpu).hasSize(2);
        assertThat(cpu.get(0).getDataPoint().getTimestamp()).isEqualTo(T1_MS);
        assertThat(cpu.get(0).getJvmCpuLoad()).isEqualTo(0.1);
        assertThat(cpu.get(1).getDataPoint().getTimestamp()).isEqualTo(T2_MS);
        assertThat(cpu.get(1).getJvmCpuLoad()).isEqualTo(0.2);
        assertThat(result.getMapped(AgentStatType.CPU_LOAD)).isEqualTo(2);
        // the single thread point belongs to the first cycle
        assertThat(result.getAgentStatBo().getTotalThreadCountBos()).hasSize(1);
        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getDataPoint().getTimestamp()).isEqualTo(T1_MS);
    }

    @Test
    void seriesThatFirstAppearsInALaterCycle_joinsThatCycle() {
        // batch of two cycles where the thread series only exists in the second one: cpu keeps its two
        // instants (t1, t2) and threads go with the t2 cycle, not into the first one, whichever metric
        // comes first in the request
        final Metric cpuMetric = gauge(JVM_CPU_RECENT_UTILIZATION, "1", doublePoint(0.1, T1_NS), doublePoint(0.2, T2_NS));
        final Metric threadMetric = upDownCounter(JVM_THREAD_COUNT, "{thread}", intPoint(40, T2_NS));

        for (List<Metric> metrics : List.of(List.of(cpuMetric, threadMetric), List.of(threadMetric, cpuMetric))) {
            final MappingResult result = mapper.map(AGENT, metrics);

            final List<CpuLoadBo> cpu = result.getAgentStatBo().getCpuLoadBos();
            assertThat(cpu).hasSize(2);
            assertThat(cpu.get(0).getDataPoint().getTimestamp()).isEqualTo(T1_MS);
            assertThat(cpu.get(1).getDataPoint().getTimestamp()).isEqualTo(T2_MS);
            assertThat(result.getAgentStatBo().getTotalThreadCountBos()).hasSize(1);
            assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getDataPoint().getTimestamp()).isEqualTo(T2_MS);
        }
    }

    @Test
    void differentInstantsOfOneCycle_becomeOneStatPoint_atTheLatestInstant() {
        // Micrometer stamps each meter with its own clock read (and step-aligns delta histograms):
        // memory and cpu at T1, the gc histogram at T2, all from one export
        final MappingResult result = mapper.map(AGENT, List.of(
                memoryUsedG1(T1_NS), memoryLimitG1(T1_NS),
                gauge(JVM_CPU_RECENT_UTILIZATION, "1", doublePoint(0.1, T1_NS)),
                gcDurationG1(T2_NS)));

        assertThat(result.getMapped(AgentStatType.JVM_GC)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.CPU_LOAD)).isEqualTo(1);

        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        final JvmGcBo gc = bo.getJvmGcBos().get(0);
        // one whole point: heap from T1 and gc from T2, stamped with the latest instant
        assertThat(gc.getDataPoint().getTimestamp()).isEqualTo(T2_MS);
        assertThat(gc.getHeapUsed()).isEqualTo(410);
        assertThat(gc.getGcOldCount()).isEqualTo(1);
        assertThat(bo.getCpuLoadBos().get(0).getDataPoint().getTimestamp()).isEqualTo(T2_MS);
    }

    @Test
    void emptyInput_isEmptyResult() {
        final MappingResult result = mapper.map(AGENT, List.of());

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getReceivedPoints()).isZero();
        assertThat(result.getMapped()).isEmpty();
    }

    /** Micrometer writes the TimeUnit name; 50 ms of GC must stay 50, not become 50,000. */
    @Test
    void durationUnit_millisecondsSpelledOut_isNotScaledAsSeconds() {
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS),
                histogram(JVM_GC_DURATION, "milliseconds", AGGREGATION_TEMPORALITY_CUMULATIVE,
                        histogramPoint(2, 50, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "G1 Old Generation")),
                histogram(HTTP_SERVER_REQUEST_DURATION, "milliseconds", AGGREGATION_TEMPORALITY_DELTA,
                        histogramPoint(4, 400, T1_NS, List.of(), List.of(), "uri", "/a"))));

        assertThat(result.getDropped()).isEmpty();
        assertThat(result.getAgentStatBo().getJvmGcBos().get(0).getGcOldTime()).isEqualTo(50);
        assertThat(result.getAgentStatBo().getResponseTimeBos().get(0).getAvg()).isEqualTo(100);
    }

    @Test
    void durationUnit_emptyUsesTheDialectDefault_unknownIsInvalid() {
        final MappingResult result = mapper.map(AGENT, List.of(memoryUsedG1(T1_NS),
                histogram(JVM_GC_DURATION, "", AGGREGATION_TEMPORALITY_CUMULATIVE,
                        histogramPoint(1, 0.5, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "G1 Old Generation")),
                histogram(HTTP_SERVER_REQUEST_DURATION, "furlongs", AGGREGATION_TEMPORALITY_DELTA,
                        histogramPoint(4, 0.4, T1_NS, List.of(), List.of(), "uri", "/a"))));

        // OTel default is seconds
        assertThat(result.getAgentStatBo().getJvmGcBos().get(0).getGcOldTime()).isEqualTo(500);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
        assertThat(result.getAgentStatBo().getResponseTimeBos()).isEmpty();
    }

    @Test
    void durationUnits_table() {
        assertThat(DurationUnits.millisPerUnit("s", 1)).isEqualTo(1000);
        assertThat(DurationUnits.millisPerUnit("seconds", 1)).isEqualTo(1000);
        assertThat(DurationUnits.millisPerUnit("ms", 1000)).isEqualTo(1);
        assertThat(DurationUnits.millisPerUnit("milliseconds", 1000)).isEqualTo(1);
        assertThat(DurationUnits.millisPerUnit("us", 1000)).isEqualTo(0.001);
        assertThat(DurationUnits.millisPerUnit("µs", 1000)).isEqualTo(0.001);
        assertThat(DurationUnits.millisPerUnit("nanoseconds", 1000)).isEqualTo(0.000001);
        assertThat(DurationUnits.millisPerUnit("min", 1000)).isEqualTo(60_000);
        assertThat(DurationUnits.millisPerUnit("MS", 1000)).isEqualTo(1);
        assertThat(DurationUnits.millisPerUnit("", 1000)).isEqualTo(1000);
        assertThat(DurationUnits.millisPerUnit(null, 1000)).isEqualTo(1000);
        assertThat(DurationUnits.millisPerUnit("{request}", 1000)).isNaN();
        assertThat(DurationUnits.millisPerUnit("By", 1000)).isNaN();
        assertThat(DurationUnits.isKnown("milliseconds")).isTrue();
        assertThat(DurationUnits.isKnown("bytes")).isFalse();
    }

    /** Micrometer's jvm.memory.max is -1 for a pool without a limit: not a value, not an error. */
    @Test
    void negativeMemoryLimit_isSkipped_negativeUsedIsInvalid() {
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter(JVM_MEMORY_USED, "By",
                        intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Eden Space"),
                        intPoint(-1, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Survivor Space")),
                upDownCounter(JVM_MEMORY_LIMIT, "By",
                        intPoint(-1, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Eden Space"),
                        intPoint(1000, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"),
                        intPoint(-1, T1_NS, ATTR_MEMORY_TYPE, "non_heap", ATTR_MEMORY_POOL_NAME, "Metaspace"))));

        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapUsed()).isEqualTo(100);
        assertThat(gc.getHeapMax()).isEqualTo(1000);
        assertThat(gc.getNonHeapMax()).isEqualTo(-1);
    }

    @Test
    void nanAndInfinitePoints_areInvalid() {
        final MappingResult result = mapper.map(AGENT, List.of(
                gauge(JVM_CPU_RECENT_UTILIZATION, "1", doublePoint(Double.NaN, T1_NS)),
                upDownCounter(JVM_THREAD_COUNT, "{thread}", doublePoint(Double.POSITIVE_INFINITY, T1_NS)),
                upDownCounter(JVM_CLASS_COUNT, "{class}", intPoint(-5, T1_NS)),
                upDownCounter(JVM_BUFFER_COUNT, "{buffer}", doublePoint(Double.NaN, T1_NS, ATTR_BUFFER_POOL_NAME, "direct")),
                histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                        histogramPoint(1, Double.NaN, T1_NS, List.of(), List.of(), ATTR_GC_NAME, "G1 Old Generation"))));

        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(5);
    }

    @Test
    void temporalityOfProtoValues() {
        assertThat(Temporality.of(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)).isEqualTo(Temporality.CUMULATIVE);
        assertThat(Temporality.of(AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA)).isEqualTo(Temporality.DELTA);
        assertThat(Temporality.of(AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED)).isEqualTo(Temporality.UNSPECIFIED);
    }
}
