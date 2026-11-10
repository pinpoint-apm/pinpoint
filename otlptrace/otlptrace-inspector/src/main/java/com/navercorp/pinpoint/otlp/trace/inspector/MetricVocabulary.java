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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * The dialects a mapper understands, merged. Resolution is per metric name, not per Resource:
 * Micrometer 1.17 lets an application switch single binders to the OTel conventions, so one
 * Resource can carry {@code jvm.memory.used{jvm.memory.type}} next to {@code jvm.gc.pause{gc}}.
 *
 * <p>A name known to several dialects (the three shared names {@code jvm.memory.used},
 * {@code jvm.buffer.count}, {@code jvm.buffer.memory.used}) is attributed to the dialect whose
 * attribute keys the point carries; when none match, to the first dialect in configuration order.
 * Attribute values are then read through every dialect's keys ("aliases"), so the choice only
 * affects the {@code dialect} tag and the default duration unit. Two dialects giving one name
 * different roles is a configuration error and rejected at construction.
 */
public final class MetricVocabulary {

    /** A resolved metric name. */
    public record Match(MetricDialect dialect, MetricRole role) {
    }

    /** Heap/non-heap as the Inspector sees it, whatever the dialect called it. */
    public enum MemoryType {
        HEAP,
        NON_HEAP
    }

    /** Direct/mapped as the Inspector sees it; {@code OTHER} for pools without a chart. */
    public enum BufferPool {
        DIRECT,
        MAPPED,
        OTHER
    }

    private final List<MetricDialect> dialects;
    private final Map<String, List<Match>> matchesByName;

    public MetricVocabulary(List<MetricDialect> dialects) {
        Objects.requireNonNull(dialects, "dialects");
        if (dialects.isEmpty()) {
            throw new IllegalArgumentException("at least one dialect is required");
        }
        this.dialects = List.copyOf(dialects);
        this.matchesByName = new LinkedHashMap<>();
        for (MetricDialect dialect : this.dialects) {
            for (String name : dialect.metricNames()) {
                final MetricRole role = Objects.requireNonNull(dialect.roleOf(name), name);
                final List<Match> matches = matchesByName.computeIfAbsent(name, k -> new ArrayList<>(2));
                for (Match existing : matches) {
                    if (existing.role() != role) {
                        throw new IllegalArgumentException("metric '" + name + "' is " + existing.role() + " in "
                                + existing.dialect().name() + " but " + role + " in " + dialect.name());
                    }
                }
                matches.add(new Match(dialect, role));
            }
        }
    }

    public static MetricVocabulary of(MetricDialect... dialects) {
        return new MetricVocabulary(List.of(dialects));
    }

    public List<MetricDialect> getDialects() {
        return dialects;
    }

    public List<String> dialectNames() {
        final List<String> names = new ArrayList<>(dialects.size());
        for (MetricDialect dialect : dialects) {
            names.add(dialect.name());
        }
        return Collections.unmodifiableList(names);
    }

    /** @return the role of the name, or {@code null} when no dialect knows it */
    public @Nullable MetricRole roleOf(String metricName) {
        final List<Match> matches = matchesByName.get(metricName);
        return matches == null ? null : matches.get(0).role();
    }

    /**
     * Resolves a point's name to a dialect and role. {@code attributes} breaks the tie for names
     * shared by several dialects.
     */
    public @Nullable Match resolve(String metricName, Map<String, String> attributes) {
        final List<Match> matches = matchesByName.get(metricName);
        if (matches == null) {
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        for (Match match : matches) {
            if (carriesKeysOf(match.dialect(), match.role(), attributes)) {
                return match;
            }
        }
        return matches.get(0);
    }

    private static boolean carriesKeysOf(MetricDialect dialect, MetricRole role, Map<String, String> attributes) {
        return switch (role) {
            case MEMORY_USED, MEMORY_LIMIT -> attributes.containsKey(dialect.memoryTypeKey()) || attributes.containsKey(dialect.memoryPoolKey());
            case GC_DURATION -> attributes.containsKey(dialect.gcNameKey());
            case BUFFER_COUNT, BUFFER_MEMORY_USED -> attributes.containsKey(dialect.bufferPoolKey());
            default -> false;
        };
    }

    /** Heap or non-heap from whichever dialect's type key/value the point carries; {@code null} when none. */
    public @Nullable MemoryType memoryType(MetricPoint point) {
        for (MetricDialect dialect : dialects) {
            final String value = point.attribute(dialect.memoryTypeKey());
            if (value == null) {
                continue;
            }
            if (value.equalsIgnoreCase(dialect.memoryTypeHeap())) {
                return MemoryType.HEAP;
            }
            if (value.equalsIgnoreCase(dialect.memoryTypeNonHeap())) {
                return MemoryType.NON_HEAP;
            }
        }
        return null;
    }

    public @Nullable String memoryPoolName(MetricPoint point) {
        for (MetricDialect dialect : dialects) {
            final String value = point.attribute(dialect.memoryPoolKey());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public @Nullable String gcName(MetricPoint point) {
        for (MetricDialect dialect : dialects) {
            final String value = point.attribute(dialect.gcNameKey());
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** Direct/mapped/other from whichever dialect's pool key the point carries; {@code null} when none. */
    public @Nullable BufferPool bufferPool(MetricPoint point) {
        for (MetricDialect dialect : dialects) {
            final String value = point.attribute(dialect.bufferPoolKey());
            if (value == null) {
                continue;
            }
            final String lower = value.toLowerCase(Locale.ROOT);
            if (lower.equals(dialect.bufferPoolDirect().toLowerCase(Locale.ROOT))) {
                return BufferPool.DIRECT;
            }
            if (lower.equals(dialect.bufferPoolMapped().toLowerCase(Locale.ROOT))) {
                return BufferPool.MAPPED;
            }
            return BufferPool.OTHER;
        }
        return null;
    }

    @Override
    public String toString() {
        return "MetricVocabulary" + dialectNames();
    }
}
