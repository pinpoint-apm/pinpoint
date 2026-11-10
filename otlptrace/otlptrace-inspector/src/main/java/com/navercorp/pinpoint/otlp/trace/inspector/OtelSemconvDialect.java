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

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OpenTelemetry semantic conventions for JVM runtime and HTTP server metrics, as emitted by the OTel
 * Java agent's {@code runtime-telemetry} instrumentation (names and attributes stable since semconv
 * 1.23). {@code jvm.system.cpu.utilization} and {@code jvm.file_descriptor.count} are experimental
 * and only emitted when the agent is started with
 * {@code otel.instrumentation.runtime-telemetry.emit-experimental-telemetry=true}.
 *
 * <p>Connection pools (the agent's HikariCP, DBCP2, Tomcat JDBC, c3p0, Vibur, Oracle UCP and Druid
 * instrumentations, on by default) under both names the agent uses: the older
 * {@code db.client.connections.*} with {@code pool.name}/{@code state} (the default in 2.x) and the
 * stable {@code db.client.connection.*} with {@code db.client.connection.pool.name}/{@code .state}
 * ({@code otel.semconv-stability.opt-in=database}).
 *
 * <p>Host CPU times, the running totals {@code process.cpu.time{cpu.mode}} and
 * {@code system.cpu.time{cpu.mode}} of the OTel Go contrib {@code instrumentation/host} and the JS
 * {@code instrumentation-host-metrics}: process CPU = the process's increase over the machine's (every
 * CPU, idle included), system CPU = 1 - idle share. They are used when no CPU utilization gauge is
 * sent (the Java agent sends {@code jvm.cpu.recent_utilization}). The mode is read from {@code cpu.mode}
 * or the older keys {@code system.cpu.state} / {@code state} (Python's
 * {@code opentelemetry-instrumentation-system-metrics} still sends {@code system.cpu.time{cpu, state}}).
 * {@code process.thread.count} (the process's OS threads; the same instrumentation) is Total Thread.
 */
public final class OtelSemconvDialect implements MetricDialect {

    public static final String NAME = "otel";

    public static final String JVM_MEMORY_USED = "jvm.memory.used";
    public static final String JVM_MEMORY_LIMIT = "jvm.memory.limit";
    public static final String JVM_GC_DURATION = "jvm.gc.duration";
    public static final String JVM_CPU_RECENT_UTILIZATION = "jvm.cpu.recent_utilization";
    public static final String JVM_SYSTEM_CPU_UTILIZATION = "jvm.system.cpu.utilization";
    public static final String JVM_THREAD_COUNT = "jvm.thread.count";
    public static final String JVM_CLASS_COUNT = "jvm.class.count";
    public static final String JVM_CLASS_UNLOADED = "jvm.class.unloaded";
    public static final String JVM_BUFFER_COUNT = "jvm.buffer.count";
    public static final String JVM_BUFFER_MEMORY_USED = "jvm.buffer.memory.used";
    public static final String JVM_FILE_DESCRIPTOR_COUNT = "jvm.file_descriptor.count";
    public static final String HTTP_SERVER_REQUEST_DURATION = "http.server.request.duration";
    public static final String PROCESS_CPU_TIME = "process.cpu.time";
    public static final String SYSTEM_CPU_TIME = "system.cpu.time";
    public static final String PROCESS_THREAD_COUNT = "process.thread.count";
    public static final String DB_CLIENT_CONNECTIONS_USAGE = "db.client.connections.usage";
    public static final String DB_CLIENT_CONNECTIONS_MAX = "db.client.connections.max";
    public static final String DB_CLIENT_CONNECTION_COUNT = "db.client.connection.count";
    public static final String DB_CLIENT_CONNECTION_MAX = "db.client.connection.max";

    public static final String ATTR_MEMORY_TYPE = "jvm.memory.type";
    public static final String MEMORY_TYPE_HEAP = "heap";
    public static final String MEMORY_TYPE_NON_HEAP = "non_heap";
    public static final String ATTR_MEMORY_POOL_NAME = "jvm.memory.pool.name";
    public static final String ATTR_GC_NAME = "jvm.gc.name";
    public static final String ATTR_BUFFER_POOL_NAME = "jvm.buffer.pool.name";
    public static final String BUFFER_POOL_DIRECT = "direct";
    public static final String BUFFER_POOL_MAPPED = "mapped";
    public static final String ATTR_CPU_MODE = "cpu.mode";
    /** Older names of {@link #ATTR_CPU_MODE}, in lookup order after it: this and {@link #ATTR_STATE}. */
    public static final String ATTR_SYSTEM_CPU_STATE = "system.cpu.state";
    public static final String CPU_MODE_IDLE = "idle";
    public static final String ATTR_POOL_NAME = "pool.name";
    public static final String ATTR_CONNECTION_POOL_NAME = "db.client.connection.pool.name";
    public static final String ATTR_STATE = "state";
    public static final String ATTR_CONNECTION_STATE = "db.client.connection.state";
    public static final String CONNECTION_STATE_USED = "used";

    private static final List<String> CONNECTION_POOL_NAME_KEYS = List.of(ATTR_CONNECTION_POOL_NAME, ATTR_POOL_NAME);
    private static final List<String> CONNECTION_STATE_KEYS = List.of(ATTR_CONNECTION_STATE, ATTR_STATE);

    private static final Map<String, MetricRole> ROLES = Map.ofEntries(
            Map.entry(JVM_MEMORY_USED, MetricRole.MEMORY_USED),
            Map.entry(JVM_MEMORY_LIMIT, MetricRole.MEMORY_LIMIT),
            Map.entry(JVM_GC_DURATION, MetricRole.GC_DURATION),
            Map.entry(JVM_CPU_RECENT_UTILIZATION, MetricRole.CPU_JVM),
            Map.entry(JVM_SYSTEM_CPU_UTILIZATION, MetricRole.CPU_SYSTEM),
            Map.entry(JVM_THREAD_COUNT, MetricRole.THREAD_COUNT),
            Map.entry(JVM_CLASS_COUNT, MetricRole.CLASS_COUNT),
            Map.entry(JVM_CLASS_UNLOADED, MetricRole.CLASS_UNLOADED),
            Map.entry(JVM_BUFFER_COUNT, MetricRole.BUFFER_COUNT),
            Map.entry(JVM_BUFFER_MEMORY_USED, MetricRole.BUFFER_MEMORY_USED),
            Map.entry(JVM_FILE_DESCRIPTOR_COUNT, MetricRole.FILE_DESCRIPTOR),
            Map.entry(HTTP_SERVER_REQUEST_DURATION, MetricRole.HTTP_SERVER_DURATION),
            Map.entry(PROCESS_CPU_TIME, MetricRole.PROCESS_CPU_TIME),
            Map.entry(SYSTEM_CPU_TIME, MetricRole.SYSTEM_CPU_TIME),
            Map.entry(PROCESS_THREAD_COUNT, MetricRole.THREAD_COUNT),
            Map.entry(DB_CLIENT_CONNECTIONS_USAGE, MetricRole.CONNECTION_POOL_USED),
            Map.entry(DB_CLIENT_CONNECTIONS_MAX, MetricRole.CONNECTION_POOL_MAX),
            Map.entry(DB_CLIENT_CONNECTION_COUNT, MetricRole.CONNECTION_POOL_USED),
            Map.entry(DB_CLIENT_CONNECTION_MAX, MetricRole.CONNECTION_POOL_MAX)
    );

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public @Nullable MetricRole roleOf(String metricName) {
        return ROLES.get(metricName);
    }

    @Override
    public Set<String> metricNames() {
        return ROLES.keySet();
    }

    /** Semantic conventions record durations in seconds. */
    @Override
    public double defaultDurationMillisPerUnit() {
        return DurationUnits.MILLIS_PER_SECOND;
    }

    @Override
    public String memoryTypeKey() {
        return ATTR_MEMORY_TYPE;
    }

    @Override
    public String memoryTypeHeap() {
        return MEMORY_TYPE_HEAP;
    }

    @Override
    public String memoryTypeNonHeap() {
        return MEMORY_TYPE_NON_HEAP;
    }

    @Override
    public MetricVocabulary.@Nullable MemoryType unlabeledMemoryType() {
        return null;
    }

    @Override
    public String memoryPoolKey() {
        return ATTR_MEMORY_POOL_NAME;
    }

    @Override
    public String gcNameKey() {
        return ATTR_GC_NAME;
    }

    @Override
    public @Nullable String unlabeledGcName() {
        return null;
    }

    @Override
    public String bufferPoolKey() {
        return ATTR_BUFFER_POOL_NAME;
    }

    @Override
    public String bufferPoolDirect() {
        return BUFFER_POOL_DIRECT;
    }

    @Override
    public String bufferPoolMapped() {
        return BUFFER_POOL_MAPPED;
    }

    /** {@code idle} in {@code cpu.mode}, or in the older {@code system.cpu.state} / {@code state}. */
    @Override
    public @Nullable Boolean isIdleCpuTime(MetricPoint point) {
        String mode = point.attribute(ATTR_CPU_MODE);
        if (mode == null) {
            mode = point.attribute(ATTR_SYSTEM_CPU_STATE);
        }
        if (mode == null) {
            mode = point.attribute(ATTR_STATE);
        }
        if (mode == null) {
            return null;
        }
        return CPU_MODE_IDLE.equals(mode);
    }

    @Override
    public List<String> connectionPoolNameKeys() {
        return CONNECTION_POOL_NAME_KEYS;
    }

    @Override
    public List<String> connectionStateKeys() {
        return CONNECTION_STATE_KEYS;
    }

    @Override
    public String connectionStateUsed() {
        return CONNECTION_STATE_USED;
    }
}
