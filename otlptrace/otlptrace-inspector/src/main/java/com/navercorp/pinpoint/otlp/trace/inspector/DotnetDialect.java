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
 * The .NET runtime metrics, mapped onto the Inspector charts whose meaning is closest, under both
 * names: the runtime's own {@code System.Runtime} meter of .NET 9+ ({@code dotnet.*}, semantic
 * conventions, stable; {@code AddRuntimeInstrumentation()} subscribes to it there) and the
 * {@code process.runtime.dotnet.*} that {@code OpenTelemetry.Instrumentation.Runtime} computes itself on
 * .NET 8 and earlier. The process thread count and CPU time of {@code OpenTelemetry.Instrumentation.Process}
 * are the {@code otel} dialect's.
 *
 * <ul>
 *     <li>Heap used: {@code process.runtime.dotnet.gc.objects.size} (bytes of live objects now), or on
 *     .NET 9+ {@code dotnet.gc.last_collection.heap.size} summed over the generations (the heap after the
 *     latest collection, fragmentation included). No limit (-1) and no non-heap.</li>
 *     <li>GC old count: the gen2 (full) collections of {@code dotnet.gc.collections} /
 *     {@code process.runtime.dotnet.gc.collections.count}, both counted exclusively per generation;
 *     gen0 and gen1 are {@code ignored_series}. The time is the total pause time of every generation
 *     ({@code dotnet.gc.pause.time}, {@code process.runtime.dotnet.gc.duration}): .NET does not split it.</li>
 *     <li>JVM CPU: {@code dotnet.process.cpu.time} over the elapsed time times
 *     {@code dotnet.process.cpu.count} (.NET 9+); no system CPU.</li>
 * </ul>
 */
public final class DotnetDialect implements MetricDialect {

    public static final String NAME = "dotnet";

    // .NET 9+ System.Runtime meter
    public static final String DOTNET_GC_COLLECTIONS = "dotnet.gc.collections";
    public static final String DOTNET_GC_PAUSE_TIME = "dotnet.gc.pause.time";
    public static final String DOTNET_GC_LAST_COLLECTION_HEAP_SIZE = "dotnet.gc.last_collection.heap.size";
    public static final String DOTNET_PROCESS_CPU_TIME = "dotnet.process.cpu.time";
    public static final String DOTNET_PROCESS_CPU_COUNT = "dotnet.process.cpu.count";
    // .NET 8 and earlier: OpenTelemetry.Instrumentation.Runtime
    public static final String RUNTIME_GC_COLLECTIONS_COUNT = "process.runtime.dotnet.gc.collections.count";
    public static final String RUNTIME_GC_DURATION = "process.runtime.dotnet.gc.duration";
    public static final String RUNTIME_GC_OBJECTS_SIZE = "process.runtime.dotnet.gc.objects.size";

    /** The .NET 9 runtime's key (the semantic conventions say {@code dotnet.gc.heap.generation}). */
    public static final String ATTR_GC_HEAP_GENERATION = "gc.heap.generation";
    public static final String ATTR_DOTNET_GC_HEAP_GENERATION = "dotnet.gc.heap.generation";
    /** OpenTelemetry.Instrumentation.Runtime's key. */
    public static final String ATTR_GENERATION = "generation";
    public static final String FULL_GENERATION = "gen2";

    private static final List<String> GC_NAME_KEYS = List.of(ATTR_GC_HEAP_GENERATION, ATTR_DOTNET_GC_HEAP_GENERATION, ATTR_GENERATION);
    private static final String FULL_COLLECTION = ".NET gen2 (full) collection";
    private static final String GENERATION_PREFIX = ".NET collection ";
    // no heap/non-heap split, memory pools, buffer pools or connection pools as attributes
    private static final String NO_ATTRIBUTE = "";

    private static final Map<String, MetricRole> ROLES = Map.ofEntries(
            Map.entry(DOTNET_GC_COLLECTIONS, MetricRole.GC_CYCLES),
            Map.entry(DOTNET_GC_PAUSE_TIME, MetricRole.GC_PAUSE_TIME),
            Map.entry(DOTNET_GC_LAST_COLLECTION_HEAP_SIZE, MetricRole.MEMORY_USED),
            Map.entry(DOTNET_PROCESS_CPU_TIME, MetricRole.PROCESS_CPU_TIME),
            Map.entry(DOTNET_PROCESS_CPU_COUNT, MetricRole.CPU_COUNT),
            Map.entry(RUNTIME_GC_COLLECTIONS_COUNT, MetricRole.GC_CYCLES),
            Map.entry(RUNTIME_GC_DURATION, MetricRole.GC_PAUSE_TIME),
            Map.entry(RUNTIME_GC_OBJECTS_SIZE, MetricRole.MEMORY_USED)
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

    /** {@code dotnet.*} durations are in seconds; the instrumentation's say {@code ns}. */
    @Override
    public double defaultDurationMillisPerUnit() {
        return DurationUnits.MILLIS_PER_SECOND;
    }

    @Override
    public String memoryTypeKey() {
        return NO_ATTRIBUTE;
    }

    @Override
    public String memoryTypeHeap() {
        return NO_ATTRIBUTE;
    }

    @Override
    public String memoryTypeNonHeap() {
        return NO_ATTRIBUTE;
    }

    /** Every managed heap generation (and the live objects) is heap. */
    @Override
    public MetricVocabulary.MemoryType unlabeledMemoryType() {
        return MetricVocabulary.MemoryType.HEAP;
    }

    @Override
    public String memoryPoolKey() {
        return NO_ATTRIBUTE;
    }

    @Override
    public String gcNameKey() {
        return ATTR_GC_HEAP_GENERATION;
    }

    @Override
    public List<String> gcNameKeys() {
        return GC_NAME_KEYS;
    }

    /** gen2 is the full collection (classified old by name), gen0 and gen1 the young ones. */
    @Override
    public String gcCollectorName(String gcNameValue) {
        if (FULL_GENERATION.equals(gcNameValue)) {
            return FULL_COLLECTION;
        }
        return GENERATION_PREFIX + gcNameValue;
    }

    @Override
    public @Nullable String unlabeledGcName() {
        return null;
    }

    @Override
    public String bufferPoolKey() {
        return NO_ATTRIBUTE;
    }

    @Override
    public String bufferPoolDirect() {
        return NO_ATTRIBUTE;
    }

    @Override
    public String bufferPoolMapped() {
        return NO_ATTRIBUTE;
    }

    @Override
    public List<String> connectionPoolNameKeys() {
        return List.of();
    }

    @Override
    public List<String> connectionStateKeys() {
        return List.of();
    }

    @Override
    public String connectionStateUsed() {
        return NO_ATTRIBUTE;
    }
}
