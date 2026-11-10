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
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.PythonDialect.CPYTHON_GC_COLLECTIONS;
import static com.navercorp.pinpoint.otlp.trace.inspector.PythonDialect.CPYTHON_MEMORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * The CPython system-metrics on the Inspector charts: RSS as Heap, full collections as the old GC count;
 * the process/system CPU times with the older {@code type}/{@code state} attributes and the thread count
 * through the otel dialect.
 */
class PythonDialectTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-py", "order-py-1", 1_700_000_000_000L);
    private static final AggregationTemporality CUMULATIVE = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;

    private final OtlpMetricInspectorMapper mapper = newMapper();

    private static OtlpMetricInspectorMapper newMapper() {
        return new OtlpMetricInspectorMapper(
                MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect(), new NodeDialect(), new PythonDialect()),
                new SeriesStateCache(1000, Duration.ofMinutes(10)));
    }

    private static Metric memory(long rss, long vms, long timeNs) {
        return upDownCounter(CPYTHON_MEMORY, "By",
                intPoint(rss, timeNs, PythonDialect.ATTR_MEMORY_TYPE, "rss"),
                intPoint(vms, timeNs, PythonDialect.ATTR_MEMORY_TYPE, "vms"));
    }

    /** cpython.gc.generation is an int attribute, next to the older string attribute generation. */
    private static NumberDataPoint generation(long collections, long generation, long timeNs) {
        return NumberDataPoint.newBuilder().setAsInt(collections).setTimeUnixNano(timeNs).setStartTimeUnixNano(START_NS)
                .addAttributes(KeyValue.newBuilder().setKey(PythonDialect.ATTR_GC_GENERATION).setValue(AnyValue.newBuilder().setIntValue(generation)))
                .addAttributes(KeyValue.newBuilder().setKey("generation").setValue(AnyValue.newBuilder().setStringValue(String.valueOf(generation))))
                .build();
    }

    private static Metric gcCollections(long gen0, long gen1, long gen2, long timeNs) {
        return counter(CPYTHON_GC_COLLECTIONS, "{collection}", CUMULATIVE,
                generation(gen0, 0, timeNs), generation(gen1, 1, timeNs), generation(gen2, 2, timeNs));
    }

    private static JvmGcBo jvmGc(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        return bo.getJvmGcBos().get(0);
    }

    @Test
    void rssIsTheHeap_vmsIsIgnored() {
        final MappingResult result = mapper.map(AGENT, List.of(memory(92_000_000, 74_000_000, T1_NS)));

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(92_000_000);
        assertThat(gc.getHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getNonHeapUsed()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(1);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
    }

    /** Only full collections (generation 2) are the old GC; CPython reports no GC time. */
    @Test
    void generation2_isTheOldGcCount_withoutTime() {
        final MappingResult result = mapper.map(AGENT, List.of(memory(92_000_000, 74_000_000, T1_NS), gcCollections(57, 5, 2, T1_NS)));

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getGcOldCount()).isEqualTo(2);
        assertThat(gc.getGcOldTime()).isZero();
        // vms and generations 0, 1
        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(3);
    }

    @Test
    void processThreadCount_isTheTotalThreadCount() {
        final MappingResult result = mapper.map(AGENT, List.of(upDownCounter(OtelSemconvDialect.PROCESS_THREAD_COUNT, "", intPoint(6, T1_NS))));

        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(6);
        assertThat(result.getAccepted(OtelSemconvDialect.NAME)).isEqualTo(1);
    }

    /** system-metrics sends process.cpu.time{type} and system.cpu.time{cpu, state}: the older attribute names. */
    @Test
    void cpuTimesWithTheOlderStateAttribute() {
        final Metric process1 = counter(OtelSemconvDialect.PROCESS_CPU_TIME, "s", CUMULATIVE,
                doublePoint(3.0, T1_NS, "type", "user"), doublePoint(1.0, T1_NS, "type", "system"));
        final Metric system1 = counter(OtelSemconvDialect.SYSTEM_CPU_TIME, "s", CUMULATIVE,
                doublePoint(100, T1_NS, "cpu", "1", "state", "user"), doublePoint(900, T1_NS, "cpu", "1", "state", "idle"),
                doublePoint(100, T1_NS, "cpu", "2", "state", "user"), doublePoint(900, T1_NS, "cpu", "2", "state", "idle"));
        final Metric process2 = counter(OtelSemconvDialect.PROCESS_CPU_TIME, "s", CUMULATIVE,
                doublePoint(9.0, T2_NS, "type", "user"), doublePoint(3.0, T2_NS, "type", "system"));
        final Metric system2 = counter(OtelSemconvDialect.SYSTEM_CPU_TIME, "s", CUMULATIVE,
                doublePoint(130, T2_NS, "cpu", "1", "state", "user"), doublePoint(930, T2_NS, "cpu", "1", "state", "idle"),
                doublePoint(110, T2_NS, "cpu", "2", "state", "user"), doublePoint(1010, T2_NS, "cpu", "2", "state", "idle"));

        final MappingResult first = mapper.map(AGENT, List.of(process1, system1));
        final MappingResult second = mapper.map(AGENT, List.of(process2, system2));

        assertThat(first.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        final CpuLoadBo cpu = second.getAgentStatBo().getCpuLoadBos().get(0);
        // the machine: 40 busy + 140 idle = 180 CPU-seconds; the process used 8
        assertThat(cpu.getJvmCpuLoad()).isCloseTo(8.0 / 180, within(1e-9));
        assertThat(cpu.getSystemCpuLoad()).isCloseTo(40.0 / 180, within(1e-9));
        assertThat(second.getDropped()).isEmpty();
    }
}
