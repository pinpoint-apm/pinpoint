package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpDbSystemTypeResolverTest {

    private final OtlpDbSystemTypeResolver resolver = new OtlpDbSystemTypeResolver();

    // =======================================================================
    // resolveBaseCode — db.system.name (2.x)
    // =======================================================================

    @Test
    void resolveBaseCode_sql() {
        assertThat(resolver.resolveBaseCode("mysql")).isEqualTo((short) 2100);
        assertThat(resolver.resolveBaseCode("mariadb")).isEqualTo((short) 2150);
        assertThat(resolver.resolveBaseCode("microsoft.sql_server")).isEqualTo((short) 2250);
        assertThat(resolver.resolveBaseCode("oracle.db")).isEqualTo((short) 2300);
        assertThat(resolver.resolveBaseCode("postgresql")).isEqualTo((short) 2500);
        assertThat(resolver.resolveBaseCode("ibm.db2")).isEqualTo((short) 2160);
        assertThat(resolver.resolveBaseCode("ibm.informix")).isEqualTo((short) 2450);
        assertThat(resolver.resolveBaseCode("h2database")).isEqualTo((short) 2750);
        assertThat(resolver.resolveBaseCode("clickhouse")).isEqualTo((short) 2800);
    }

    @Test
    void resolveBaseCode_nosql() {
        assertThat(resolver.resolveBaseCode("cassandra")).isEqualTo((short) 2602);
        assertThat(resolver.resolveBaseCode("mongodb")).isEqualTo((short) 2650);
        assertThat(resolver.resolveBaseCode("couchdb")).isEqualTo((short) 2700);
    }

    @Test
    void resolveBaseCode_cacheAndSearch() {
        assertThat(resolver.resolveBaseCode("redis")).isEqualTo((short) 8200);
        assertThat(resolver.resolveBaseCode("elasticsearch")).isEqualTo((short) 9203);
        assertThat(resolver.resolveBaseCode("opensearch")).isEqualTo((short) 9203);
        assertThat(resolver.resolveBaseCode("hbase")).isEqualTo((short) 8800);
    }

    // =======================================================================
    // resolveExecuteQueryCode — db.system.name (2.x)
    // =======================================================================

    @Test
    void resolveExecuteQueryCode_sql() {
        assertThat(resolver.resolveExecuteQueryCode("mysql")).isEqualTo((short) 2101);
        assertThat(resolver.resolveExecuteQueryCode("mariadb")).isEqualTo((short) 2151);
        assertThat(resolver.resolveExecuteQueryCode("microsoft.sql_server")).isEqualTo((short) 2251);
        assertThat(resolver.resolveExecuteQueryCode("oracle.db")).isEqualTo((short) 2301);
        assertThat(resolver.resolveExecuteQueryCode("postgresql")).isEqualTo((short) 2501);
        assertThat(resolver.resolveExecuteQueryCode("ibm.db2")).isEqualTo((short) 2161);
        assertThat(resolver.resolveExecuteQueryCode("ibm.informix")).isEqualTo((short) 2451);
        assertThat(resolver.resolveExecuteQueryCode("h2database")).isEqualTo((short) 2751);
        assertThat(resolver.resolveExecuteQueryCode("clickhouse")).isEqualTo((short) 2801);
    }

    @Test
    void resolveExecuteQueryCode_nosql() {
        assertThat(resolver.resolveExecuteQueryCode("cassandra")).isEqualTo((short) 2603);
        assertThat(resolver.resolveExecuteQueryCode("mongodb")).isEqualTo((short) 2651);
        assertThat(resolver.resolveExecuteQueryCode("couchdb")).isEqualTo((short) 2701);
    }

    @Test
    void resolveExecuteQueryCode_cacheAndSearch_sameAsBase() {
        assertThat(resolver.resolveExecuteQueryCode("redis")).isEqualTo((short) 8200);
        assertThat(resolver.resolveExecuteQueryCode("elasticsearch")).isEqualTo((short) 9203);
        assertThat(resolver.resolveExecuteQueryCode("opensearch")).isEqualTo((short) 9203);
        assertThat(resolver.resolveExecuteQueryCode("hbase")).isEqualTo((short) 8800);
    }

    // =======================================================================
    // 1.x db.system aliases
    // =======================================================================

    @Test
    void resolveBaseCode_legacyAliases() {
        assertThat(resolver.resolveBaseCode("mssql")).isEqualTo((short) 2250);
        assertThat(resolver.resolveBaseCode("oracle")).isEqualTo((short) 2300);
        assertThat(resolver.resolveBaseCode("db2")).isEqualTo((short) 2160);
        assertThat(resolver.resolveBaseCode("informix")).isEqualTo((short) 2450);
        assertThat(resolver.resolveBaseCode("h2")).isEqualTo((short) 2750);
    }

    @Test
    void resolveExecuteQueryCode_legacyAliases() {
        assertThat(resolver.resolveExecuteQueryCode("mssql")).isEqualTo((short) 2251);
        assertThat(resolver.resolveExecuteQueryCode("oracle")).isEqualTo((short) 2301);
        assertThat(resolver.resolveExecuteQueryCode("db2")).isEqualTo((short) 2161);
        assertThat(resolver.resolveExecuteQueryCode("informix")).isEqualTo((short) 2451);
        assertThat(resolver.resolveExecuteQueryCode("h2")).isEqualTo((short) 2751);
    }

    // =======================================================================
    // null / unknown → OPENTELEMETRY_DB fallback
    // =======================================================================

    @Test
    void resolveBaseCode_null_returnsOtlpDefault() {
        assertThat(resolver.resolveBaseCode(null))
                .isEqualTo(ServiceType.OPENTELEMETRY_DB.getCode());
    }

    @Test
    void resolveExecuteQueryCode_null_returnsOtlpDefault() {
        assertThat(resolver.resolveExecuteQueryCode(null))
                .isEqualTo(ServiceType.OPENTELEMETRY_DB_EXECUTE_QUERY.getCode());
    }

    @Test
    void resolveBaseCode_unknown_returnsOtlpDefault() {
        assertThat(resolver.resolveBaseCode("some_unknown_db"))
                .isEqualTo(ServiceType.OPENTELEMETRY_DB.getCode());
    }

    @Test
    void resolveExecuteQueryCode_unknown_returnsOtlpDefault() {
        assertThat(resolver.resolveExecuteQueryCode("some_unknown_db"))
                .isEqualTo(ServiceType.OPENTELEMETRY_DB_EXECUTE_QUERY.getCode());
    }
}
