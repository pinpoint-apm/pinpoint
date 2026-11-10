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
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.metrics.v1.DataPointFlags;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T2_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.attrs;
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
 * Instrumentation scope in the series identity, points that carry no value (FLAG_NO_RECORDED_VALUE,
 * no value field) and histograms without the optional sum.
 */
class ScopeAndMissingValueTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-api", "order-api-1", 1_700_000_000_000L);
    private static final long T3_NS = START_NS + TimeUnit.SECONDS.toNanos(180);
    private static final int NO_RECORDED_VALUE = DataPointFlags.DATA_POINT_FLAGS_NO_RECORDED_VALUE_MASK_VALUE;
    private static final String[] ROUTE = {"http.route", "/a", "http.request.method", "GET"};

    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(new OtelSemconvDialect(),
            new SeriesStateCache(1000, Duration.ofMinutes(10)));

    private static ResourceMetrics resource(Object... scopeAndMetrics) {
        final ResourceMetrics.Builder builder = ResourceMetrics.newBuilder();
        for (int i = 0; i < scopeAndMetrics.length; i += 2) {
            builder.addScopeMetrics(ScopeMetrics.newBuilder()
                    .setScope(InstrumentationScope.newBuilder().setName((String) scopeAndMetrics[i]))
                    .addMetrics((Metric) scopeAndMetrics[i + 1]));
        }
        return builder.build();
    }

    private static Metric http(long count, double sumSeconds, long timeNs) {
        return histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE,
                histogramPoint(count, sumSeconds, timeNs, List.of(), List.of(), ROUTE));
    }

    private static Metric threads(NumberDataPoint point) {
        return upDownCounter(JVM_THREAD_COUNT, "{thread}", point);
    }

    private static Metric heap(long timeNs) {
        return upDownCounter(JVM_MEMORY_USED, "By", intPoint(100, timeNs, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"));
    }

    private static HistogramDataPoint oldGcDelta(long count, double sumSeconds, long timeNs) {
        return histogramPoint(count, sumSeconds, timeNs, timeNs - TimeUnit.SECONDS.toNanos(60), List.of(), List.of(),
                ATTR_GC_NAME, "G1 Old Generation", "jvm.gc.action", "end of major GC");
    }

    private static Metric gcDelta(HistogramDataPoint point) {
        return histogram(JVM_GC_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA, point);
    }

    private static HistogramDataPoint withoutSum(HistogramDataPoint point) {
        return point.toBuilder().clearSum().build();
    }

    private static NumberDataPoint flagged(NumberDataPoint point) {
        return point.toBuilder().setFlags(NO_RECORDED_VALUE).build();
    }

    private static HistogramDataPoint flagged(HistogramDataPoint point) {
        return point.toBuilder().setFlags(NO_RECORDED_VALUE).build();
    }

    private static ResponseTimeBo onlyResponseTime(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getResponseTimeBos()).hasSize(1);
        return bo.getResponseTimeBos().get(0);
    }

    // ---- instrumentation scope ----

    @Test
    void scopeKey_isTheNamePlusTheVersion() {
        assertThat(OtlpMetricPointReader.scopeKey(InstrumentationScope.newBuilder().setName("io.opentelemetry.tomcat-10.0").build()))
                .isEqualTo("io.opentelemetry.tomcat-10.0");
        assertThat(OtlpMetricPointReader.scopeKey(InstrumentationScope.newBuilder().setName("app").setVersion("1.2").build()))
                .isEqualTo("app@1.2");
        assertThat(OtlpMetricPointReader.scopeKey(InstrumentationScope.getDefaultInstance())).isEmpty();
    }

    /** Two scopes with the same name and attributes are two series: their requests add up in one point. */
    @Test
    void serverDurationOfTwoScopes_areSeparateSeries_andAddUp() {
        mapper.map(AGENT, resource("scope.a", http(1, 0.1, T1_NS), "scope.b", http(1, 0.1, T1_NS)));
        // scope.a: +1 request of 100 ms, scope.b: +1 request of 300 ms
        final MappingResult second = mapper.map(AGENT, resource("scope.a", http(2, 0.2, T2_NS), "scope.b", http(2, 0.4, T2_NS)));

        assertThat(second.getDropped()).isEmpty();
        final ResponseTimeBo responseTime = onlyResponseTime(second);
        assertThat(responseTime.getAvg()).isEqualTo(200);
        // no buckets: each series' interval mean, the point keeps the largest
        assertThat(responseTime.getMax()).isEqualTo(300);
    }

    /** The same runtime series from two scopes describes one JVM twice: one scope (in key order) is kept. */
    @Test
    void runtimeSeriesOfTwoScopes_isCountedOnce() {
        final MappingResult result = mapper.map(AGENT, resource(
                "scope.b", threads(intPoint(12, T1_NS)),
                "scope.a", threads(intPoint(14, T1_NS))));

        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo.getTotalThreadCountBos()).hasSize(1);
        assertThat(bo.getTotalThreadCountBos().get(0).getTotalThreadCount()).isEqualTo(14);
    }

    /** One Inspector value comes from one source, even when the sources report different series of it. */
    @Test
    void oneRoleFromTwoScopes_usesOneScope_evenForDifferentSeries() {
        final MappingResult result = mapper.map(AGENT, resource(
                "scope.a", upDownCounter(JVM_MEMORY_USED, "By", intPoint(100, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Eden Space")),
                "scope.b", upDownCounter(JVM_MEMORY_USED, "By", intPoint(300, T1_NS, ATTR_MEMORY_TYPE, "heap", ATTR_MEMORY_POOL_NAME, "G1 Old Gen"))));

        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(1);
        final JvmGcBo gc = result.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getHeapUsed()).isEqualTo(100);
    }

    // ---- no recorded value ----

    @Test
    void flaggedNumberPoint_isNotAZero() {
        final MappingResult result = mapper.map(AGENT, List.of(threads(flagged(intPoint(0, T1_NS)))));

        assertThat(result.getDropped(InspectorDropReason.NO_RECORDED_VALUE)).isEqualTo(1);
        assertThat(result.getAgentStatBo()).isNull();
    }

    @Test
    void numberPointWithoutValue_isNotAZero() {
        final NumberDataPoint noValue = NumberDataPoint.newBuilder().setTimeUnixNano(T1_NS).setStartTimeUnixNano(START_NS).build();
        final MappingResult result = mapper.map(AGENT, List.of(threads(noValue), heap(T1_NS)));

        assertThat(result.getDropped(InspectorDropReason.NO_RECORDED_VALUE)).isEqualTo(1);
        assertThat(result.getAgentStatBo().getTotalThreadCountBos()).isEmpty();
    }

    /** A flagged cumulative point must not become the baseline of the next interval. */
    @Test
    void flaggedCumulativeHistogram_keepsTheBaseline() {
        mapper.map(AGENT, List.of(http(1, 0.1, T1_NS)));
        final HistogramDataPoint stale = flagged(histogramPoint(0, 0, T2_NS, List.of(), List.of(), ROUTE));
        final MappingResult staleResult = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, stale)));
        // +2 requests, +0.4 s since the first point
        final MappingResult third = mapper.map(AGENT, List.of(http(3, 0.5, T3_NS)));

        assertThat(staleResult.getDropped(InspectorDropReason.NO_RECORDED_VALUE)).isEqualTo(1);
        assertThat(staleResult.getAgentStatBo()).isNull();
        assertThat(onlyResponseTime(third).getAvg()).isEqualTo(200);
    }

    @Test
    void flaggedDeltaHistogram_leavesTheTotalUnchanged() {
        final MappingResult first = mapper.map(AGENT, List.of(heap(T1_NS), gcDelta(oldGcDelta(1, 0.1, T1_NS))));
        mapper.map(AGENT, List.of(heap(T2_NS), gcDelta(flagged(oldGcDelta(5, 0.5, T2_NS)))));
        final MappingResult third = mapper.map(AGENT, List.of(heap(T3_NS), gcDelta(oldGcDelta(1, 0.2, T3_NS))));

        assertThat(first.getAgentStatBo().getJvmGcBos().get(0).getGcOldCount()).isEqualTo(1);
        final JvmGcBo gc = third.getAgentStatBo().getJvmGcBos().get(0);
        assertThat(gc.getGcOldCount()).isEqualTo(2);
        assertThat(gc.getGcOldTime()).isEqualTo(300);
    }

    // ---- histogram without sum ----

    @Test
    void cumulativeHistogramWithoutSum_isInvalid_andKeepsTheBaseline() {
        mapper.map(AGENT, List.of(http(1, 0.1, T1_NS)));
        final HistogramDataPoint noSum = withoutSum(histogramPoint(2, 0, T2_NS, List.of(), List.of(), ROUTE));
        final MappingResult second = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_CUMULATIVE, noSum)));
        final MappingResult third = mapper.map(AGENT, List.of(http(3, 0.5, T3_NS)));

        assertThat(second.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
        assertThat(second.getAgentStatBo()).isNull();
        assertThat(onlyResponseTime(third).getAvg()).isEqualTo(200);
    }

    @Test
    void deltaHistogramWithoutSum_isInvalid_notAZeroAverage() {
        final HistogramDataPoint noSum = withoutSum(HistogramDataPoint.newBuilder().setCount(2).setMax(0.12)
                .setTimeUnixNano(T1_NS).setStartTimeUnixNano(T1_NS - TimeUnit.SECONDS.toNanos(60))
                .addAllAttributes(attrs(ROUTE)).build());
        final MappingResult result = mapper.map(AGENT, List.of(histogram(HTTP_SERVER_REQUEST_DURATION, "s", AGGREGATION_TEMPORALITY_DELTA, noSum)));

        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
        assertThat(result.getAgentStatBo()).isNull();
    }

    @Test
    void gcHistogramWithoutSum_isInvalid_andLeavesTheTotalUnchanged() {
        mapper.map(AGENT, List.of(heap(T1_NS), gcDelta(oldGcDelta(1, 0.1, T1_NS))));
        final MappingResult second = mapper.map(AGENT, List.of(heap(T2_NS), gcDelta(withoutSum(oldGcDelta(4, 0, T2_NS)))));

        assertThat(second.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(1);
        final JvmGcBo gc = second.getAgentStatBo().getJvmGcBos().get(0);
        // the total carried forward, not 1 + 4 with an unknown time
        assertThat(gc.getGcOldCount()).isEqualTo(1);
        assertThat(gc.getGcOldTime()).isEqualTo(100);
    }
}
