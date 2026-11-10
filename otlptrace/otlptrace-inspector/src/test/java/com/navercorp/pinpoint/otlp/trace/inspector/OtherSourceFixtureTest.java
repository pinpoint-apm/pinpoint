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
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real output of the sources other than the OTel Java agent's default setup and Micrometer (see
 * {@code fixtures/README-other-sources.md}): OTel SDK used directly (Spring Boot starter), JFR runtime
 * metrics, other collectors, the 1.x agent, and the Go, .NET, Node.js and Python SDKs. Go, .NET, Node.js
 * and Python (system-metrics) runtime metrics map through their dialects ({@code GoDialectTest},
 * {@code DotnetDialectTest}, {@code NodeDialectTest}, {@code PythonDialectTest}); the Python fixtures
 * here leave the system-metrics scope out, so those map to Response Time only.
 */
class OtherSourceFixtureTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "x.src", "src-1", 0);
    private static final long HEAP_512M = 512L * 1024 * 1024;

    private final OtlpMetricInspectorMapper mapper = new OtlpMetricInspectorMapper(
            MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect(), new GoDialect(), new NodeDialect(), new PythonDialect(), new DotnetDialect()),
            new SeriesStateCache(1000, Duration.ofMinutes(10)));

    private static List<Metric> load(String path) throws IOException {
        try (InputStream in = OtherSourceFixtureTest.class.getResourceAsStream("/fixtures/" + path)) {
            assertThat(in).as(path).isNotNull();
            final ExportMetricsServiceRequest request = ExportMetricsServiceRequest.parseFrom(in);
            assertThat(request.getResourceMetricsCount()).isEqualTo(1);
            final ResourceMetrics resourceMetrics = request.getResourceMetrics(0);
            final List<Metric> metrics = new ArrayList<>();
            for (ScopeMetrics scopeMetrics : resourceMetrics.getScopeMetricsList()) {
                metrics.addAll(scopeMetrics.getMetricsList());
            }
            return metrics;
        }
    }

    /** Maps two consecutive cumulative exports and returns the second (the one with a response time). */
    private MappingResult mapPair(String dir) throws IOException {
        mapper.map(AGENT, load(dir + "/cumulative-1.pb"));
        return mapper.map(AGENT, load(dir + "/cumulative-2.pb"));
    }

    private static JvmGcBo jvmGc(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getJvmGcBos()).hasSize(1);
        return bo.getJvmGcBos().get(0);
    }

    // ---------------------------------------------------------------- JVM sources

    @Test
    void springBootStarter_otelSdkWithoutAgent_mapsAllButTheExperimentalCharts() throws IOException {
        final MappingResult result = mapPair("otel-spring-starter-2.28");

        assertThat(result.getMapped(AgentStatType.JVM_GC)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.CPU_LOAD)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.TOTAL_THREAD)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.LOADED_CLASS)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.RESPONSE_TIME)).isEqualTo(1);
        // jvm.buffer.* and jvm.system.cpu.utilization are experimental and off by default
        assertThat(result.getMapped(AgentStatType.DIRECT_BUFFER)).isZero();
        final CpuLoadBo cpu = result.getAgentStatBo().getCpuLoadBos().get(0);
        assertThat(cpu.getSystemCpuLoad()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);

        final JvmGcBo gc = jvmGc(result);
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.G1);
        assertThat(gc.getHeapMax()).isEqualTo(HEAP_512M);
        assertThat(gc.getNonHeapMax()).isEqualTo(InspectorStatSnapshot.UNCOLLECTED);
    }

    @Test
    void jfrRuntimeMetrics_keepTheJmxCollectorNames() throws IOException {
        final MappingResult result = mapPair("otel-javaagent-2.31-jfr");

        assertThat(result.getMapped(AgentStatType.DIRECT_BUFFER)).isEqualTo(1);
        assertThat(result.getMapped(AgentStatType.RESPONSE_TIME)).isEqualTo(1);
        final JvmGcBo gc = jvmGc(result);
        // G1 Young/Old Generation, as with JMX: classified as with the agent
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.G1);
        assertThat(gc.getHeapMax()).isEqualTo(HEAP_512M);
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isZero();
    }

    @Test
    void parallelGc_isParallel_heapMaxIsTheSumOfThePoolLimits() throws IOException {
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, load("otel-javaagent-2.31-gc/parallel.pb")));

        assertThat(gc.getGcType()).isEqualTo(JvmGcType.PARALLEL);
        // PS Eden + PS Survivor + PS Old limits; the agent's MemoryMXBean max is lower (Eden is resized)
        assertThat(gc.getHeapMax()).isEqualTo(142_606_336L + 13_107_200L + 358_088_704L);
    }

    @Test
    void serialGc_isSerial_heapMaxMatchesTheAgent() throws IOException {
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, load("otel-javaagent-2.31-gc/serial.pb")));

        assertThat(gc.getGcType()).isEqualTo(JvmGcType.SERIAL);
        assertThat(gc.getHeapMax()).isEqualTo(518_979_584L);
    }

    @Test
    void zgcOnJdk17_isZgc() throws IOException {
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, load("otel-javaagent-2.31-gc/zgc-jdk17.pb")));

        assertThat(gc.getGcType()).isEqualTo(JvmGcType.ZGC);
        assertThat(gc.getHeapMax()).isEqualTo(HEAP_512M);
    }

    @Test
    void shenandoah_hasNoGcType() throws IOException {
        final JvmGcBo gc = jvmGc(mapper.map(AGENT, load("otel-javaagent-2.31-gc/shenandoah.pb")));

        // the agent has no Shenandoah type either
        assertThat(gc.getGcType()).isEqualTo(JvmGcType.UNKNOWN);
        assertThat(gc.getHeapMax()).isEqualTo(HEAP_512M);
    }

    @Test
    void javaagent1x_oldMetricNames_storeNothing() throws IOException {
        final List<Metric> metrics = load("otel-javaagent-1.33/cumulative.pb");
        final MappingResult result = mapper.map(AGENT, metrics);

        // process.runtime.jvm.* (pre-semconv names) are not recognised
        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(result.getReceivedPoints());
    }

    // ---------------------------------------------------------------- non-JVM sources

    private static void assertResponseTimeOnly(MappingResult result) {
        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.RESPONSE_TIME);
        assertThat(result.getMapped(AgentStatType.RESPONSE_TIME)).isEqualTo(1);
        final ResponseTimeBo responseTime = result.getAgentStatBo().getResponseTimeBos().get(0);
        // the probes answer within ~120 ms: seconds are converted, not taken as milliseconds
        assertThat(responseTime.getAvg()).isBetween(1L, 200L);
        assertThat(responseTime.getMax()).isGreaterThanOrEqualTo(responseTime.getAvg());
        // everything else the SDK sends is a runtime metric without an Inspector chart
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isPositive();
    }

    /** The Go runtime metrics map through the go dialect (see {@code GoDialectTest}). */
    @Test
    void go_mapsHeapThreadsAndResponseTime() throws IOException {
        assertThat(mapper.map(AGENT, load("go-otelhttp-0.72/cumulative-1.pb")).getMapped()).doesNotContainKey(AgentStatType.RESPONSE_TIME);
        final MappingResult result = mapper.map(AGENT, load("go-otelhttp-0.72/cumulative-2.pb"));

        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.JVM_GC, AgentStatType.TOTAL_THREAD, AgentStatType.RESPONSE_TIME);
        final ResponseTimeBo responseTime = result.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isBetween(1L, 200L);
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isPositive();
    }

    @Test
    void dotnet_mapsHeapGcAndResponseTime() throws IOException {
        // .NET 8 runtime instrumentation: the heap and GC map through the dotnet dialect (see DotnetDialectTest)
        final MappingResult result = mapPair("dotnet-aspnetcore-8");

        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.JVM_GC, AgentStatType.RESPONSE_TIME);
        final ResponseTimeBo responseTime = result.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isBetween(1L, 200L);
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isPositive();
    }

    @Test
    void nodeJs_mapsHeapAndResponseTime_withoutOptIn() throws IOException {
        // sdk-node 0.300 / instrumentation-http 0.300 emit http.server.request.duration by default; the
        // V8 heap and GC map through the nodejs dialect (see NodeDialectTest)
        final MappingResult result = mapPair("node-sdk-node-0.300");

        assertThat(result.getMapped()).containsOnlyKeys(AgentStatType.JVM_GC, AgentStatType.RESPONSE_TIME);
        final ResponseTimeBo responseTime = result.getAgentStatBo().getResponseTimeBos().get(0);
        assertThat(responseTime.getAvg()).isBetween(1L, 200L);
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isPositive();
    }

    @Test
    void python_oldHttpMetricName_storesNothing() throws IOException {
        // without OTEL_SEMCONV_STABILITY_OPT_IN=http the instrumentation emits http.server.duration (ms)
        final MappingResult result = mapper.map(AGENT, load("python-0.66b2/default.pb"));
        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(result.getReceivedPoints());
    }

    @Test
    void python_optIn_mapsResponseTimeOnly() throws IOException {
        mapper.map(AGENT, load("python-0.66b2/opt-in-1.pb"));
        assertResponseTimeOnly(mapper.map(AGENT, load("python-0.66b2/opt-in-2.pb")));
    }
}
