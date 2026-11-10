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
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.ATTR_MEMORY_TYPE;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_GC_PAUSE_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_GOROUTINE_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_MEMORY_LIMIT;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogramPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OTel Go runtime metrics on the Inspector charts: memory as Heap / Non-Heap, goroutines as Total
 * Thread, the deprecated GC pause histogram as the old GC count / time.
 */
class GoDialectTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-go", "order-go-1", 1_700_000_000_000L);

    private final OtlpMetricInspectorMapper mapper = newMapper();

    private static OtlpMetricInspectorMapper newMapper() {
        return new OtlpMetricInspectorMapper(MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect()),
                new SeriesStateCache(1000, Duration.ofMinutes(10)));
    }

    private static Metric memoryUsed(long other, long stack, long timeNs) {
        return upDownCounter(GO_MEMORY_USED, "By",
                intPoint(stack, timeNs, ATTR_MEMORY_TYPE, GoDialect.MEMORY_TYPE_STACK),
                intPoint(other, timeNs, ATTR_MEMORY_TYPE, GoDialect.MEMORY_TYPE_OTHER));
    }

    private static Metric gcPauses(long count, double sumNs, long timeNs) {
        return histogram(GO_GC_PAUSE_NS, "ns", AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(count, sumNs, timeNs, List.of(), List.of()));
    }

    private static JvmGcBo jvmGc(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        return bo.getJvmGcBos().get(0);
    }

    @Test
    void memory_otherIsHeap_stackIsNonHeap_withoutALimit() {
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, List.of(memoryUsed(43_000_000, 800_000, T1_NS))));

        assertThat(gc.getHeapUsed()).isEqualTo(43_000_000);
        assertThat(gc.getHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getNonHeapUsed()).isEqualTo(800_000);
        assertThat(gc.getNonHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.UNKNOWN);
        assertThat(gc.getGcOldCount()).isZero();
    }

    /** {@code go.memory.limit} is sent only when GOMEMLIMIT is set, without attributes. */
    @Test
    void memoryLimit_isTheHeapMax() {
        final Metric limit = upDownCounter(GO_MEMORY_LIMIT, "By", intPoint(512L * 1024 * 1024, T1_NS));

        final MappingResult result = mapper.map(AGENT, List.of(memoryUsed(43_000_000, 800_000, T1_NS), limit));

        assertThat(jvmGc(result).getHeapMax()).isEqualTo(512L * 1024 * 1024);
        assertThat(result.getDropped()).isEmpty();
        assertThat(result.getAccepted(GoDialect.NAME)).isEqualTo(3);
    }

    @Test
    void goroutines_areTheTotalThreadCount() {
        final MappingResult result = mapper.map(AGENT, List.of(upDownCounter(GO_GOROUTINE_COUNT, "{goroutine}", intPoint(43, T1_NS))));

        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(43);
    }

    /** Every Go GC cycle counts as an old collection; the pause histogram is in nanoseconds. */
    @Test
    void deprecatedGcPauses_areTheOldGcCountAndTime() {
        final MappingResult first = mapper.map(AGENT, List.of(memoryUsed(43_000_000, 800_000, T1_NS), gcPauses(3, 2_400_000, T1_NS)));
        final MappingResult second = mapper.map(AGENT, List.of(memoryUsed(44_000_000, 800_000, T2_NS), gcPauses(5, 4_600_000, T2_NS)));

        assertThat(jvmGc(first).getGcOldCount()).isEqualTo(3);
        assertThat(jvmGc(first).getGcOldTime()).isEqualTo(2);
        assertThat(jvmGc(second).getGcOldCount()).isEqualTo(5);
        assertThat(jvmGc(second).getGcOldTime()).isEqualTo(5);
        assertThat(second.getDropped()).isEmpty();
    }

    /** The other deprecated metrics repeat what the current names say; they are not read. */
    @Test
    void otherGoMetrics_areUnknown() {
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter("process.runtime.go.mem.heap_alloc", "By", intPoint(9_000_000, T1_NS)),
                upDownCounter("go.memory.gc.goal", "By", intPoint(14_000_000, T1_NS)),
                counter("go.memory.allocated", "By", AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE, intPoint(81_000_000, T1_NS))));

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(3);
    }

    @Test
    void withoutTheGoDialect_goMetricsAreUnknown() {
        final OtlpMetricInspectorMapper jvmOnly = new OtlpMetricInspectorMapper(MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect()),
                new SeriesStateCache(1000, Duration.ofMinutes(10)));

        final MappingResult result = jvmOnly.map(AGENT, List.of(memoryUsed(43_000_000, 800_000, T1_NS)));

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(2);
    }

    // ---- captured output: Go 1.26.1, OTel Go 1.47.0, otelhttp + instrumentation/runtime 0.72.0 ----

    private static ResourceMetrics load(String path) throws IOException {
        try (InputStream in = GoDialectTest.class.getResourceAsStream("/fixtures/" + path)) {
            assertThat(in).as(path).isNotNull();
            return ExportMetricsServiceRequest.parseFrom(in).getResourceMetrics(0);
        }
    }

    @Test
    void capturedDefaultRuntimeMetrics_heapThreadsAndResponseTime_noGc() throws IOException {
        mapper.map(AGENT, load("go-otelhttp-0.72/cumulative-1.pb"));
        final MappingResult result = mapper.map(AGENT, load("go-otelhttp-0.72/cumulative-2.pb"));

        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.JVM_GC, AgentStatType.TOTAL_THREAD, AgentStatType.RESPONSE_TIME);
        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(43_290_624);
        assertThat(gc.getNonHeapUsed()).isEqualTo(819_200);
        assertThat(gc.getHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        // the default runtime metrics carry no GC count
        assertThat(gc.getGcOldCount()).isZero();
        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(43);
    }

    @Test
    void capturedDeprecatedRuntimeMetrics_addTheGcCount() throws IOException {
        mapper.map(AGENT, load("go-runtime-0.72-deprecated/cumulative-1.pb"));
        final MappingResult result = mapper.map(AGENT, load("go-runtime-0.72-deprecated/cumulative-2.pb"));

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(19_017_728);
        assertThat(gc.getGcOldCount()).isEqualTo(3);
        assertThat(gc.getGcOldTime()).isEqualTo(1);
        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(43);
        // process.runtime.go.mem.*, .goroutines, .gc.count, runtime.uptime ... are not read
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isPositive();
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isZero();
    }
}
