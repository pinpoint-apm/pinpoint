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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maps OTel db.system / db.system.name attribute values to Pinpoint ServiceType codes.
 * Prefers db.system.name (semconv 2.x) keys; db.system (1.x deprecated) keys are aliases.
 *
 * <p>Codes are resolved from {@link ServiceTypeRegistryService} by ServiceType <em>name</em>
 * (e.g. {@code "MYSQL"}, {@code "MSSQL_JDBC"}), so a plugin re-mapping its code does not
 * desync this resolver. If a plugin is not deployed, its name is absent from the registry
 * and the resolver gracefully falls back to {@link ServiceType#UNKNOWN_DB} /
 * {@link ServiceType#UNKNOWN_DB_EXECUTE_QUERY}, which share the property set of plugin-defined
 * DB base / execute-query ServiceTypes (TERMINAL + INCLUDE_DESTINATION_ID, with additionally
 * RECORD_STATISTICS on the execute-query variant). Resolution happens eagerly at construction
 * so per-span lookups stay {@code O(1)}.</p>
 */
public class OtlpDbSystemTypeResolver {

    // OTel db.system / db.system.name key → {baseTypeName, executeQueryTypeName}.
    // baseTypeName == executeQueryTypeName when the plugin has no separate execute-query
    // ServiceType (redis, elasticsearch, hbase).
    private static final Map<String, String[]> NAME_MAP;

    static {
        Map<String, String[]> map = new HashMap<>();

        // SQL
        put(map, "MYSQL", "MYSQL_EXECUTE_QUERY", "mysql");
        put(map, "MARIADB", "MARIADB_EXECUTE_QUERY", "mariadb");
        put(map, "MSSQL_JDBC", "MSSQL_JDBC_QUERY", "microsoft.sql_server", "mssql");
        put(map, "ORACLE", "ORACLE_EXECUTE_QUERY", "oracle.db", "oracle");
        put(map, "POSTGRESQL", "POSTGRESQL_EXECUTE_QUERY", "postgresql");
        put(map, "DB2", "DB2_EXECUTE_QUERY", "ibm.db2", "db2");
        put(map, "INFORMIX", "INFORMIX_EXECUTE_QUERY", "ibm.informix", "informix");
        put(map, "H2", "H2_EXECUTE_QUERY", "h2database", "h2");
        put(map, "CLICK_HOUSE", "CLICK_HOUSE_EXECUTE_QUERY", "clickhouse");

        // NoSQL
        put(map, "CASSANDRA4", "CASSANDRA4_EXECUTE_QUERY", "cassandra");
        put(map, "MONGO", "MONGO_EXECUTE_QUERY", "mongodb");
        put(map, "COUCHDB", "COUCHDB_EXECUTE_QUERY", "couchdb");

        // Cache / Search — no separate execute-query type in existing plugins.
        put(map, "REDIS", "REDIS", "redis");
        put(map, "ELASTICSEARCH", "ELASTICSEARCH", "elasticsearch", "opensearch");
        put(map, "HBASE_CLIENT", "HBASE_CLIENT", "hbase");

        NAME_MAP = Map.copyOf(map);
    }

    private static void put(Map<String, String[]> map, String baseName, String executeQueryName, String... dbSystemKeys) {
        String[] names = {baseName, executeQueryName};
        for (String key : dbSystemKeys) {
            map.put(key, names);
        }
    }

    private static final short DEFAULT_BASE = ServiceType.UNKNOWN_DB.getCode();
    private static final short DEFAULT_EXECUTE_QUERY = ServiceType.UNKNOWN_DB_EXECUTE_QUERY.getCode();

    // dbSystem key → {baseCode, executeQueryCode}, eagerly resolved at construction.
    private final Map<String, short[]> codeMap;

    public OtlpDbSystemTypeResolver(ServiceTypeRegistryService registry) {
        Objects.requireNonNull(registry, "registry");
        Map<String, short[]> resolved = new HashMap<>();
        NAME_MAP.forEach((dbSystem, names) -> {
            short base = resolveCode(registry, names[0], DEFAULT_BASE);
            short executeQuery = resolveCode(registry, names[1], DEFAULT_EXECUTE_QUERY);
            resolved.put(dbSystem, new short[]{base, executeQuery});
        });
        this.codeMap = Map.copyOf(resolved);
    }

    private static short resolveCode(ServiceTypeRegistryService registry, String typeName, short fallback) {
        ServiceType type = registry.findServiceTypeByName(typeName);
        if (type == null || type == ServiceType.UNDEFINED) {
            return fallback;
        }
        return type.getCode();
    }

    short resolveBaseCode(String dbSystem) {
        if (dbSystem == null) {
            return DEFAULT_BASE;
        }
        short[] codes = codeMap.get(dbSystem);
        return codes != null ? codes[0] : DEFAULT_BASE;
    }

    short resolveExecuteQueryCode(String dbSystem) {
        if (dbSystem == null) {
            return DEFAULT_EXECUTE_QUERY;
        }
        short[] codes = codeMap.get(dbSystem);
        return codes != null ? codes[1] : DEFAULT_EXECUTE_QUERY;
    }
}
