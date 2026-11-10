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
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.gauge;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogram;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.histogramPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.NodeDialect.ATTR_GC_TYPE;
import static com.navercorp.pinpoint.otlp.trace.inspector.NodeDialect.ATTR_HEAP_SPACE_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.NodeDialect.V8JS_GC_DURATION;
import static com.navercorp.pinpoint.otlp.trace.inspector.NodeDialect.V8JS_MEMORY_HEAP_USED;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OTel JS runtime-node metrics on the Inspector charts: the V8 heap spaces as Heap, the major GC as
 * the old GC; the host CPU times (instrumentation-host-metrics) through the otel dialect.
 */
class NodeDialectTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-node", "order-node-1", 1_700_000_000_000L);
    private static final AggregationTemporality CUMULATIVE = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;

    private final OtlpMetricInspectorMapper mapper = newMapper();

    private static OtlpMetricInspectorMapper newMapper() {
        return new OtlpMetricInspectorMapper(
                MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect(), new NodeDialect()),
                new SeriesStateCache(1000, Duration.ofMinutes(10)));
    }

    /** runtime-node 0.37 sends the heap as a gauge per V8 heap space. */
    private static Metric heapUsed(long timeNs) {
        return gauge(V8JS_MEMORY_HEAP_USED, "By",
                doublePoint(700_000, timeNs, ATTR_HEAP_SPACE_NAME, "new_space"),
                doublePoint(11_500_000, timeNs, ATTR_HEAP_SPACE_NAME, "old_space"),
                doublePoint(1_450_000, timeNs, ATTR_HEAP_SPACE_NAME, "code_space"),
                doublePoint(0, timeNs, ATTR_HEAP_SPACE_NAME, "read_only_space"));
    }

    private static Metric gc(long major, double majorSeconds, long minor, double minorSeconds, long timeNs) {
        return histogram(V8JS_GC_DURATION, "s", CUMULATIVE,
                histogramPoint(major, majorSeconds, timeNs, List.of(), List.of(), ATTR_GC_TYPE, "major"),
                histogramPoint(minor, minorSeconds, timeNs, List.of(), List.of(), ATTR_GC_TYPE, "minor"),
                histogramPoint(3, 0.001, timeNs, List.of(), List.of(), ATTR_GC_TYPE, "incremental"),
                histogramPoint(1, 0.0001, timeNs, List.of(), List.of(), ATTR_GC_TYPE, "weakcb"));
    }

    private static JvmGcBo jvmGc(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        return bo.getJvmGcBos().get(0);
    }

    @Test
    void heapSpaces_addUpToTheHeap_withoutAMaxOrNonHeap() {
        final MappingResult result = mapper.map(AGENT, List.of(heapUsed(T1_NS)));

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getHeapUsed()).isEqualTo(13_650_000);
        assertThat(gc.getHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getNonHeapUsed()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getNonHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.UNKNOWN);
        assertThat(result.getDropped()).isEmpty();
        assertThat(result.getAccepted(NodeDialect.NAME)).isEqualTo(4);
    }

    /** Only the major (Mark-Sweep-Compact) collections are the old GC; scavenges and marking steps are not. */
    @Test
    void majorGc_isTheOldGc_theOtherTypesAreIgnored() {
        final MappingResult first = mapper.map(AGENT, List.of(heapUsed(T1_NS), gc(2, 0.004, 40, 0.02, T1_NS)));
        final MappingResult second = mapper.map(AGENT, List.of(heapUsed(T2_NS), gc(5, 0.0105, 90, 0.05, T2_NS)));

        assertThat(jvmGc(first).getGcOldCount()).isEqualTo(2);
        assertThat(jvmGc(first).getGcOldTime()).isEqualTo(4);
        assertThat(jvmGc(second).getGcOldCount()).isEqualTo(5);
        assertThat(jvmGc(second).getGcOldTime()).isEqualTo(11);
        assertThat(second.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(3);
    }

    @Test
    void otherNodeMetrics_areUnknown() {
        final MappingResult result = mapper.map(AGENT, List.of(
                gauge("nodejs.eventloop.utilization", "1", doublePoint(0.003, T1_NS)),
                gauge("v8js.memory.heap.space.available_size", "By", doublePoint(339_616, T1_NS, ATTR_HEAP_SPACE_NAME, "new_space")),
                gauge("v8js.resource.active", "{resource}", doublePoint(4, T1_NS, "v8js.resource.type", "Timeout"))));

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(3);
    }
}
