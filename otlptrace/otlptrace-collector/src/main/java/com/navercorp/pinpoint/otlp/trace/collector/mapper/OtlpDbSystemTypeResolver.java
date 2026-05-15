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

import java.util.HashMap;
import java.util.Map;

/**
 * Maps OTel db.system / db.system.name attribute values to Pinpoint ServiceType codes.
 * Prefers db.system.name (semconv 2.x) keys; db.system (1.x deprecated) keys are aliases.
 */
public class OtlpDbSystemTypeResolver {

    // {baseCode, executeQueryCode}
    private static final Map<String, short[]> DB_SYSTEM_MAP;

    static {
        Map<String, short[]> map = new HashMap<>();

        // SQL
        put(map, (short) 2100, (short) 2101, "mysql");
        put(map, (short) 2150, (short) 2151, "mariadb");
        put(map, (short) 2250, (short) 2251, "microsoft.sql_server", "mssql");
        put(map, (short) 2300, (short) 2301, "oracle.db", "oracle");
        put(map, (short) 2500, (short) 2501, "postgresql");
        put(map, (short) 2160, (short) 2161, "ibm.db2", "db2");
        put(map, (short) 2450, (short) 2451, "ibm.informix", "informix");
        put(map, (short) 2750, (short) 2751, "h2database", "h2");
        put(map, (short) 2800, (short) 2801, "clickhouse");

        // NoSQL
        put(map, (short) 2602, (short) 2603, "cassandra");
        put(map, (short) 2650, (short) 2651, "mongodb");
        put(map, (short) 2700, (short) 2701, "couchdb");

        // Cache / Search (no separate execute-query type in existing plugins)
        put(map, (short) 8200, (short) 8200, "redis");
        put(map, (short) 9203, (short) 9203, "elasticsearch", "opensearch");
        put(map, (short) 8800, (short) 8800, "hbase");

        DB_SYSTEM_MAP = Map.copyOf(map);
    }

    private static void put(Map<String, short[]> map, short base, short executeQuery, String... keys) {
        short[] codes = {base, executeQuery};
        for (String key : keys) {
            map.put(key, codes);
        }
    }

    private static final short DEFAULT_BASE = ServiceType.OPENTELEMETRY_DB.getCode();
    private static final short DEFAULT_EXECUTE_QUERY = ServiceType.OPENTELEMETRY_DB_EXECUTE_QUERY.getCode();

    short resolveBaseCode(String dbSystem) {
        if (dbSystem == null) {
            return DEFAULT_BASE;
        }
        short[] codes = DB_SYSTEM_MAP.get(dbSystem);
        return codes != null ? codes[0] : DEFAULT_BASE;
    }

    short resolveExecuteQueryCode(String dbSystem) {
        if (dbSystem == null) {
            return DEFAULT_EXECUTE_QUERY;
        }
        short[] codes = DB_SYSTEM_MAP.get(dbSystem);
        return codes != null ? codes[1] : DEFAULT_EXECUTE_QUERY;
    }
}
