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
 * The Go runtime metrics of the OTel Go contrib {@code instrumentation/runtime} package (semantic
 * conventions {@code go.*}, Development stability), mapped onto the Inspector charts whose meaning
 * is closest. The HTTP server duration is the {@code otel} dialect's.
 *
 * <ul>
 *     <li>Heap / Non-Heap: {@code go.memory.used} with {@code go.memory.type=other} (memory the Go
 *     runtime uses outside goroutine stacks: heap objects, free and unused heap spans, runtime
 *     metadata) as heap and {@code stack} as non-heap; {@code go.memory.limit} (only sent when
 *     {@code GOMEMLIMIT} is set, no attributes) as heap max.</li>
 *     <li>Total Thread: {@code go.goroutine.count}, the live goroutines (not OS threads).</li>
 *     <li>GC old count / time: the opt-in {@code go.memory.gc.cycles} (count) and
 *     {@code go.memory.gc.pause.duration} (stop-the-world time; its count is the pauses, two per cycle,
 *     and is not used), listed in {@code OTEL_GO_X_RUNTIME_METRICS_OPTIN} (contrib after 0.72); or, up to
 *     contrib 0.72, the deprecated {@code process.runtime.go.gc.pause_ns} (one point per cycle) sent with
 *     {@code OTEL_GO_X_DEPRECATED_RUNTIME_METRICS=true}, which later contrib releases remove. When both
 *     come, the cycles win. Go's collector is not generational: every cycle counts, as the single
 *     collector {@link #GC_NAME}.</li>
 *     <li>JVM CPU (process): the opt-in {@code go.cpu.time{go.cpu.state}}, the busy share of GOMAXPROCS
 *     (1 - idle share, the runtime's own estimate), when the contrib {@code instrumentation/host}
 *     running totals {@code process.cpu.time} / {@code system.cpu.time} are absent; those are semantic
 *     convention names shared with other SDKs and belong to the {@code otel} dialect.</li>
 * </ul>
 * Not mapped: the other deprecated {@code process.runtime.go.*} metrics (the current names above are
 * sent next to them).
 */
public final class GoDialect implements MetricDialect {

    public static final String NAME = "go";

    public static final String GO_MEMORY_USED = "go.memory.used";
    public static final String GO_MEMORY_LIMIT = "go.memory.limit";
    public static final String GO_GOROUTINE_COUNT = "go.goroutine.count";
    public static final String GO_GC_PAUSE_NS = "process.runtime.go.gc.pause_ns";
    public static final String GO_MEMORY_GC_CYCLES = "go.memory.gc.cycles";
    public static final String GO_MEMORY_GC_PAUSE_DURATION = "go.memory.gc.pause.duration";
    public static final String GO_CPU_TIME = "go.cpu.time";

    public static final String ATTR_GO_CPU_STATE = "go.cpu.state";
    public static final String CPU_IDLE = "idle";

    public static final String ATTR_MEMORY_TYPE = "go.memory.type";
    public static final String MEMORY_TYPE_OTHER = "other";
    public static final String MEMORY_TYPE_STACK = "stack";

    /** The collector name a Go GC pause is counted under; classified as the old collector. */
    public static final String GC_NAME = GcNameClassifier.GO_GC;

    // Go has no memory pools, GC name, buffer pools or connection pools as attributes
    private static final String NO_ATTRIBUTE = "";

    private static final Map<String, MetricRole> ROLES = Map.ofEntries(
            Map.entry(GO_MEMORY_USED, MetricRole.MEMORY_USED),
            Map.entry(GO_MEMORY_LIMIT, MetricRole.MEMORY_LIMIT),
            Map.entry(GO_GOROUTINE_COUNT, MetricRole.THREAD_COUNT),
            Map.entry(GO_GC_PAUSE_NS, MetricRole.GC_DURATION),
            Map.entry(GO_MEMORY_GC_CYCLES, MetricRole.GC_CYCLES),
            Map.entry(GO_MEMORY_GC_PAUSE_DURATION, MetricRole.GC_PAUSE_TIME),
            Map.entry(GO_CPU_TIME, MetricRole.RUNTIME_CPU_TIME)
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

    /** Semantic conventions record durations in seconds; the deprecated pause histogram says {@code ns}. */
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
        return MEMORY_TYPE_OTHER;
    }

    @Override
    public String memoryTypeNonHeap() {
        return MEMORY_TYPE_STACK;
    }

    /** {@code go.memory.limit} carries no attributes: it limits the whole runtime, the heap here. */
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
        return NO_ATTRIBUTE;
    }

    @Override
    public String unlabeledGcName() {
        return GC_NAME;
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

    /** {@code go.cpu.state=idle}. */
    @Override
    public @Nullable Boolean isIdleCpuTime(MetricPoint point) {
        final String state = point.attribute(ATTR_GO_CPU_STATE);
        if (state == null) {
            return null;
        }
        return CPU_IDLE.equals(state);
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
