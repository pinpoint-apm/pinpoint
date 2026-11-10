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
 * The CPython metrics of {@code opentelemetry-instrumentation-system-metrics} (installed by
 * {@code opentelemetry-bootstrap} when psutil is present, then enabled by {@code opentelemetry-instrument}),
 * mapped onto the Inspector charts whose meaning is closest. Its process/system CPU times and thread
 * count use semantic-convention names and belong to the {@code otel} dialect.
 *
 * <ul>
 *     <li>Heap used: {@code process.runtime.cpython.memory{type=rss}}, the process's resident set size.
 *     CPython has no heap size API, so this is the whole process memory, not a heap; {@code type=vms}
 *     is {@code ignored_series}. The CPython-specific name is used because the semantic-convention
 *     {@code process.memory.usage} is also sent by other runtimes' host instrumentations, where it would
 *     replace the runtime's own heap.</li>
 *     <li>GC old count: {@code cpython.gc.collections} of {@code cpython.gc.generation=2} (full
 *     collections); generations 0 and 1 are {@code ignored_series}. CPython reports no GC time: the old GC
 *     time stays 0.</li>
 * </ul>
 */
public final class PythonDialect implements MetricDialect {

    public static final String NAME = "python";

    public static final String CPYTHON_MEMORY = "process.runtime.cpython.memory";
    public static final String CPYTHON_GC_COLLECTIONS = "cpython.gc.collections";

    public static final String ATTR_MEMORY_TYPE = "type";
    public static final String MEMORY_TYPE_RSS = "rss";
    public static final String ATTR_GC_GENERATION = "cpython.gc.generation";
    /** The generation a full collection is counted in. */
    public static final String FULL_GENERATION = "2";

    private static final String FULL_COLLECTION = "CPython full collection (generation 2)";
    private static final String GENERATION_PREFIX = "CPython generation ";
    // no non-heap, buffer pools or connection pools as attributes
    private static final String NO_ATTRIBUTE = "";

    private static final Map<String, MetricRole> ROLES = Map.ofEntries(
            Map.entry(CPYTHON_MEMORY, MetricRole.MEMORY_USED),
            Map.entry(CPYTHON_GC_COLLECTIONS, MetricRole.GC_CYCLES)
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
        return MEMORY_TYPE_RSS;
    }

    @Override
    public String memoryTypeNonHeap() {
        return NO_ATTRIBUTE;
    }

    @Override
    public MetricVocabulary.@Nullable MemoryType unlabeledMemoryType() {
        return null;
    }

    @Override
    public String memoryPoolKey() {
        return NO_ATTRIBUTE;
    }

    @Override
    public String gcNameKey() {
        return ATTR_GC_GENERATION;
    }

    /** Generation 2 is the full collection (classified old by name), 0 and 1 the young ones. */
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
