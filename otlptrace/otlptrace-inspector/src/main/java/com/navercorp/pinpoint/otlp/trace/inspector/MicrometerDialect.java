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
 * Micrometer's own naming ({@code micrometer-core} JVM/system binders as exported by
 * {@code micrometer-registry-otlp}, i.e. Spring Boot Actuator's OTLP metrics). Names and tags are
 * the binders' defaults ({@code MicrometerJvm*MeterConventions}); durations are in the registry's
 * base time unit, milliseconds, with the unit written out as {@code milliseconds}.
 *
 * <p>Deliberately left out: {@code jvm.threads.states} (a split of {@code jvm.threads.live}, would
 * double count), {@code jvm.gc.memory.*}, {@code jvm.memory.committed}, {@code process.uptime},
 * {@code tomcat.*} (no Inspector chart), and {@code jdbc.connections.*}: Spring Boot
 * binds it when the context starts, before HikariCP opens its pool, so for HikariCP it carries only
 * {@code max}/{@code min} and never the active count. Connection pools come from HikariCP's own
 * {@code hikaricp.connections.*} (tag {@code pool}), registered once the pool has started.
 */
public final class MicrometerDialect implements MetricDialect {

    public static final String NAME = "micrometer";

    public static final String JVM_MEMORY_USED = "jvm.memory.used";
    public static final String JVM_MEMORY_MAX = "jvm.memory.max";
    public static final String JVM_GC_PAUSE = "jvm.gc.pause";
    public static final String JVM_GC_CONCURRENT_PHASE_TIME = "jvm.gc.concurrent.phase.time";
    public static final String PROCESS_CPU_USAGE = "process.cpu.usage";
    public static final String SYSTEM_CPU_USAGE = "system.cpu.usage";
    public static final String JVM_THREADS_LIVE = "jvm.threads.live";
    public static final String JVM_CLASSES_LOADED = "jvm.classes.loaded";
    public static final String JVM_CLASSES_UNLOADED = "jvm.classes.unloaded";
    public static final String JVM_BUFFER_COUNT = "jvm.buffer.count";
    public static final String JVM_BUFFER_MEMORY_USED = "jvm.buffer.memory.used";
    public static final String PROCESS_FILES_OPEN = "process.files.open";
    public static final String HTTP_SERVER_REQUESTS = "http.server.requests";
    public static final String PROCESS_START_TIME = "process.start.time";
    public static final String HIKARICP_CONNECTIONS_ACTIVE = "hikaricp.connections.active";
    public static final String HIKARICP_CONNECTIONS_MAX = "hikaricp.connections.max";

    public static final String TAG_AREA = "area";
    public static final String AREA_HEAP = "heap";
    public static final String AREA_NON_HEAP = "nonheap";
    public static final String TAG_ID = "id";
    public static final String TAG_GC = "gc";
    public static final String TAG_POOL = "pool";

    private static final List<String> CONNECTION_POOL_NAME_KEYS = List.of(TAG_POOL);

    private static final Map<String, MetricRole> ROLES = Map.ofEntries(
            Map.entry(JVM_MEMORY_USED, MetricRole.MEMORY_USED),
            Map.entry(JVM_MEMORY_MAX, MetricRole.MEMORY_LIMIT),
            Map.entry(JVM_GC_PAUSE, MetricRole.GC_DURATION),
            Map.entry(JVM_GC_CONCURRENT_PHASE_TIME, MetricRole.GC_DURATION),
            Map.entry(PROCESS_CPU_USAGE, MetricRole.CPU_JVM),
            Map.entry(SYSTEM_CPU_USAGE, MetricRole.CPU_SYSTEM),
            Map.entry(JVM_THREADS_LIVE, MetricRole.THREAD_COUNT),
            Map.entry(JVM_CLASSES_LOADED, MetricRole.CLASS_COUNT),
            Map.entry(JVM_CLASSES_UNLOADED, MetricRole.CLASS_UNLOADED),
            Map.entry(JVM_BUFFER_COUNT, MetricRole.BUFFER_COUNT),
            Map.entry(JVM_BUFFER_MEMORY_USED, MetricRole.BUFFER_MEMORY_USED),
            Map.entry(PROCESS_FILES_OPEN, MetricRole.FILE_DESCRIPTOR),
            Map.entry(HTTP_SERVER_REQUESTS, MetricRole.HTTP_SERVER_DURATION),
            Map.entry(PROCESS_START_TIME, MetricRole.START_TIME),
            Map.entry(HIKARICP_CONNECTIONS_ACTIVE, MetricRole.CONNECTION_POOL_USED),
            Map.entry(HIKARICP_CONNECTIONS_MAX, MetricRole.CONNECTION_POOL_MAX)
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

    /** The OTLP registry's base time unit. */
    @Override
    public double defaultDurationMillisPerUnit() {
        return DurationUnits.MILLIS_PER_MILLI;
    }

    @Override
    public String memoryTypeKey() {
        return TAG_AREA;
    }

    @Override
    public String memoryTypeHeap() {
        return AREA_HEAP;
    }

    @Override
    public String memoryTypeNonHeap() {
        return AREA_NON_HEAP;
    }

    @Override
    public MetricVocabulary.@Nullable MemoryType unlabeledMemoryType() {
        return null;
    }

    @Override
    public String memoryPoolKey() {
        return TAG_ID;
    }

    @Override
    public String gcNameKey() {
        return TAG_GC;
    }

    @Override
    public @Nullable String unlabeledGcName() {
        return null;
    }

    @Override
    public String bufferPoolKey() {
        return TAG_ID;
    }

    @Override
    public String bufferPoolDirect() {
        return "direct";
    }

    @Override
    public String bufferPoolMapped() {
        return "mapped";
    }

    @Override
    public List<String> connectionPoolNameKeys() {
        return CONNECTION_POOL_NAME_KEYS;
    }

    /** {@code hikaricp.connections.active} counts only connections in use. */
    @Override
    public List<String> connectionStateKeys() {
        return List.of();
    }

    @Override
    public String connectionStateUsed() {
        return "";
    }
}
