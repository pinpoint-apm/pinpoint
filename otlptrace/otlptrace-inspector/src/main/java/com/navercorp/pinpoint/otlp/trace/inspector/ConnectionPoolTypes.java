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

import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * The Pinpoint service type of a connection pool, the {@code serviceTypeCode} tag of the Inspector
 * dataSource chart. The native agent reports its pool plugin's type; here the pool library is told by
 * the OTel instrumentation scope ({@code io.opentelemetry.hikaricp-3.0}, ...) or by a Micrometer
 * metric name that only one library registers ({@code hikaricp.*}). A pool library Pinpoint has no
 * plugin for, or a metric without a scope, is {@link ServiceType#UNKNOWN_DB}.
 */
final class ConnectionPoolTypes {

    // the codes of the agent's pool plugins (agent-module/plugins/hikaricp, dbcp2, druid)
    static final short HIKARICP = 6060;
    static final short DBCP2 = 6052;
    static final short DRUID = 6062;
    static final short UNKNOWN = ServiceType.UNKNOWN_DB.getCode();

    private static final String OTEL_SCOPE_PREFIX = "io.opentelemetry.";
    private static final String MICROMETER_HIKARICP_PREFIX = "hikaricp.";

    private ConnectionPoolTypes() {
    }

    static short serviceTypeCode(String scope, String metricName) {
        if (metricName.startsWith(MICROMETER_HIKARICP_PREFIX)) {
            return HIKARICP;
        }
        if (!scope.startsWith(OTEL_SCOPE_PREFIX)) {
            return UNKNOWN;
        }
        final String library = scope.substring(OTEL_SCOPE_PREFIX.length());
        if (library.startsWith("hikaricp-")) {
            return HIKARICP;
        }
        if (library.startsWith("apache-dbcp-")) {
            return DBCP2;
        }
        if (library.startsWith("alibaba-druid-")) {
            return DRUID;
        }
        return UNKNOWN;
    }
}
