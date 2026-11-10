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
 * The V8 runtime metrics of the OTel JS contrib {@code instrumentation-runtime-node} package (semantic
 * conventions {@code v8js.*}, Development stability; on by default in
 * {@code auto-instrumentations-node}), mapped onto the Inspector charts whose meaning is closest. The
 * HTTP server duration and the host CPU times ({@code instrumentation-host-metrics}) are the
 * {@code otel} dialect's.
 *
 * <ul>
 *     <li>Heap used: {@code v8js.memory.heap.used}, summed over the V8 heap spaces
 *     ({@code v8js.heap.space.name}). V8 reports no heap limit, so the heap max is -1, and there is no
 *     non-heap.</li>
 *     <li>GC old count / time: {@code v8js.gc.duration} of {@code v8js.gc.type=major} (Mark-Sweep-Compact);
 *     {@code minor} (Scavenge), {@code incremental} (marking steps) and {@code weakcb} are
 *     {@code ignored_series}, as the agent counts only the old collector.</li>
 * </ul>
 * Not mapped (no Inspector chart): {@code nodejs.eventloop.*}, {@code v8js.resource.active} and the
 * other {@code v8js.memory.heap.space.*} sizes.
 */
public final class NodeDialect implements MetricDialect {

    public static final String NAME = "nodejs";

    public static final String V8JS_MEMORY_HEAP_USED = "v8js.memory.heap.used";
    public static final String V8JS_GC_DURATION = "v8js.gc.duration";

    public static final String ATTR_HEAP_SPACE_NAME = "v8js.heap.space.name";
    public static final String ATTR_GC_TYPE = "v8js.gc.type";

    // V8 has no heap/non-heap split, buffer pools or connection pools as attributes
    private static final String NO_ATTRIBUTE = "";

    private static final Map<String, MetricRole> ROLES = Map.ofEntries(
            Map.entry(V8JS_MEMORY_HEAP_USED, MetricRole.MEMORY_USED),
            Map.entry(V8JS_GC_DURATION, MetricRole.GC_DURATION)
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

    /** Every V8 heap space is heap. */
    @Override
    public MetricVocabulary.MemoryType unlabeledMemoryType() {
        return MetricVocabulary.MemoryType.HEAP;
    }

    @Override
    public String memoryPoolKey() {
        return ATTR_HEAP_SPACE_NAME;
    }

    /** {@code major} is classified as the old collector by name; the other types as young. */
    @Override
    public String gcNameKey() {
        return ATTR_GC_TYPE;
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
