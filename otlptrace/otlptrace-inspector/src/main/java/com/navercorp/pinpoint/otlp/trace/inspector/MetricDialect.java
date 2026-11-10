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
import java.util.Set;

/**
 * A metric naming convention: which names carry which {@link MetricRole}, and which attribute keys
 * and values split them. {@link OtelSemconvDialect} covers the OTel semantic conventions (the OTel
 * Java agent); a Micrometer dialect ({@code jvm.gc.pause}, {@code process.cpu.usage}, attribute
 * {@code area}/{@code id}) can be added without touching the mapper.
 */
public interface MetricDialect {

    String name();

    @Nullable MetricRole roleOf(String metricName);

    /** Every name {@link #roleOf} knows, for merging dialects into a {@link MetricVocabulary}. */
    Set<String> metricNames();

    /**
     * Millis per unit assumed for a duration metric that carries no {@code unit}: OTel conventions
     * record durations in seconds, Micrometer in its base time unit (milliseconds).
     */
    double defaultDurationMillisPerUnit();

    /** Attribute key whose value is {@link #memoryTypeHeap()} or {@link #memoryTypeNonHeap()}. */
    String memoryTypeKey();

    String memoryTypeHeap();

    String memoryTypeNonHeap();

    /**
     * The memory type of a memory point that carries none of the type keys, or {@code null} when such a
     * point is invalid (the JVM dialects always split memory by type).
     */
    MetricVocabulary.@Nullable MemoryType unlabeledMemoryType();

    /** Attribute key holding the memory pool name ("G1 Eden Space" ...). */
    String memoryPoolKey();

    /** Attribute key holding the garbage collector name ("G1 Young Generation" ...). */
    String gcNameKey();

    /**
     * The collector name of a GC point without a collector name attribute (a runtime with a single
     * collector), or {@code null} when such a point is invalid.
     */
    @Nullable String unlabeledGcName();

    /**
     * Every attribute key that may hold the collector name, in lookup order ({@link #gcNameKey()} by
     * default); read before the other dialects' keys.
     */
    default List<String> gcNameKeys() {
        return List.of(gcNameKey());
    }

    /**
     * The collector name a GC name attribute value stands for, as {@link GcNameClassifier} classifies it
     * (CPython reports generation numbers). The value itself by default.
     */
    default String gcCollectorName(String gcNameValue) {
        return gcNameValue;
    }

    /** Attribute key whose value is {@link #bufferPoolDirect()} or {@link #bufferPoolMapped()}. */
    String bufferPoolKey();

    String bufferPoolDirect();

    String bufferPoolMapped();

    /**
     * Whether a CPU time point ({@link MetricRole#PROCESS_CPU_TIME}, {@link MetricRole#SYSTEM_CPU_TIME},
     * {@link MetricRole#RUNTIME_CPU_TIME}) is idle time, or {@code null} when the point carries no CPU
     * mode/state this dialect knows (such a point is invalid: idle time cannot be told apart).
     * Only dialects with CPU time metrics override it.
     */
    default @Nullable Boolean isIdleCpuTime(MetricPoint point) {
        return null;
    }

    /** Attribute keys that may hold a connection pool's name, in lookup order. */
    List<String> connectionPoolNameKeys();

    /**
     * Attribute keys that may hold the state of a pool's connection count. Empty when the dialect's
     * used-connections metric counts only connections in use.
     */
    List<String> connectionStateKeys();

    /** The connection state value counted as in use. */
    String connectionStateUsed();
}
