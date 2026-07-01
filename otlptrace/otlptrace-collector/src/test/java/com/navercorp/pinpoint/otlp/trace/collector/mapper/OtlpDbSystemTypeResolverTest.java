package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpDbSystemTypeResolverTest {

    // Stubbed registry pre-populated with every ServiceType name OtlpDbSystemTypeResolver
    // looks up. Codes mirror the agent plugin definitions / commons ServiceType doc table.
    private static final ServiceTypeRegistryService REGISTRY = mockRegistry();

    private final OtlpDbSystemTypeResolver resolver = new OtlpDbSystemTypeResolver(REGISTRY);

    private static ServiceTypeRegistryService mockRegistry() {
        Map<String, ServiceType> byName = new HashMap<>();
        addType(byName, (short) 2100, "MYSQL");
        addType(byName, (short) 2101, "MYSQL_EXECUTE_QUERY");
        addType(byName, (short) 2150, "MARIADB");
        addType(byName, (short) 2151, "MARIADB_EXECUTE_QUERY");
        addType(byName, (short) 2250, "MSSQL_JDBC");
        addType(byName, (short) 2251, "MSSQL_JDBC_QUERY");
        addType(byName, (short) 2300, "ORACLE");
        addType(byName, (short) 2301, "ORACLE_EXECUTE_QUERY");
        addType(byName, (short) 2500, "POSTGRESQL");
        addType(byName, (short) 2501, "POSTGRESQL_EXECUTE_QUERY");
        addType(byName, (short) 2160, "DB2");
        addType(byName, (short) 2161, "DB2_EXECUTE_QUERY");
        addType(byName, (short) 2450, "INFORMIX");
        addType(byName, (short) 2451, "INFORMIX_EXECUTE_QUERY");
        addType(byName, (short) 2750, "H2");
        addType(byName, (short) 2751, "H2_EXECUTE_QUERY");
        addType(byName, (short) 2800, "CLICK_HOUSE");
        addType(byName, (short) 2801, "CLICK_HOUSE_EXECUTE_QUERY");
        addType(byName, (short) 2602, "CASSANDRA4");
        addType(byName, (short) 2603, "CASSANDRA4_EXECUTE_QUERY");
        addType(byName, (short) 2650, "MONGO");
        addType(byName, (short) 2651, "MONGO_EXECUTE_QUERY");
        addType(byName, (short) 2700, "COUCHDB");
        addType(byName, (short) 2701, "COUCHDB_EXECUTE_QUERY");
        addType(byName, (short) 8200, "REDIS");
        addType(byName, (short) 9203, "ELASTICSEARCH");
        addType(byName, (short) 8800, "HBASE_CLIENT");
        return new MapBackedRegistry(byName);
    }

    private static void addType(Map<String, ServiceType> map, short code, String name) {
        map.put(name, ServiceTypeFactory.of(code, name, name));
    }

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
    void resolveBaseCode_normalizesDbSystem() {
        assertThat(resolver.resolveBaseCode(" H2DATABASE ")).isEqualTo((short) 2750);
        assertThat(resolver.resolveBaseCode(" H2 ")).isEqualTo((short) 2750);
    }

    @Test
    void resolveExecuteQueryCode_legacyAliases() {
        assertThat(resolver.resolveExecuteQueryCode("mssql")).isEqualTo((short) 2251);
        assertThat(resolver.resolveExecuteQueryCode("oracle")).isEqualTo((short) 2301);
        assertThat(resolver.resolveExecuteQueryCode("db2")).isEqualTo((short) 2161);
        assertThat(resolver.resolveExecuteQueryCode("informix")).isEqualTo((short) 2451);
        assertThat(resolver.resolveExecuteQueryCode("h2")).isEqualTo((short) 2751);
    }

    @Test
    void resolveExecuteQueryCode_normalizesDbSystem() {
        assertThat(resolver.resolveExecuteQueryCode(" H2DATABASE ")).isEqualTo((short) 2751);
        assertThat(resolver.resolveExecuteQueryCode(" H2 ")).isEqualTo((short) 2751);
    }

    // =======================================================================
    // null / unknown → UNKNOWN_DB fallback
    // =======================================================================

    @Test
    void resolveBaseCode_null_returnsUnknownDbFallback() {
        assertThat(resolver.resolveBaseCode(null))
                .isEqualTo(ServiceType.UNKNOWN_DB.getCode());
    }

    @Test
    void resolveExecuteQueryCode_null_returnsUnknownDbFallback() {
        assertThat(resolver.resolveExecuteQueryCode(null))
                .isEqualTo(ServiceType.UNKNOWN_DB_EXECUTE_QUERY.getCode());
    }

    @Test
    void resolveBaseCode_unknown_returnsUnknownDbFallback() {
        assertThat(resolver.resolveBaseCode("some_unknown_db"))
                .isEqualTo(ServiceType.UNKNOWN_DB.getCode());
    }

    @Test
    void resolveExecuteQueryCode_unknown_returnsUnknownDbFallback() {
        assertThat(resolver.resolveExecuteQueryCode("some_unknown_db"))
                .isEqualTo(ServiceType.UNKNOWN_DB_EXECUTE_QUERY.getCode());
    }

    // =======================================================================
    // Plugin missing from registry → graceful fallback (new behavior)
    // =======================================================================

    @Test
    void resolveBaseCode_unregisteredPlugin_fallsBackToUnknownDb() {
        // Build an empty registry — no plugin types registered. Every dbSystem key should
        // resolve to the UNKNOWN_DB fallback rather than returning a stale code.
        OtlpDbSystemTypeResolver bareResolver = new OtlpDbSystemTypeResolver(new MapBackedRegistry(Map.of()));
        assertThat(bareResolver.resolveBaseCode("mysql"))
                .isEqualTo(ServiceType.UNKNOWN_DB.getCode());
        assertThat(bareResolver.resolveExecuteQueryCode("mysql"))
                .isEqualTo(ServiceType.UNKNOWN_DB_EXECUTE_QUERY.getCode());
    }

    /**
     * Minimal {@link ServiceTypeRegistryService} stub backed by a name → ServiceType map.
     * Matches the real {@link com.navercorp.pinpoint.common.profiler.trace.ServiceTypeRegistry}
     * contract: unknown names return {@link ServiceType#UNDEFINED} rather than throwing.
     */
    private static final class MapBackedRegistry implements ServiceTypeRegistryService {
        private final Map<String, ServiceType> byName;

        MapBackedRegistry(Map<String, ServiceType> byName) {
            this.byName = byName;
        }

        @Override
        public ServiceType findServiceType(int serviceType) {
            return byName.values().stream()
                    .filter(t -> t.getCode() == serviceType)
                    .findFirst()
                    .orElse(ServiceType.UNDEFINED);
        }

        @Override
        public ServiceType findServiceTypeByName(String typeName) {
            return byName.getOrDefault(typeName, ServiceType.UNDEFINED);
        }

        @Override
        public List<ServiceType> findDesc(String desc) {
            return List.of();
        }
    }
}
