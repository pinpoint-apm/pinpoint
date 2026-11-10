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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Maps a {@code GarbageCollectorMXBean} name (the {@code jvm.gc.name} attribute) to the Pinpoint GC
 * type and whether the agent's {@code jvmGc} chart counts it. The agent
 * ({@code profiler/.../monitor/metric/gc/GarbageCollectorType}) counts exactly one bean per family,
 * the old/major one; the young bean, the G1 concurrent collector (JDK 20+, a separate bean) and the
 * ZGC cycle counters (every cycle also reports its pauses) are not counted, so the two paths show the
 * same numbers. An unknown collector (Shenandoah, a vendor collector) is {@link JvmGcType#UNKNOWN} and
 * is counted when its name says old ("Old", "Major", "Full"), left out otherwise.
 */
public final class GcNameClassifier {

    private static final GcClassification SERIAL_OLD = new GcClassification(JvmGcType.SERIAL, true);
    private static final GcClassification SERIAL_YOUNG = new GcClassification(JvmGcType.SERIAL, false);
    private static final GcClassification PARALLEL_OLD = new GcClassification(JvmGcType.PARALLEL, true);
    private static final GcClassification PARALLEL_YOUNG = new GcClassification(JvmGcType.PARALLEL, false);
    private static final GcClassification CMS_OLD = new GcClassification(JvmGcType.CMS, true);
    private static final GcClassification CMS_YOUNG = new GcClassification(JvmGcType.CMS, false);
    private static final GcClassification G1_OLD = new GcClassification(JvmGcType.G1, true);
    private static final GcClassification G1_YOUNG = new GcClassification(JvmGcType.G1, false);
    private static final GcClassification ZGC_OLD = new GcClassification(JvmGcType.ZGC, true);
    private static final GcClassification ZGC_YOUNG = new GcClassification(JvmGcType.ZGC, false);
    private static final GcClassification UNKNOWN_OLD = new GcClassification(JvmGcType.UNKNOWN, true);
    private static final GcClassification UNKNOWN_YOUNG = new GcClassification(JvmGcType.UNKNOWN, false);

    /** The Go runtime's collector: not generational, every cycle counts (no Pinpoint GC type). */
    public static final String GO_GC = "Go GC";

    private static final Map<String, GcClassification> KNOWN = Map.ofEntries(
            Map.entry("MarkSweepCompact", SERIAL_OLD),
            Map.entry("Copy", SERIAL_YOUNG),
            Map.entry("PS MarkSweep", PARALLEL_OLD),
            Map.entry("PS Scavenge", PARALLEL_YOUNG),
            Map.entry("ConcurrentMarkSweep", CMS_OLD),
            Map.entry("ParNew", CMS_YOUNG),
            Map.entry("G1 Old Generation", G1_OLD),
            Map.entry("G1 Young Generation", G1_YOUNG),
            // JDK 20+: concurrent cycle reported as its own collector; the agent ignores it
            Map.entry("G1 Concurrent GC", G1_YOUNG),
            // JDK 21+ generational ZGC: the agent counts the major pauses, cycles are not pauses
            Map.entry("ZGC Major Pauses", ZGC_OLD),
            Map.entry("ZGC Minor Pauses", ZGC_YOUNG),
            Map.entry("ZGC Major Cycles", ZGC_YOUNG),
            Map.entry("ZGC Minor Cycles", ZGC_YOUNG),
            // JDK 15-20 single-generation ZGC (one pause bean; the agent has no entry for it)
            Map.entry("ZGC Pauses", ZGC_OLD),
            Map.entry("ZGC Cycles", ZGC_YOUNG),
            Map.entry("ZGC", ZGC_OLD),
            Map.entry(GO_GC, UNKNOWN_OLD)
    );

    // The heap pools name their collector too, and every cycle reports them: under DELTA the collector
    // series are left out of an export in which nothing was collected.
    private static final Map<String, JvmGcType> HEAP_POOLS = Map.ofEntries(
            Map.entry("Eden Space", JvmGcType.SERIAL),
            Map.entry("Survivor Space", JvmGcType.SERIAL),
            Map.entry("Tenured Gen", JvmGcType.SERIAL),
            Map.entry("PS Eden Space", JvmGcType.PARALLEL),
            Map.entry("PS Survivor Space", JvmGcType.PARALLEL),
            Map.entry("PS Old Gen", JvmGcType.PARALLEL),
            Map.entry("Par Eden Space", JvmGcType.CMS),
            Map.entry("Par Survivor Space", JvmGcType.CMS),
            Map.entry("CMS Old Gen", JvmGcType.CMS),
            Map.entry("G1 Eden Space", JvmGcType.G1),
            Map.entry("G1 Survivor Space", JvmGcType.G1),
            Map.entry("G1 Old Gen", JvmGcType.G1),
            Map.entry("ZHeap", JvmGcType.ZGC),
            Map.entry("ZGC Young Generation", JvmGcType.ZGC),
            Map.entry("ZGC Old Generation", JvmGcType.ZGC)
    );

    // Generational ZGC: the young and the old generation share one heap, and each pool reports the
    // whole heap as its limit (MemoryPoolMXBean#getUsage().getMax() = -Xmx).
    private static final Set<String> SHARED_HEAP_POOLS = Set.of("ZGC Young Generation", "ZGC Old Generation");

    /** The collector family a heap memory pool belongs to, {@link JvmGcType#UNKNOWN} for others (Shenandoah). */
    public JvmGcType heapPoolGcType(String poolName) {
        return HEAP_POOLS.getOrDefault(poolName, JvmGcType.UNKNOWN);
    }

    /** True for a heap pool whose limit is the whole heap it shares with other pools: limits do not add up. */
    public boolean isSharedHeapPool(String poolName) {
        return SHARED_HEAP_POOLS.contains(poolName);
    }

    public GcClassification classify(String gcName) {
        final GcClassification known = KNOWN.get(gcName);
        if (known != null) {
            return known;
        }
        final String lower = gcName.toLowerCase(Locale.ROOT);
        if (lower.contains("old") || lower.contains("major") || lower.contains("full")) {
            return UNKNOWN_OLD;
        }
        return UNKNOWN_YOUNG;
    }
}
