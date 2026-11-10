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
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.T1_NS;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.doublePoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.gauge;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.intPoint;
import static com.navercorp.pinpoint.otlp.trace.inspector.MetricFixtures.upDownCounter;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_CONNECTION_POOL_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_CONNECTION_STATE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_POOL_NAME;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.ATTR_STATE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.DB_CLIENT_CONNECTIONS_MAX;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.DB_CLIENT_CONNECTIONS_USAGE;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.DB_CLIENT_CONNECTION_COUNT;
import static com.navercorp.pinpoint.otlp.trace.inspector.OtelSemconvDialect.DB_CLIENT_CONNECTION_MAX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Inspector dataSource chart from connection pool metrics: the OTel Java agent's pool
 * instrumentations (older and stable names) and Micrometer's {@code hikaricp.connections.*}.
 */
class DataSourceTest {

    private static final InspectorAgentIdentity AGENT = new InspectorAgentIdentity("DEFAULT", "order-api", "order-api-1", 1_700_000_000_000L);
    private static final String HIKARI = "io.opentelemetry.hikaricp-3.0";
    private static final String DBCP2 = "io.opentelemetry.apache-dbcp-2.0";
    private static final String C3P0 = "io.opentelemetry.c3p0-0.9";
    private static final String BRIDGE = "io.opentelemetry.micrometer-1.5";
    private static final String CONNECTIONS = "{connections}";

    private final OtlpMetricInspectorMapper mapper = newMapper();

    private static OtlpMetricInspectorMapper newMapper() {
        return new OtlpMetricInspectorMapper(MetricVocabulary.of(new OtelSemconvDialect(), new MicrometerDialect()),
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

    private static Metric usage(String pool, long used, long idle) {
        return upDownCounter(DB_CLIENT_CONNECTIONS_USAGE, CONNECTIONS,
                intPoint(used, T1_NS, ATTR_POOL_NAME, pool, ATTR_STATE, "used"),
                intPoint(idle, T1_NS, ATTR_POOL_NAME, pool, ATTR_STATE, "idle"));
    }

    private static Metric max(String pool, long max) {
        return upDownCounter(DB_CLIENT_CONNECTIONS_MAX, CONNECTIONS, intPoint(max, T1_NS, ATTR_POOL_NAME, pool));
    }

    private static Metric hikariActive(String pool, double active) {
        return gauge(MicrometerDialect.HIKARICP_CONNECTIONS_ACTIVE, "", doublePoint(active, T1_NS, MicrometerDialect.TAG_POOL, pool));
    }

    private static Metric hikariMax(String pool, double max) {
        return gauge(MicrometerDialect.HIKARICP_CONNECTIONS_MAX, "", doublePoint(max, T1_NS, MicrometerDialect.TAG_POOL, pool));
    }

    private static List<DataSourceBo> dataSources(MappingResult result) {
        final AgentStatBo bo = result.getAgentStatBo();
        assertThat(bo).isNotNull();
        assertThat(bo.getDataSourceListBos()).hasSize(1);
        return bo.getDataSourceListBos().get(0).getList();
    }

    private static void assertDataSource(DataSourceBo dataSource, String pool, short serviceTypeCode, int active, int max) {
        assertDataSource(dataSource, pool, pool, serviceTypeCode, active, max);
    }

    private static void assertDataSource(DataSourceBo dataSource, String pool, String jdbcUrl, short serviceTypeCode, int active, int max) {
        assertThat(dataSource.getJdbcUrl()).isEqualTo(jdbcUrl);
        assertThat(dataSource.getDatabaseName()).isEqualTo(pool);
        assertThat(dataSource.getId()).isNotNegative();
        assertThat(dataSource.getServiceTypeCode()).isEqualTo(serviceTypeCode);
        assertThat(dataSource.getActiveConnectionSize()).isEqualTo(active);
        assertThat(dataSource.getMaxConnectionSize()).isEqualTo(max);
    }

    @Test
    void otelPool_usedAndMax_idleIsIgnored() {
        final MappingResult result = mapper.map(AGENT, resource(HIKARI, usage("db-host:3306/orders", 3, 7), HIKARI, max("db-host:3306/orders", 10)));

        final List<DataSourceBo> dataSources = dataSources(result);
        assertThat(dataSources).hasSize(1);
        assertDataSource(dataSources.get(0), "db-host:3306/orders", ConnectionPoolTypes.HIKARICP, 3, 10);
        assertThat(result.getMapped(AgentStatType.DATASOURCE)).isEqualTo(1);
        assertThat(result.getDropped(InspectorDropReason.IGNORED_SERIES)).isEqualTo(1);
        assertThat(result.getAccepted(OtelSemconvDialect.NAME)).isEqualTo(2);
    }

    @Test
    void stableNames_mapTheSame() {
        final Metric count = upDownCounter(DB_CLIENT_CONNECTION_COUNT, "{connection}",
                intPoint(2, T1_NS, ATTR_CONNECTION_POOL_NAME, "orders", ATTR_CONNECTION_STATE, "used"),
                intPoint(8, T1_NS, ATTR_CONNECTION_POOL_NAME, "orders", ATTR_CONNECTION_STATE, "idle"));
        final Metric max = upDownCounter(DB_CLIENT_CONNECTION_MAX, "{connection}", intPoint(10, T1_NS, ATTR_CONNECTION_POOL_NAME, "orders"));

        final List<DataSourceBo> dataSources = dataSources(mapper.map(AGENT, resource(HIKARI, count, HIKARI, max)));

        assertThat(dataSources).hasSize(1);
        assertDataSource(dataSources.get(0), "orders", ConnectionPoolTypes.HIKARICP, 2, 10);
    }

    @Test
    void micrometerHikariCp_isAHikariCpDataSource() {
        final List<DataSourceBo> dataSources = dataSources(mapper.map(AGENT, List.of(hikariActive("HikariPool-1", 4), hikariMax("HikariPool-1", 10))));

        assertThat(dataSources).hasSize(1);
        assertDataSource(dataSources.get(0), "HikariPool-1", ConnectionPoolTypes.HIKARICP, 4, 10);
    }

    /** Spring Boot's {@code jdbc.connections.*} has no active count for HikariCP; it is not read. */
    @Test
    void micrometerJdbcConnections_isNotADataSource() {
        final Metric jdbcMax = gauge("jdbc.connections.max", "", doublePoint(10, T1_NS, "name", "dataSource"));

        final MappingResult result = mapper.map(AGENT, List.of(jdbcMax));

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isEqualTo(1);
    }

    @Test
    void twoPoolLibraries_areTwoDataSources_inNameOrder() {
        final MappingResult result = mapper.map(AGENT, resource(
                HIKARI, usage("orders", 3, 7), HIKARI, max("orders", 10),
                DBCP2, usage("billing", 1, 4), DBCP2, max("billing", 8)));

        final List<DataSourceBo> dataSources = dataSources(result);
        assertThat(dataSources).hasSize(2);
        assertDataSource(dataSources.get(0), "billing", ConnectionPoolTypes.DBCP2, 1, 8);
        assertDataSource(dataSources.get(1), "orders", ConnectionPoolTypes.HIKARICP, 3, 10);
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isZero();
    }

    /**
     * Two libraries' pools of the same name are two pools, each with its own type and id; the agent's
     * web chart tells pools apart by the JDBC URL only, so theirs name the library too.
     */
    @Test
    void samePoolNameInTwoLibraries_staysTwoDataSources_withTheLibraryInTheUrl() {
        final MappingResult result = mapper.map(AGENT, resource(
                HIKARI, usage("orders", 3, 7), HIKARI, max("orders", 10),
                DBCP2, usage("orders", 1, 4), DBCP2, max("orders", 8)));

        final List<DataSourceBo> dataSources = dataSources(result);
        assertThat(dataSources).hasSize(2);
        final DataSourceBo dbcp2 = dataSources.stream().filter(ds -> ds.getServiceTypeCode() == ConnectionPoolTypes.DBCP2).findFirst().orElseThrow();
        final DataSourceBo hikari = dataSources.stream().filter(ds -> ds.getServiceTypeCode() == ConnectionPoolTypes.HIKARICP).findFirst().orElseThrow();
        assertDataSource(dbcp2, "orders", "orders (apache-dbcp-2.0)", ConnectionPoolTypes.DBCP2, 1, 8);
        assertDataSource(hikari, "orders", "orders (hikaricp-3.0)", ConnectionPoolTypes.HIKARICP, 3, 10);
        assertThat(dbcp2.getId()).isNotEqualTo(hikari.getId());
        assertThat(dbcp2.getId()).isEqualTo(InspectorStatSnapshot.dataSourceId("io.opentelemetry.apache-dbcp-2.0", "orders"));
    }

    /** Only the shared name gets the library; a pool whose name is its own keeps the plain name. */
    @Test
    void onlyTheSharedPoolName_carriesTheLibrary() {
        final List<DataSourceBo> dataSources = dataSources(mapper.map(AGENT, resource(
                HIKARI, usage("orders", 3, 7), HIKARI, usage("billing", 2, 3),
                DBCP2, usage("orders", 1, 4))));

        assertThat(dataSources).extracting(DataSourceBo::getJdbcUrl)
                .containsExactly("billing", "orders (apache-dbcp-2.0)", "orders (hikaricp-3.0)");
        assertThat(InspectorStatSnapshot.libraryLabel("")).isEqualTo("no scope");
        assertThat(InspectorStatSnapshot.libraryLabel("com.example.pool")).isEqualTo("com.example.pool");
    }

    /** The id is the library (scope without its version) and the name: an instrumentation upgrade keeps the series. */
    @Test
    void poolId_ignoresTheScopeVersion() {
        final List<DataSourceBo> before = dataSources(mapper.map(AGENT, resource(HIKARI + "@2.31.1", usage("orders", 3, 7))));
        final List<DataSourceBo> after = dataSources(newMapper().map(AGENT, resource(HIKARI + "@2.32.0", usage("orders", 3, 7))));

        assertThat(before.get(0).getId()).isEqualTo(after.get(0).getId()).isEqualTo(InspectorStatSnapshot.dataSourceId(HIKARI, "orders"));
        assertThat(OtlpMetricInspectorMapper.scopeName("@opentelemetry/instrumentation-host-metrics@0.7.0")).isEqualTo("@opentelemetry/instrumentation-host-metrics");
        assertThat(OtlpMetricInspectorMapper.scopeName("@opentelemetry/instrumentation-host-metrics")).isEqualTo("@opentelemetry/instrumentation-host-metrics");
        assertThat(OtlpMetricInspectorMapper.scopeName("")).isEmpty();
    }

    @Test
    void poolLibraryWithoutAPinpointPlugin_isUnknownDb() {
        final List<DataSourceBo> dataSources = dataSources(mapper.map(AGENT, resource(C3P0, usage("orders", 1, 1), C3P0, max("orders", 2))));

        assertDataSource(dataSources.get(0), "orders", ConnectionPoolTypes.UNKNOWN, 1, 2);
        assertThat(ConnectionPoolTypes.UNKNOWN).isEqualTo((short) 2050);
    }

    /** The Micrometer bridge reports the same pool again (under HikariCP's own name): the OTel pool is kept. */
    @Test
    void bridgedHikariCp_nextToTheAgentsPool_isADuplicateSource() {
        final MappingResult result = mapper.map(AGENT, resource(
                BRIDGE, hikariActive("HikariPool-1", 3), BRIDGE, hikariMax("HikariPool-1", 10),
                HIKARI, usage("orders", 3, 7), HIKARI, max("orders", 10)));

        final List<DataSourceBo> dataSources = dataSources(result);
        assertThat(dataSources).hasSize(1);
        assertDataSource(dataSources.get(0), "orders", ConnectionPoolTypes.HIKARICP, 3, 10);
        assertThat(result.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isEqualTo(2);
    }

    @Test
    void poolWithoutAMax_hasAnUncollectedMax_andAMaxAlone_isNoDataSource() {
        assertDataSource(dataSources(mapper.map(AGENT, resource(HIKARI, usage("orders", 3, 7)))).get(0),
                "orders", ConnectionPoolTypes.HIKARICP, 3, (int) InspectorStatSnapshot.UNCOLLECTED);

        final MappingResult maxOnly = newMapper().map(AGENT, resource(HIKARI, max("orders", 10)));
        assertThat(maxOnly.getAgentStatBo()).isNull();
        assertThat(maxOnly.getAccepted(OtelSemconvDialect.NAME)).isEqualTo(1);
    }

    @Test
    void usageWithoutStateOrPoolName_isInvalid() {
        final Metric noState = upDownCounter(DB_CLIENT_CONNECTIONS_USAGE, CONNECTIONS, intPoint(3, T1_NS, ATTR_POOL_NAME, "orders"));
        final Metric noPool = upDownCounter(DB_CLIENT_CONNECTIONS_USAGE, CONNECTIONS, intPoint(3, T1_NS, ATTR_STATE, "used"));

        final MappingResult result = mapper.map(AGENT, resource(HIKARI, noState, HIKARI, noPool));

        assertThat(result.getAgentStatBo()).isNull();
        assertThat(result.getDropped(InspectorDropReason.INVALID_POINT)).isEqualTo(2);
    }

    // ---- captured output (Spring Boot 3.5.16, HikariCP 6.3.3, in-memory H2 "probe", maximum-pool-size=5) ----

    private static ResourceMetrics load(String path) throws IOException {
        try (InputStream in = DataSourceTest.class.getResourceAsStream("/fixtures/" + path)) {
            assertThat(in).as(path).isNotNull();
            return ExportMetricsServiceRequest.parseFrom(in).getResourceMetrics(0);
        }
    }

    /**
     * The javaagent names a HikariCP pool left at its generated name after the JDBC URL ("probe" for
     * {@code jdbc:h2:mem:probe}); with the bridge on, the bridged {@code hikaricp.*} pool
     * ("HikariPool-1") is the same pool and left out.
     */
    @ParameterizedTest
    @ValueSource(strings = {"default", "stable", "bridge"})
    void capturedJavaagentExports(String config) throws IOException {
        final OtlpMetricInspectorMapper capturedMapper = newMapper();
        final MappingResult idle = capturedMapper.map(AGENT, load("otel-javaagent-2.31-datasource/" + config + "-1.pb"));
        final MappingResult busy = capturedMapper.map(AGENT, load("otel-javaagent-2.31-datasource/" + config + "-2.pb"));

        assertDataSource(dataSources(idle).get(0), "probe", ConnectionPoolTypes.HIKARICP, 0, 5);
        final List<DataSourceBo> dataSources = dataSources(busy);
        assertThat(dataSources).hasSize(1);
        assertDataSource(dataSources.get(0), "probe", ConnectionPoolTypes.HIKARICP, 3, 5);
        if (config.equals("bridge")) {
            assertThat(busy.getDropped(InspectorDropReason.DUPLICATE_SOURCE)).isPositive();
        }
    }

    @Test
    void capturedMicrometerExports() throws IOException {
        final OtlpMetricInspectorMapper capturedMapper = newMapper();
        final MappingResult idle = capturedMapper.map(AGENT, load("micrometer-1.15-datasource/cumulative-1.pb"));
        final MappingResult busy = capturedMapper.map(AGENT, load("micrometer-1.15-datasource/cumulative-2.pb"));

        assertDataSource(dataSources(idle).get(0), "HikariPool-1", ConnectionPoolTypes.HIKARICP, 0, 5);
        final List<DataSourceBo> dataSources = dataSources(busy);
        assertThat(dataSources).hasSize(1);
        assertDataSource(dataSources.get(0), "HikariPool-1", ConnectionPoolTypes.HIKARICP, 3, 5);
        // jdbc.connections.max and .min of the same pool are not read
        assertThat(busy.getDropped(InspectorDropReason.UNKNOWN_METRIC)).isPositive();
    }
}
