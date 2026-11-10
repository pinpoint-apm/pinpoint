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

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.trace.inspector.DotnetDialect.DOTNET_PROCESS_CPU_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.START_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.counter;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_CPU_MODE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.PROCESS_CPU_TIME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.SYSTEM_CPU_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * CPU running totals add up only over one interval. A CUMULATIVE series left out of an export (or
 * dropped as invalid) gives no CPU for that export, and its next increase spans two intervals, so the
 * export after gives none either, instead of mixing a 120 s increase with 60 s ones.
 */
class CpuTimeIntervalTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-net", "order-net-1", 1_700_000_000_000L);
    private static final AggregationTemporality CUMULATIVE = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
    private static final AggregationTemporality DELTA = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
    // a steady load on one CPU: 10 % user, 20 % system
    private static final double USER_SHARE = 0.10;
    private static final double SYSTEM_SHARE = 0.20;

    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(
            MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect(), new NodeDialect(),
                    new PythonDialect(), new DotnetDialect()),
            new SeriesStateCache(1000, Duration.ofMinutes(10)));

    private static long at(int minute) {
        return START_NS + TimeUnit.MINUTES.toNanos(minute);
    }

    /** process.cpu.time of the steady load at the minute, without the modes left out. */
    private static Metric processCpu(int minute, String... leftOut) {
        final List<NumberDataPoint> points = new ArrayList<>();
        if (!List.of(leftOut).contains("user")) {
            points.add(doublePoint(USER_SHARE * 60 * minute, at(minute), ATTR_CPU_MODE, "user"));
        }
        if (!List.of(leftOut).contains("system")) {
            points.add(doublePoint(SYSTEM_SHARE * 60 * minute, at(minute), ATTR_CPU_MODE, "system"));
        }
        return counter(PROCESS_CPU_TIME, "s", CUMULATIVE, points.toArray(new NumberDataPoint[0]));
    }

    /** system.cpu.time of a 2-CPU machine where only the process runs, without the modes left out. */
    private static Metric systemCpu(int minute, String... leftOut) {
        final double seconds = 2 * 60 * minute;
        final List<NumberDataPoint> points = new ArrayList<>();
        final double[] shares = {USER_SHARE, SYSTEM_SHARE, 2 - USER_SHARE - SYSTEM_SHARE};
        final String[] modes = {"user", "system", "idle"};
        for (int i = 0; i < modes.length; i++) {
            if (!List.of(leftOut).contains(modes[i])) {
                points.add(doublePoint(shares[i] / 2 * seconds, at(minute), ATTR_CPU_MODE, modes[i]));
            }
        }
        return counter(SYSTEM_CPU_TIME, "s", CUMULATIVE, points.toArray(new NumberDataPoint[0]));
    }

    private static Metric cpuCount(int minute) {
        return upDownCounter(DOTNET_PROCESS_CPU_COUNT, "{cpu}", intPoint(1, at(minute)));
    }

    private @Nullable CpuLoadBo cpu(List<Metric> metrics) {
        final MappingResult result = mapper.map(AGENT, metrics);
        if (result.getAgentStatBo() == null || result.getAgentStatBo().getCpuLoadBos().isEmpty()) {
            return null;
        }
        return result.getAgentStatBo().getCpuLoadBos().get(0);
    }

    /** The review's reproduction: 30 % steady, system left out of one export. */
    @Test
    void processCpuOverTheCpuCount_aLeftOutSeries_givesNoCpu_forThatExportAndTheNext() {
        assertThat(cpu(List.of(processCpu(1), cpuCount(1)))).isNull();
        assertThat(cpu(List.of(processCpu(2), cpuCount(2))).getJvmCpuLoad()).isCloseTo(0.30, within(1e-9));

        // without the check: 10 % (user only)
        assertThat(cpu(List.of(processCpu(3, "system"), cpuCount(3)))).isNull();
        // without the check: 25 % (system over 120 s added to user over 60 s, divided by 120 s)
        assertThat(cpu(List.of(processCpu(4), cpuCount(4)))).isNull();
        assertThat(cpu(List.of(processCpu(5), cpuCount(5))).getJvmCpuLoad()).isCloseTo(0.30, within(1e-9));
    }

    @Test
    void processOverSystemCpu_aLeftOutProcessSeries_givesNoCpu_forThatExportAndTheNext() {
        cpu(List.of(processCpu(1), systemCpu(1)));
        final CpuLoadBo steady = cpu(List.of(processCpu(2), systemCpu(2)));
        assertThat(steady.getJvmCpuLoad()).isCloseTo(0.15, within(1e-9));
        assertThat(steady.getSystemCpuLoad()).isCloseTo(0.15, within(1e-9));

        // the process CPU is the point's primary input: without it there is no CPU point
        assertThat(cpu(List.of(processCpu(3, "user"), systemCpu(3)))).isNull();
        // process over two minutes, system over one: not divided
        assertThat(cpu(List.of(processCpu(4), systemCpu(4)))).isNull();
        final CpuLoadBo back = cpu(List.of(processCpu(5), systemCpu(5)));
        assertThat(back.getJvmCpuLoad()).isCloseTo(0.15, within(1e-9));
        assertThat(back.getSystemCpuLoad()).isCloseTo(0.15, within(1e-9));
    }

    @Test
    void aLeftOutIdleSeries_givesNoSystemCpu_forThatExportAndTheNext() {
        cpu(List.of(processCpu(1), systemCpu(1)));
        cpu(List.of(processCpu(2), systemCpu(2)));

        assertThat(cpu(List.of(processCpu(3), systemCpu(3, "idle")))).isNull();
        assertThat(cpu(List.of(processCpu(4), systemCpu(4)))).isNull();
        final CpuLoadBo back = cpu(List.of(processCpu(5), systemCpu(5)));
        assertThat(back.getJvmCpuLoad()).isCloseTo(0.15, within(1e-9));
        assertThat(back.getSystemCpuLoad()).isCloseTo(0.15, within(1e-9));
    }

    /** A series that stops for good costs one export, not every export after it. */
    @Test
    void aSeriesThatStops_givesCpuAgain_fromTheExportAfter() {
        cpu(List.of(processCpu(1), cpuCount(1)));
        cpu(List.of(processCpu(2), cpuCount(2)));

        assertThat(cpu(List.of(processCpu(3, "system"), cpuCount(3)))).isNull();
        assertThat(cpu(List.of(processCpu(4, "system"), cpuCount(4))).getJvmCpuLoad()).isCloseTo(USER_SHARE, within(1e-9));
    }

    /** A retried export is older than the remembered cycle: it neither fails the check nor moves it. */
    @Test
    void aRetriedExport_doesNotChangeTheRememberedSeries() {
        cpu(List.of(processCpu(1), cpuCount(1)));
        cpu(List.of(processCpu(2), cpuCount(2)));
        cpu(List.of(processCpu(1, "system"), cpuCount(1)));

        assertThat(cpu(List.of(processCpu(3), cpuCount(3))).getJvmCpuLoad()).isCloseTo(0.30, within(1e-9));
    }

    /** Under DELTA the SDK leaves out a series without recordings: that is no CPU used, not a gap. */
    @Test
    void aDeltaSeriesLeftOut_isNoCpuUsed() {
        final Metric both = counter(PROCESS_CPU_TIME, "s", DELTA,
                deltaPoint(6, 1, "user"), deltaPoint(12, 1, "system"));
        final Metric userOnly = counter(PROCESS_CPU_TIME, "s", DELTA, deltaPoint(6, 2, "user"));

        assertThat(cpu(List.of(both, cpuCount(1))).getJvmCpuLoad()).isCloseTo(0.30, within(1e-9));
        assertThat(cpu(List.of(userOnly, cpuCount(2))).getJvmCpuLoad()).isCloseTo(USER_SHARE, within(1e-9));
    }

    private static NumberDataPoint deltaPoint(double seconds, int minute, String mode) {
        return doublePoint(seconds, at(minute), ATTR_CPU_MODE, mode).toBuilder().setStartTimeUnixNano(at(minute - 1)).build();
    }

    /** Points of one collection stamped up to a second apart (one callback each) are one interval. */
    @Test
    void pointsStampedWithinASecond_areOneInterval() {
        final long skew = TimeUnit.MILLISECONDS.toNanos(400);
        cpu(List.of(counter(PROCESS_CPU_TIME, "s", CUMULATIVE,
                doublePoint(6, at(1), ATTR_CPU_MODE, "user"), doublePoint(12, at(1) + skew, ATTR_CPU_MODE, "system")), cpuCount(1)));
        final CpuLoadBo cpu = cpu(List.of(counter(PROCESS_CPU_TIME, "s", CUMULATIVE,
                doublePoint(12, at(2) + skew, ATTR_CPU_MODE, "user"), doublePoint(24, at(2), ATTR_CPU_MODE, "system")), cpuCount(2)));

        assertThat(cpu).isNotNull();
        assertThat(cpu.getJvmCpuLoad()).isCloseTo(0.30, within(0.01));
    }
}
