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
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
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
 * A point stamped ahead of the collector's clock (a client clock that jumped forward) is dropped
 * before it reaches the series state. The state only moves forward in time, so without the check one
 * such point would turn every later, correctly stamped point of the series into "already covered" for
 * as long as the agent keeps exporting.
 */
class FutureTimestampTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-api", "order-api-1", 1_700_000_000_000L);
    private static final long MINUTE_NS = TimeUnit.MINUTES.toNanos(1);
    private static final long T1_NS = START_NS + MINUTE_NS;
    private static final long T2_NS = START_NS + 2 * MINUTE_NS;
    private static final long T3_NS = START_NS + 3 * MINUTE_NS;
    private static final long FUTURE_NS = T2_NS + TimeUnit.DAYS.toNanos(1);
    private static final List<Double> BOUNDS = List.of(0.05, 0.1, 0.25, 0.5);

    // the collector's clock follows the exports
    private final AtomicLong nowMs = new AtomicLong();
    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(MetricVocabulary.of(new OtelSemconvDialect()),
            new SeriesStateCache(1000, Duration.ofMinutes(10)), nowMs::get);

    private MappingResult mapAt(long nowNs, Metric... metrics) {
        nowMs.set(TimeUnit.NANOSECONDS.toMillis(nowNs));
        return mapper.map(AGENT, List.of(metrics));
    }

    private static Metric http(long count, double sumSeconds, long timeNs) {
        return histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(count, sumSeconds, timeNs, BOUNDS, List.of(count, 0L, 0L, 0L, 0L)));
    }

    private static Metric threads(long value, long timeNs) {
        return upDownCounter(JVM_THREAD_COUNT, "{thread}", intPoint(value, timeNs));
    }

    private static Metric heap(long timeNs) {
        return upDownCounter(JVM_MEMORY_USED, "By",
                intPoint(100, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"));
    }

    private static Metric deltaOldGc(long count, double sumSeconds, long timeNs) {
        return histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA,
                histogramPoint(count, sumSeconds, timeNs, timeNs - MINUTE_NS, List.of(), List.of(), ATTR_GC_NAME, "G1 Old Generation"));
    }

    @Test
    void futurePoint_isDroppedAndStoresNothing() {
        final MappingResult result = mapAt(T2_NS, threads(12, FUTURE_NS), http(10, 1.0, FUTURE_NS));

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(2);
    }

    @Test
    void futurePoint_doesNotFreezeTheCumulativeBaseline() {
        // baseline
        assertThat(mapAt(T1_NS, http(10, 1.0, T1_NS)).getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isEqualTo(1);
        // the client clock jumps a day ahead for one export
        final MappingResult jumped = mapAt(T2_NS, http(15, 2.0, FUTURE_NS));
        assertThat(jumped.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
        assertThat(jumped.getAgentStatBo()).isNull();

        // back to the right time: measured from the last usable point (T1), not refused as "old"
        final MappingResult next = mapAt(T3_NS, http(20, 3.0, T3_NS));

        assertThat(next.getDropped(InspectorDropReason.NO_PREVIOUS_POINT)).isZero();
        final ResponseTimeBo responseTime = next.getAgentStatBo().getResponseTimeBos().get(0);
        // (3.0 - 1.0) s over (20 - 10) requests
        assertThat(responseTime.getAvg()).isEqualTo(200);
        assertThat(responseTime.getDataPoint().getTimestamp()).isEqualTo(TimeUnit.NANOSECONDS.toMillis(T3_NS));
    }

    @Test
    void futurePoint_doesNotFreezeTheDeltaTotals() {
        mapAt(T1_NS, heap(T1_NS), deltaOldGc(1, 0.010, T1_NS));
        // a future DELTA increment would become the group's latest time and every later one "too old"
        final MappingResult jumped = mapAt(T2_NS, heap(T2_NS), deltaOldGc(5, 0.050, FUTURE_NS));
        assertThat(jumped.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);

        final AgentStatBo next = mapAt(T3_NS, heap(T3_NS), deltaOldGc(2, 0.020, T3_NS)).getAgentStatBo();

        // 1 + 2: the dropped increment is not counted, the later one is
        assertThat(next.getJvmGcBos().get(0).getGcOldCount()).isEqualTo(3);
        assertThat(next.getJvmGcBos().get(0).getGcOldTime()).isEqualTo(30);
    }

    @Test
    void pointWithinTheAllowedSkew_isUsed() {
        // the client clock is 4 minutes ahead of the collector's: inside the 5 minute allowance
        final long aheadNs = T2_NS + TimeUnit.MINUTES.toNanos(4);

        final MappingResult result = mapAt(T2_NS, threads(12, aheadNs));

        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
        assertThat(result.getAgentStatBo().getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(12);
    }

    @Test
    void futureCounterPoint_isDropped() {
        final Metric unloaded = counter(OtelSemconvDialect.JVM_CLASS_UNLOADED, "{class}", AGGREGATION_TEMPORALITY_CUMULATIVE,
                intPoint(3, FUTURE_NS));

        final MappingResult result = mapAt(T2_NS, unloaded);

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
    }
}
