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
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.ATTR_GO_CPU_STATE;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.ATTR_MEMORY_TYPE;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_CPU_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_GC_PAUSE_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_MEMORY_GC_CYCLES;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_MEMORY_GC_PAUSE_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.GoDialect.GO_MEMORY_USED;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.attrs;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogramPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_CPU_MODE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.PROCESS_CPU_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.SYSTEM_CPU_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Go CPU from running totals (the contrib host metrics, or the runtime's opt-in {@code go.cpu.time})
 * and the Go GC from {@code go.memory.gc.cycles} + {@code go.memory.gc.pause.duration}, the
 * replacement of the deprecated pause histogram.
 */
class GoCpuAndGcTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-go", "order-go-1", 1_700_000_000_000L);
    private static final AggregationTemporality CUMULATIVE = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
    private static final AggregationTemporality DELTA = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
    private static final long T3_NS = START_NS + TimeUnit.SECONDS.toNanos(180);

    private final OtlpMetricInspectorMapper mapper = newMapper();

    private static OtlpMetricInspectorMapper newMapper() {
        return new OtlpMetricInspectorMapper(MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect()),
                new SeriesStateCache(1000, Duration.ofMinutes(10)));
    }

    private static NumberDataPoint seconds(double value, long timeNs, String key, String mode) {
        return doublePoint(value, timeNs, key, mode);
    }

    /** {@code process.cpu.time} user + system seconds. */
    private static Metric processCpu(AggregationTemporality temporality, double user, double system, long timeNs) {
        return counter(PROCESS_CPU_TIME, "s", temporality,
                seconds(user, timeNs, ATTR_CPU_MODE, "user"), seconds(system, timeNs, ATTR_CPU_MODE, "system"));
    }

    /** {@code system.cpu.time}: busy seconds split user/system, and idle. */
    private static Metric systemCpu(AggregationTemporality temporality, double busy, double idle, long timeNs) {
        return counter(SYSTEM_CPU_TIME, "s", temporality,
                seconds(busy / 2, timeNs, ATTR_CPU_MODE, "user"), seconds(busy / 2, timeNs, ATTR_CPU_MODE, "system"),
                seconds(idle, timeNs, ATTR_CPU_MODE, "idle"));
    }

    private static Metric runtimeCpu(double user, double gc, double idle, long timeNs) {
        return counter(GO_CPU_TIME, "s", CUMULATIVE,
                seconds(user, timeNs, ATTR_GO_CPU_STATE, "user"), seconds(gc, timeNs, ATTR_GO_CPU_STATE, "gc"),
                seconds(0, timeNs, ATTR_GO_CPU_STATE, "scavenge"), seconds(idle, timeNs, ATTR_GO_CPU_STATE, "idle"));
    }

    private static CpuLoadBo cpu(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getCpuLoadBos()).hasSize(1);
        return bo.getCpuLoadBos().get(0);
    }

    private static boolean hasCpu(MappingResult result) {
        return result.getAgentStatBo() != null && !result.getAgentStatBo().getCpuLoadBos().isEmpty();
    }

    // ---- CPU ----

    /** 8 machine CPUs over 60 s = 480 CPU-seconds; the process used 12 of them, the machine 120. */
    @Test
    void hostCpuTimes_processShareOfTheMachine_andSystemBusyShare() {
        final MappingResult first = mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 10, 2, T1_NS), systemCpu(CUMULATIVE, 1000, 3000, T1_NS)));
        assertThat(hasCpu(first)).isFalse();
        assertThat(first.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isEqualTo(5);

        final MappingResult second = mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 20, 4, T2_NS), systemCpu(CUMULATIVE, 1120, 3360, T2_NS)));

        assertThat(cpu(second).getJvmCpuLoad()).isCloseTo(12.0 / 480, within(1e-9));
        assertThat(cpu(second).getSystemCpuLoad()).isCloseTo(120.0 / 480, within(1e-9));
        assertThat(second.getDropped()).isEmpty();
    }

    /** A process that used no CPU in the interval is 0 %, and the machine's CPU is still stored. */
    @Test
    void hostCpuTimes_zeroProcessIncrease_isZeroPercent() {
        mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 10, 2, T1_NS), systemCpu(CUMULATIVE, 1000, 3000, T1_NS)));

        final MappingResult second = mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 10, 2, T2_NS), systemCpu(CUMULATIVE, 1120, 3360, T2_NS)));

        assertThat(cpu(second).getJvmCpuLoad()).isZero();
        assertThat(cpu(second).getSystemCpuLoad()).isCloseTo(120.0 / 480, within(1e-9));
    }

    /** The same CPU time sent in milliseconds maps to the same ratios as in seconds, also mixed with seconds. */
    @Test
    void cpuTimesInMilliseconds_areConvertedToSeconds() {
        final Metric process1 = counter(PROCESS_CPU_TIME, "ms", CUMULATIVE,
                seconds(10_000, T1_NS, ATTR_CPU_MODE, "user"), seconds(2_000, T1_NS, ATTR_CPU_MODE, "system"));
        final Metric process2 = counter(PROCESS_CPU_TIME, "ms", CUMULATIVE,
                seconds(20_000, T2_NS, ATTR_CPU_MODE, "user"), seconds(4_000, T2_NS, ATTR_CPU_MODE, "system"));
        mapper.map(AGENT, List.of(process1, systemCpu(CUMULATIVE, 1000, 3000, T1_NS)));

        final MappingResult second = mapper.map(AGENT, List.of(process2, systemCpu(CUMULATIVE, 1120, 3360, T2_NS)));

        assertThat(cpu(second).getJvmCpuLoad()).isCloseTo(12.0 / 480, within(1e-9));
        assertThat(cpu(second).getSystemCpuLoad()).isCloseTo(120.0 / 480, within(1e-9));
    }

    @Test
    void runtimeCpuTime_busyShareOfGomaxprocs_withoutSystemCpu() {
        mapper.map(AGENT, List.of(runtimeCpu(5, 1, 100, T1_NS)));

        final CpuLoadBo cpu = cpu(mapper.map(AGENT, List.of(runtimeCpu(8, 1.5, 136.5, T2_NS))));

        // busy 3.5 of 40 runtime CPU-seconds
        assertThat(cpu.getJvmCpuLoad()).isCloseTo(3.5 / 40, within(1e-9));
        assertThat(cpu.getSystemCpuLoad()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
    }

    /** The runtime refreshes its CPU totals every ~15 s: an export within that window repeats them. */
    @Test
    void unchangedRuntimeCpuTime_givesNoCpuPoint() {
        mapper.map(AGENT, List.of(runtimeCpu(5, 1, 100, T1_NS)));

        final MappingResult unchanged = mapper.map(AGENT, List.of(runtimeCpu(5, 1, 100, T2_NS)));

        assertThat(hasCpu(unchanged)).isFalse();
        assertThat(unchanged.getDropped()).isEmpty();
    }

    @Test
    void hostCpuTimes_winOverTheRuntimeEstimate() {
        mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 10, 2, T1_NS), systemCpu(CUMULATIVE, 1000, 3000, T1_NS), runtimeCpu(5, 1, 100, T1_NS)));

        final MappingResult second = mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 20, 4, T2_NS), systemCpu(CUMULATIVE, 1120, 3360, T2_NS),
                runtimeCpu(50, 1, 100, T2_NS)));

        assertThat(cpu(second).getJvmCpuLoad()).isCloseTo(12.0 / 480, within(1e-9));
    }

    @Test
    void deltaCpuTimes_areUsedAsSent() {
        final MappingResult result = mapper.map(AGENT, List.of(processCpu(DELTA, 10, 2, T1_NS), systemCpu(DELTA, 120, 360, T1_NS)));

        assertThat(cpu(result).getJvmCpuLoad()).isCloseTo(12.0 / 480, within(1e-9));
        assertThat(cpu(result).getSystemCpuLoad()).isCloseTo(120.0 / 480, within(1e-9));
    }

    /** Without the mode, idle time cannot be told apart: no system CPU, and so no process share either. */
    @Test
    void systemCpuTimeWithoutAMode_isInvalid() {
        final Metric noMode = counter(SYSTEM_CPU_TIME, "s", DELTA, doublePoint(480, T1_NS));

        final MappingResult result = mapper.map(AGENT, List.of(processCpu(DELTA, 10, 2, T1_NS), noMode));

        assertThat(hasCpu(result)).isFalse();
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
    }

    /** A new process start time is a new baseline, not a negative or huge increase. */
    @Test
    void restart_startsANewBaseline() {
        mapper.map(AGENT, List.of(processCpu(CUMULATIVE, 10, 2, T1_NS), systemCpu(CUMULATIVE, 1000, 3000, T1_NS)));
        final long restartNs = T2_NS - TimeUnit.SECONDS.toNanos(5);
        final Metric restarted = counter(PROCESS_CPU_TIME, "s", CUMULATIVE,
                NumberDataPoint.newBuilder().setAsDouble(0.5).setTimeUnixNano(T2_NS).setStartTimeUnixNano(restartNs)
                        .addAllAttributes(attrs(ATTR_CPU_MODE, "user")).build());

        final MappingResult result = mapper.map(AGENT, List.of(restarted, systemCpu(CUMULATIVE, 1120, 3360, T2_NS)));

        assertThat(hasCpu(result)).isFalse();
        assertThat(result.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isEqualTo(1);
    }

    // ---- GC ----

    private static Metric memory(long timeNs) {
        return upDownCounter(GO_MEMORY_USED, "By",
                intPoint(20_000_000, timeNs, ATTR_MEMORY_TYPE, GoDialect.MEMORY_TYPE_OTHER),
                intPoint(500_000, timeNs, ATTR_MEMORY_TYPE, GoDialect.MEMORY_TYPE_STACK));
    }

    private static JvmGcBo jvmGc(MappingResult result) {
        assertThat(result.getAgentStatBo()).isNotNull();
        assertThat(result.getAgentStatBo().getJvmGcBos()).hasSize(1);
        return result.getAgentStatBo().getJvmGcBos().get(0);
    }

    /** Count = cycles; time = the pause histogram's sum (its count, two pauses per cycle, is not used). */
    @Test
    void gcCyclesAndPauseDuration_areTheOldGcCountAndTime() {
        final MappingResult result = mapper.map(AGENT, List.of(memory(T1_NS),
                counter(GO_MEMORY_GC_CYCLES, "{gc_cycle}", CUMULATIVE, intPoint(4, T1_NS)),
                histogram(GO_MEMORY_GC_PAUSE_DURATION, "s", CUMULATIVE, histogramPoint(8, 0.0032, T1_NS, List.of(), List.of()))));

        assertThat(jvmGc(result).getGcOldCount()).isEqualTo(4);
        assertThat(jvmGc(result).getGcOldTime()).isEqualTo(3);
        assertThat(result.getDropped()).isEmpty();
    }

    @Test
    void gcCycles_winOverTheDeprecatedPauseHistogram() {
        final MappingResult result = mapper.map(AGENT, List.of(memory(T1_NS),
                counter(GO_MEMORY_GC_CYCLES, "{gc_cycle}", CUMULATIVE, intPoint(4, T1_NS)),
                histogram(GO_MEMORY_GC_PAUSE_DURATION, "s", CUMULATIVE, histogramPoint(8, 0.0032, T1_NS, List.of(), List.of())),
                histogram(GO_GC_PAUSE_NS, "ns", CUMULATIVE, histogramPoint(4, 3_200_000, T1_NS, List.of(), List.of()))));

        assertThat(jvmGc(result).getGcOldCount()).isEqualTo(4);
        assertThat(jvmGc(result).getGcOldTime()).isEqualTo(3);
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
    }

    @Test
    void deltaGcCyclesAndPauses_addUpToRunningTotals() {
        final List<Metric> first = new ArrayList<>(List.of(memory(T1_NS),
                counter(GO_MEMORY_GC_CYCLES, "{gc_cycle}", DELTA, intPoint(2, T1_NS)),
                histogram(GO_MEMORY_GC_PAUSE_DURATION, "s", DELTA, histogramPoint(4, 0.002, T1_NS, T1_NS - TimeUnit.SECONDS.toNanos(60), List.of(), List.of()))));
        final List<Metric> second = new ArrayList<>(List.of(memory(T2_NS),
                counter(GO_MEMORY_GC_CYCLES, "{gc_cycle}", DELTA, intPoint(3, T2_NS)),
                histogram(GO_MEMORY_GC_PAUSE_DURATION, "s", DELTA, histogramPoint(6, 0.003, T2_NS, T1_NS, List.of(), List.of()))));

        mapper.map(AGENT, first);
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, second));
        // nothing collected in the third interval: the totals stay
        final JvmGcBo idle = jvmGc(mapper.map(AGENT, List.of(memory(T3_NS))));

        assertThat(gc.getGcOldCount()).isEqualTo(5);
        assertThat(gc.getGcOldTime()).isEqualTo(5);
        assertThat(idle.getGcOldCount()).isEqualTo(5);
        assertThat(idle.getGcOldTime()).isEqualTo(5);
    }

    // ---- captured output: contrib runtime main (after 0.72) with every opt-in + instrumentation/host 0.72 ----

    private static ResourceMetrics load(String name) throws IOException {
        try (InputStream in = GoCpuAndGcTest.class.getResourceAsStream("/fixtures/go-runtime-main-host/" + name)) {
            assertThat(in).as(name).isNotNull();
            return ExportMetricsServiceRequest.parseFrom(in).getResourceMetrics(0);
        }
    }

    /**
     * 10 s exports on a 32-CPU machine, the probe burning about 0.4 s of CPU every 2 s. The host metrics
     * give the CPU every export; go.cpu.time (refreshed every ~15 s) would only every other one.
     */
    @Test
    void capturedExports() throws IOException {
        mapper.map(AGENT, load("cumulative-1.pb"));
        final MappingResult second = mapper.map(AGENT, load("cumulative-2.pb"));
        final MappingResult third = mapper.map(AGENT, load("cumulative-3.pb"));

        assertThat(cpu(second).getJvmCpuLoad()).isBetween(0.001, 0.02);
        assertThat(cpu(second).getSystemCpuLoad()).isBetween(0.01, 0.5);
        assertThat(cpu(third).getJvmCpuLoad()).isBetween(0.001, 0.02);
        final JvmGcBo gc = jvmGc(second);
        assertThat(gc.getGcOldCount()).isEqualTo(4);
        assertThat(gc.getHeapUsed()).isEqualTo(23_252_992);
        assertThat(second.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(43);
        assertThat(second.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isZero();
        assertThat(second.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        // the host CPU times are semantic-convention names of the otel dialect
        assertThat(second.getAccepted(OtelSemconvDialect.NAME)).isPositive();
    }
}
