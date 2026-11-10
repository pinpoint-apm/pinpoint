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
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.ATTR_GC_HEAP_GENERATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.ATTR_GENERATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.DOTNET_GC_COLLECTIONS;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.DOTNET_GC_LAST_COLLECTION_HEAP_SIZE;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.DOTNET_GC_PAUSE_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.DOTNET_PROCESS_CPU_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.DOTNET_PROCESS_CPU_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.RUNTIME_GC_COLLECTIONS_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.RUNTIME_GC_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.RUNTIME_GC_OBJECTS_SIZE;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_CPU_MODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * The .NET runtime metrics on the Inspector charts, under the .NET 9+ {@code System.Runtime} names and
 * the .NET 8 runtime instrumentation names: heap, gen2 collections with the total GC pause, and the
 * process CPU over the CPU count.
 */
class DotnetDialectTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-net", "order-net-1", 1_700_000_000_000L);
    private static final AggregationTemporality CUMULATIVE = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
    private static final String SYSTEM_RUNTIME = "System.Runtime";
    private static final String PROCESS_INSTRUMENTATION = "OpenTelemetry.Instrumentation.Process";

    private final OtlpMetricInspectorMapper mapper = newMapper();

    private static OtlpMetricInspectorMapper newMapper() {
        return new OtlpMetricInspectorMapper(
                MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect(), new NodeDialect(),
                        new PythonDialect(), new DotnetDialect()),
                new SeriesStateCache(1000, Duration.ofMinutes(10)));
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

    private static Metric heap9(long timeNs) {
        return upDownCounter(DOTNET_GC_LAST_COLLECTION_HEAP_SIZE, "By",
                intPoint(0, timeNs, ATTR_GC_HEAP_GENERATION, "gen0"), intPoint(40_000, timeNs, ATTR_GC_HEAP_GENERATION, "gen1"),
                intPoint(2_500_000, timeNs, ATTR_GC_HEAP_GENERATION, "gen2"), intPoint(18_400_000, timeNs, ATTR_GC_HEAP_GENERATION, "loh"),
                intPoint(90_000, timeNs, ATTR_GC_HEAP_GENERATION, "poh"));
    }

    private static Metric collections9(long gen0, long gen1, long gen2, long timeNs) {
        return counter(DOTNET_GC_COLLECTIONS, "{collection}", CUMULATIVE,
                intPoint(gen0, timeNs, ATTR_GC_HEAP_GENERATION, "gen0"), intPoint(gen1, timeNs, ATTR_GC_HEAP_GENERATION, "gen1"),
                intPoint(gen2, timeNs, ATTR_GC_HEAP_GENERATION, "gen2"));
    }

    private static Metric cpuTime(String name, double user, double system, long timeNs) {
        return counter(name, "s", CUMULATIVE, doublePoint(user, timeNs, ATTR_CPU_MODE, "user"), doublePoint(system, timeNs, ATTR_CPU_MODE, "system"));
    }

    private static Metric cpuCount(long count, long timeNs) {
        return upDownCounter(DOTNET_PROCESS_CPU_COUNT, "{cpu}", intPoint(count, timeNs));
    }

    private static JvmGcBo jvmGc(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        return bo.getJvmGcBos().get(0);
    }

    private static boolean hasCpu(MappingResult result) {
        return result.getAgentStatBo() != null && !result.getAgentStatBo().getCpuLoadBos().isEmpty();
    }

    @Test
    void dotnet9_heapIsTheSumOfTheGenerations_afterTheLatestCollection() {
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, List.of(heap9(T1_NS))));

        assertThat(gc.getHeapUsed()).isEqualTo(21_030_000);
        assertThat(gc.getHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getNonHeapUsed()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
    }

    /** gen2 collections are the old GC; the time is the pause of every generation (.NET does not split it). */
    @Test
    void dotnet9_gen2IsTheOldGcCount_andThePauseTimeTheTime() {
        final MappingResult result = mapper.map(AGENT, List.of(heap9(T1_NS), collections9(40, 6, 3, T1_NS),
                counter(DOTNET_GC_PAUSE_TIME, "s", CUMULATIVE, doublePoint(0.0113, T1_NS))));

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getGcOldCount()).isEqualTo(3);
        assertThat(gc.getGcOldTime()).isEqualTo(11);
        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(2);
    }

    @Test
    void dotnet8_liveObjectsAreTheHeap_gcDurationInNanoseconds() {
        final MappingResult result = mapper.map(AGENT, List.of(
                upDownCounter(RUNTIME_GC_OBJECTS_SIZE, "bytes", intPoint(38_548_072, T1_NS)),
                counter(RUNTIME_GC_COLLECTIONS_COUNT, "", CUMULATIVE,
                        intPoint(0, T1_NS, ATTR_GENERATION, "gen0"), intPoint(0, T1_NS, ATTR_GENERATION, "gen1"),
                        intPoint(1, T1_NS, ATTR_GENERATION, "gen2")),
                counter(RUNTIME_GC_DURATION, "ns", CUMULATIVE, intPoint(71_227_000, T1_NS))));

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(38_548_072);
        assertThat(gc.getGcOldCount()).isEqualTo(1);
        assertThat(gc.getGcOldTime()).isEqualTo(71);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
    }

    /** 4 CPUs over 60 s = 240 CPU-seconds; the process used 12 of them. No system CPU metric. */
    @Test
    void dotnet9_processCpuOverTheElapsedTimeTimesTheCpuCount() {
        mapper.map(AGENT, List.of(cpuTime(DOTNET_PROCESS_CPU_TIME, 10, 2, T1_NS), cpuCount(4, T1_NS)));

        final MappingResult second = mapper.map(AGENT, List.of(cpuTime(DOTNET_PROCESS_CPU_TIME, 19, 5, T2_NS), cpuCount(4, T2_NS)));

        final CpuLoadBo cpu = second.getAgentStatBo().getCpuLoadBos().get(0);
        assertThat(cpu.getJvmCpuLoad()).isCloseTo(12.0 / 240, within(1e-9));
        assertThat(cpu.getSystemCpuLoad()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
    }

    /** An idle interval is 0 %, not a gap. */
    @Test
    void dotnet9_idleInterval_isZeroPercent() {
        mapper.map(AGENT, List.of(cpuTime(DOTNET_PROCESS_CPU_TIME, 10, 2, T1_NS), cpuCount(4, T1_NS)));

        final MappingResult second = mapper.map(AGENT, List.of(cpuTime(DOTNET_PROCESS_CPU_TIME, 10, 2, T2_NS), cpuCount(4, T2_NS)));

        assertThat(second.getAgentStatBo().getCpuLoadBos()).hasSize(1);
        assertThat(second.getAgentStatBo().getCpuLoadBos().get(0).getJvmCpuLoad()).isZero();
    }

    /** CPU seconds sent in milliseconds: the ratio over the elapsed time is the same. */
    @Test
    void dotnet9_cpuTimeInMilliseconds_isConverted() {
        mapper.map(AGENT, List.of(counter(DOTNET_PROCESS_CPU_TIME, "ms", CUMULATIVE,
                doublePoint(10_000, T1_NS, ATTR_CPU_MODE, "user"), doublePoint(2_000, T1_NS, ATTR_CPU_MODE, "system")), cpuCount(4, T1_NS)));

        final MappingResult second = mapper.map(AGENT, List.of(counter(DOTNET_PROCESS_CPU_TIME, "ms", CUMULATIVE,
                doublePoint(19_000, T2_NS, ATTR_CPU_MODE, "user"), doublePoint(5_000, T2_NS, ATTR_CPU_MODE, "system")), cpuCount(4, T2_NS)));

        assertThat(second.getAgentStatBo().getCpuLoadBos().get(0).getJvmCpuLoad()).isCloseTo(12.0 / 240, within(1e-9));
    }

    /** With OpenTelemetry.Instrumentation.Process the semantic-convention process.cpu.time is used, divided the same way. */
    @Test
    void processInstrumentationCpuTime_winsOverTheRuntimes() {
        mapper.map(AGENT, resource(PROCESS_INSTRUMENTATION, cpuTime(OtelSemconvDialect.PROCESS_CPU_TIME, 10, 2, T1_NS),
                SYSTEM_RUNTIME, cpuTime(DOTNET_PROCESS_CPU_TIME, 10, 2, T1_NS), SYSTEM_RUNTIME, cpuCount(4, T1_NS)));

        final MappingResult second = mapper.map(AGENT, resource(PROCESS_INSTRUMENTATION, cpuTime(OtelSemconvDialect.PROCESS_CPU_TIME, 19, 5, T2_NS),
                SYSTEM_RUNTIME, cpuTime(DOTNET_PROCESS_CPU_TIME, 25, 5, T2_NS), SYSTEM_RUNTIME, cpuCount(4, T2_NS)));

        assertThat(second.getAgentStatBo().getCpuLoadBos().get(0).getJvmCpuLoad()).isCloseTo(12.0 / 240, within(1e-9));
        assertThat(second.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(2);
    }

    /** .NET 8 with the process instrumentation: a CPU time but no CPU count, so no CPU. */
    @Test
    void processCpuTimeWithoutACpuCount_givesNoCpu() {
        mapper.map(AGENT, List.of(cpuTime(OtelSemconvDialect.PROCESS_CPU_TIME, 10, 2, T1_NS)));

        assertThat(hasCpu(mapper.map(AGENT, List.of(cpuTime(OtelSemconvDialect.PROCESS_CPU_TIME, 19, 5, T2_NS))))).isFalse();
    }

    // ---- captured output ----

    private static ResourceMetrics load(String path) throws IOException {
        try (InputStream in = DotnetDialectTest.class.getResourceAsStream("/fixtures/" + path)) {
            assertThat(in).as(path).isNotNull();
            return ExportMetricsServiceRequest.parseFrom(in).getResourceMetrics(0);
        }
    }

    /** ASP.NET Core 8, OpenTelemetry .NET 1.20.1 runtime instrumentation. */
    @Test
    void capturedDotnet8() throws IOException {
        mapper.map(AGENT, load("dotnet-aspnetcore-8/cumulative-1.pb"));
        final MappingResult result = mapper.map(AGENT, load("dotnet-aspnetcore-8/cumulative-2.pb"));

        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.JVM_GC, AgentStatType.RESPONSE_TIME);
        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(38_548_072);
        assertThat(gc.getGcOldCount()).isEqualTo(1);
        assertThat(gc.getGcOldTime()).isEqualTo(71);
    }

    /** ASP.NET Core 9 (System.Runtime meter) + OpenTelemetry.Instrumentation.Process 1.20.1-rc.1, 32 CPUs, 10 s exports. */
    @Test
    void capturedDotnet9WithTheProcessInstrumentation() throws IOException {
        mapper.map(AGENT, load("dotnet-9-runtime-process/cumulative-1.pb"));
        final MappingResult result = mapper.map(AGENT, load("dotnet-9-runtime-process/cumulative-2.pb"));

        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.JVM_GC, AgentStatType.CPU_LOAD, AgentStatType.TOTAL_THREAD,
                AgentStatType.RESPONSE_TIME);
        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(21_032_840);
        assertThat(gc.getGcOldCount()).isEqualTo(6);
        assertThat(gc.getGcOldTime()).isEqualTo(11);
        final CpuLoadBo cpu = result.getAgentStatBo().getCpuLoadBos().get(0);
        assertThat(cpu.getJvmCpuLoad()).isCloseTo(0.0020, within(0.0002));
        assertThat(cpu.getSystemCpuLoad()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        // process.thread.count of the process instrumentation (all threads, not the thread pool's)
        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(51);
        // dotnet.process.cpu.time repeats the process instrumentation's
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(2);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
    }
}
