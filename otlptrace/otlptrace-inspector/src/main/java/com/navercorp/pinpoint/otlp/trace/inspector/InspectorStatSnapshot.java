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
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Everything the points of one collection cycle said about the JVM, folded into the fields of the
 * Inspector BOs. The cycle's points need not share a {@code time_unix_nano}; the snapshot is stamped
 * with the latest one {@link #observe observed}. {@link #build} turns it into stat points, one BO per
 * chart family whose primary input was present. Absent secondary inputs are {@value #UNCOLLECTED},
 * the agent's own marker for a value it could not read.
 *
 * <p>Only the families the Inspector draws are produced. {@code jvmGcDetailed} (young GC, per-pool
 * usage ratios) has no chart in the web, so it is not generated and young-collector pauses count as
 * {@link InspectorDropReason#IGNORED_SERIES}.
 */
final class InspectorStatSnapshot {

    static final long UNCOLLECTED = -1;

    // The SDKs stamp one collection with one time, but an SDK may stamp each instrument at its own
    // callback: points within a second belong to the same interval. An interval left out is a whole
    // export interval (seconds or more) apart.
    private static final long INTERVAL_TOLERANCE_MS = 1000;
    private static final String CPU_SERIES = "cpu.series:";
    private static final String OTEL_SCOPE_PREFIX = "io.opentelemetry.";

    private long timeMs;

    private @Nullable Long heapUsed;
    private @Nullable Long heapMax;
    // limits of pools that each report the whole shared heap (generational ZGC): the largest, not the sum
    private @Nullable Long sharedHeapMax;
    private @Nullable Long nonHeapUsed;
    private @Nullable Long nonHeapMax;
    // MemoryMXBean's non-heap max is -1 unless every non-heap pool has a limit (Metaspace usually has
    // none); the agent reports that -1, so pools are tracked to reproduce it. Heap max keeps the sum of
    // the pools that report a limit: under G1 only the old gen does, and its limit is the heap max.
    private final Set<String> nonHeapPoolsUsed = new HashSet<>();
    private final Set<String> nonHeapPoolsLimited = new HashSet<>();

    private JvmGcType gcType = JvmGcType.UNKNOWN;
    private JvmGcType heapPoolGcType = JvmGcType.UNKNOWN;
    private boolean oldGcSeen;
    private boolean oldGcTotalSet;
    private long gcOldCount;
    private long gcOldTimeMs;

    private @Nullable Double cpuJvm;
    private @Nullable Double cpuSystem;
    // CPU running totals' increases in the cycle (seconds); a series without an increase (no previous
    // point) makes its metric incomplete, and an incomplete metric gives no ratio
    private final CpuTime processCpu = new CpuTime();
    private final CpuTime systemCpu = new CpuTime();
    private final CpuTime runtimeCpu = new CpuTime();
    private @Nullable Long cpuCount;
    private @Nullable Long threadCount;
    private @Nullable Long classCount;
    private @Nullable Long classUnloaded;
    private boolean classUnloadedTotalSet;
    private @Nullable Long directCount;
    private @Nullable Long directMemory;
    private @Nullable Long mappedCount;
    private @Nullable Long mappedMemory;
    private @Nullable Long fileDescriptors;

    private boolean httpSeen;
    private long httpCount;
    private double httpSumMs;
    private double httpMaxMs = UNCOLLECTED;

    // per pool (name, then library), in that order so the stored list is stable
    private final Map<String, ConnectionPool> connectionPools = new TreeMap<>();

    private static final class ConnectionPool {
        private final String library;
        private final String name;
        private final short serviceTypeCode;
        private @Nullable Long used;
        private @Nullable Long max;

        private ConnectionPool(String library, String name, short serviceTypeCode) {
            this.library = library;
            this.name = name;
            this.serviceTypeCode = serviceTypeCode;
        }
    }

    /** Records a point's {@code time_unix_nano} (millis); the snapshot takes the latest. */
    void observe(long timeMs) {
        if (timeMs > this.timeMs) {
            this.timeMs = timeMs;
        }
    }

    long getTimeMs() {
        return timeMs;
    }

    void addHeapUsed(long bytes) {
        heapUsed = add(heapUsed, bytes);
    }

    void addHeapMax(long bytes) {
        heapMax = add(heapMax, bytes);
    }

    /** The limit of a pool that shares one heap with others and reports all of it (generational ZGC). */
    void addSharedHeapMax(long bytes) {
        if (sharedHeapMax == null || bytes > sharedHeapMax) {
            sharedHeapMax = bytes;
        }
    }

    private @Nullable Long heapMaxTotal() {
        if (sharedHeapMax == null) {
            return heapMax;
        }
        return add(heapMax, sharedHeapMax);
    }

    /** @param poolName the pool the point describes, null for an aggregate series */
    void addNonHeapUsed(long bytes, @Nullable String poolName) {
        nonHeapUsed = add(nonHeapUsed, bytes);
        if (poolName != null) {
            nonHeapPoolsUsed.add(poolName);
        }
    }

    /** @param poolName the pool the point describes, null for an aggregate series */
    void addNonHeapMax(long bytes, @Nullable String poolName) {
        nonHeapMax = add(nonHeapMax, bytes);
        if (poolName != null) {
            nonHeapPoolsLimited.add(poolName);
        }
    }

    /** A non-heap pool that reported "no limit" explicitly (Micrometer's {@code jvm.memory.max=-1}). */
    void noteNonHeapPoolWithoutLimit(@Nullable String poolName) {
        if (poolName != null) {
            nonHeapPoolsUsed.add(poolName);
        }
    }

    /** The collector family, taken from the first classified collector (young or old). */
    void noteGcType(JvmGcType type) {
        if (gcType == JvmGcType.UNKNOWN) {
            gcType = type;
        }
    }

    /**
     * The collector family a heap pool names, used when no collector series is in the cycle (under
     * DELTA a collector that collected nothing in the interval is left out of the export).
     */
    void noteHeapPoolGcType(JvmGcType type) {
        if (heapPoolGcType == JvmGcType.UNKNOWN) {
            heapPoolGcType = type;
        }
    }

    /** One old/major collector's CUMULATIVE totals (several series add up). */
    void addOldGc(long count, long timeMs) {
        oldGcSeen = true;
        gcOldCount += count;
        gcOldTimeMs += timeMs;
    }

    /** The agent-level old GC total rebuilt from DELTA increments (replaces, the total already sums the series). */
    void setOldGcTotal(long count, long timeMs) {
        oldGcSeen = true;
        oldGcTotalSet = true;
        gcOldCount = count;
        gcOldTimeMs = timeMs;
    }

    boolean hasOldGc() {
        return oldGcSeen;
    }

    /** True when the old GC fields hold the agent-level DELTA total ({@link #setOldGcTotal}). */
    boolean hasOldGcTotal() {
        return oldGcTotalSet;
    }

    long getGcOldCount() {
        return gcOldCount;
    }

    long getGcOldTimeMs() {
        return gcOldTimeMs;
    }

    /**
     * One series' increase of a CPU running total in the cycle.
     *
     * @param seriesKey  the series ({@link MetricPoint#seriesKey})
     * @param seconds    the increase in seconds, null when the series had no usable previous point
     * @param startMs    the start of the interval the increase covers (0 when unknown)
     * @param endMs      the end of that interval
     * @param cumulative a CUMULATIVE series: one left out of an export is a gap, not "no CPU used" (a
     *                   DELTA series without recordings is left out by the SDK)
     * @param idle       idle time (system and runtime totals)
     */
    record CpuTimeIncrease(String seriesKey, @Nullable Double seconds, long startMs, long endMs,
                           boolean cumulative, boolean idle) {
    }

    void addProcessCpuTime(CpuTimeIncrease increase) {
        processCpu.add(increase);
    }

    /** CPUs the process can use (.NET). */
    void setCpuCount(long count) {
        cpuCount = count;
    }

    void addSystemCpuTime(CpuTimeIncrease increase) {
        systemCpu.add(increase);
    }

    void addRuntimeCpuTime(CpuTimeIncrease increase) {
        runtimeCpu.add(increase);
    }

    /**
     * Marks a CPU total incomplete when a CUMULATIVE series the agent sent in its previous cycle is not
     * in this one: the ratio would leave that series' time out (and the next cycle's increase of it
     * spans two intervals, which {@link CpuTime} refuses by the interval check).
     */
    void checkCpuSeriesAgainstPreviousCycle(SeriesStateCache stateCache, String keyPrefix) {
        processCpu.checkAgainstPreviousCycle(stateCache, keyPrefix + CPU_SERIES + "process", timeMs);
        systemCpu.checkAgainstPreviousCycle(stateCache, keyPrefix + CPU_SERIES + "system", timeMs);
        runtimeCpu.checkAgainstPreviousCycle(stateCache, keyPrefix + CPU_SERIES + "runtime", timeMs);
    }

    /**
     * The process CPU when no utilization gauge gave one: the process's CPU time over the machine's
     * (both from the host metrics, so every CPU, and only over the same interval), else over the
     * elapsed time times the process's CPU count (.NET), else the busy share of the runtime's own
     * capacity.
     */
    private @Nullable Double cpuJvmFromTimes() {
        // a process that used no CPU in the interval is 0 %, not a gap: only the denominator must be > 0
        if (processCpu.complete() && systemCpu.complete() && systemCpu.total > 0) {
            if (!processCpu.sameInterval(systemCpu)) {
                return null;
            }
            return ratio(processCpu.total / systemCpu.total);
        }
        final long elapsedMs = processCpu.elapsedMs();
        if (processCpu.complete() && cpuCount != null && cpuCount > 0 && elapsedMs > 0) {
            return ratio(processCpu.total / (elapsedMs / 1000d * cpuCount));
        }
        // the runtime's totals include idle: no increase at all means they were not refreshed (Go, ~15 s)
        if (runtimeCpu.complete() && runtimeCpu.total > 0) {
            return ratio(1 - runtimeCpu.idle / runtimeCpu.total);
        }
        return null;
    }

    private @Nullable Double cpuSystemFromTimes() {
        if (systemCpu.complete() && systemCpu.total > 0) {
            return ratio(1 - systemCpu.idle / systemCpu.total);
        }
        return null;
    }

    private static double ratio(double value) {
        return Math.max(0, Math.min(1, value));
    }

    /**
     * The increases of one CPU total's series in the cycle. They add up only over one interval: a
     * series whose previous point is older than the others' (it was left out of an export, or dropped
     * as invalid) covers more time, and adding it would mix the intervals, so the total is incomplete.
     */
    private static final class CpuTime {
        private boolean seen;
        private boolean incomplete;
        private double total;
        private double idle;
        private boolean intervalKnown;
        private long startMs;
        private long endMs;
        private final Set<String> cumulativeSeries = new HashSet<>();

        void add(CpuTimeIncrease increase) {
            seen = true;
            if (increase.cumulative()) {
                cumulativeSeries.add(increase.seriesKey());
            }
            final Double seconds = increase.seconds();
            if (seconds == null) {
                incomplete = true;
                return;
            }
            if (!intervalKnown) {
                intervalKnown = true;
                startMs = increase.startMs();
                endMs = increase.endMs();
            } else if (!sameInterval(startMs, endMs, increase.startMs(), increase.endMs())) {
                incomplete = true;
                return;
            }
            total += seconds;
            if (increase.idle()) {
                idle += seconds;
            }
        }

        void checkAgainstPreviousCycle(SeriesStateCache stateCache, String key, long cycleTimeMs) {
            if (cumulativeSeries.isEmpty()) {
                return;
            }
            if (!stateCache.coversPreviousSeries(key, cycleTimeMs, cumulativeSeries)) {
                incomplete = true;
            }
        }

        /** Seen, and every series had an increase (possibly 0) over the same interval. */
        boolean complete() {
            return seen && !incomplete;
        }

        boolean sameInterval(CpuTime other) {
            return intervalKnown && other.intervalKnown && sameInterval(startMs, endMs, other.startMs, other.endMs);
        }

        /** The interval the increases cover, 0 when unknown (a DELTA point without a start time). */
        long elapsedMs() {
            return intervalKnown && startMs > 0 ? endMs - startMs : 0;
        }

        private static boolean sameInterval(long startMs, long endMs, long otherStartMs, long otherEndMs) {
            return Math.abs(startMs - otherStartMs) <= INTERVAL_TOLERANCE_MS
                    && Math.abs(endMs - otherEndMs) <= INTERVAL_TOLERANCE_MS;
        }
    }

    void setCpuJvm(double ratio) {
        cpuJvm = ratio;
    }

    void setCpuSystem(double ratio) {
        cpuSystem = ratio;
    }

    void addThreadCount(long count) {
        threadCount = add(threadCount, count);
    }

    void addClassCount(long count) {
        classCount = add(classCount, count);
    }

    void addClassUnloaded(long count) {
        classUnloaded = add(classUnloaded, count);
    }

    /** The agent-level unloaded-class total rebuilt from DELTA increments. */
    void setClassUnloaded(long count) {
        classUnloaded = count;
        classUnloadedTotalSet = true;
    }

    boolean hasClassUnloaded() {
        return classUnloaded != null;
    }

    /** True when the unloaded-class field holds the agent-level DELTA total ({@link #setClassUnloaded}). */
    boolean hasClassUnloadedTotal() {
        return classUnloadedTotalSet;
    }

    long getClassUnloaded() {
        return classUnloaded != null ? classUnloaded : UNCOLLECTED;
    }

    void addDirectBuffer(long count) {
        directCount = add(directCount, count);
    }

    void addDirectBufferMemory(long bytes) {
        directMemory = add(directMemory, bytes);
    }

    void addMappedBuffer(long count) {
        mappedCount = add(mappedCount, count);
    }

    void addMappedBufferMemory(long bytes) {
        mappedMemory = add(mappedMemory, bytes);
    }

    void addFileDescriptors(long count) {
        fileDescriptors = add(fileDescriptors, count);
    }

    /** One HTTP series' interval observations, durations already in millis. */
    void addHttp(long count, double sumMs, double maxMs) {
        httpSeen = true;
        httpCount += count;
        httpSumMs += sumMs;
        if (maxMs > httpMaxMs) {
            httpMaxMs = maxMs;
        }
    }

    /**
     * Connections in use of one pool; several series of one pool add up. A pool is its library (the
     * instrumentation scope without its version, empty when unknown) and its name: two libraries' pools
     * of the same name are two pools.
     */
    void addConnectionPoolUsed(String library, String poolName, short serviceTypeCode, long connections) {
        final ConnectionPool pool = connectionPool(library, poolName, serviceTypeCode);
        pool.used = add(pool.used, connections);
    }

    void addConnectionPoolMax(String library, String poolName, short serviceTypeCode, long connections) {
        final ConnectionPool pool = connectionPool(library, poolName, serviceTypeCode);
        pool.max = add(pool.max, connections);
    }

    private ConnectionPool connectionPool(String library, String poolName, short serviceTypeCode) {
        return connectionPools.computeIfAbsent(poolName + '\u0000' + library, key -> new ConnectionPool(library, poolName, serviceTypeCode));
    }

    private static Long add(@Nullable Long current, long value) {
        return current == null ? value : current + value;
    }

    /** @return true when at least one stat point was added */
    boolean build(AgentStatBo.Builder.StatBuilder statBuilder, MappingResult.Counters counters) {
        final DataPoint point = statBuilder.getDataPoint();
        boolean any = false;

        if (heapUsed != null) {
            final JvmGcType type = gcType != JvmGcType.UNKNOWN ? gcType : heapPoolGcType;
            statBuilder.addPoint(new JvmGcBo(point, type,
                    heapUsed, orUncollected(heapMaxTotal()),
                    orUncollected(nonHeapUsed), nonHeapMaxOrUncollected(),
                    gcOldCount, gcOldTimeMs));
            counters.mapped(AgentStatType.JVM_GC);
            any = true;
        }
        final Double jvmCpu = cpuJvm != null ? cpuJvm : cpuJvmFromTimes();
        final Double systemCpuLoad = cpuSystem != null ? cpuSystem : cpuSystemFromTimes();
        if (jvmCpu != null) {
            statBuilder.addPoint(new CpuLoadBo(point, jvmCpu, systemCpuLoad != null ? systemCpuLoad : UNCOLLECTED));
            counters.mapped(AgentStatType.CPU_LOAD);
            any = true;
        }
        if (threadCount != null) {
            statBuilder.addPoint(new TotalThreadCountBo(point, threadCount));
            counters.mapped(AgentStatType.TOTAL_THREAD);
            any = true;
        }
        if (classCount != null) {
            statBuilder.addPoint(new LoadedClassBo(point, classCount, orUncollected(classUnloaded)));
            counters.mapped(AgentStatType.LOADED_CLASS);
            any = true;
        }
        if (directCount != null || directMemory != null || mappedCount != null || mappedMemory != null) {
            statBuilder.addPoint(new DirectBufferBo(point,
                    orUncollected(directCount), orUncollected(directMemory),
                    orUncollected(mappedCount), orUncollected(mappedMemory)));
            counters.mapped(AgentStatType.DIRECT_BUFFER);
            any = true;
        }
        if (fileDescriptors != null) {
            statBuilder.addPoint(new FileDescriptorBo(point, fileDescriptors));
            counters.mapped(AgentStatType.FILE_DESCRIPTOR);
            any = true;
        }
        if (httpSeen) {
            final long avg = httpCount > 0 ? Math.round(httpSumMs / httpCount) : 0;
            final long max = httpCount > 0 ? Math.round(httpMaxMs) : 0;
            statBuilder.addPoint(new ResponseTimeBo(point, avg, max));
            counters.mapped(AgentStatType.RESPONSE_TIME);
            any = true;
        }
        final List<DataSourceBo> dataSources = dataSources(point);
        if (!dataSources.isEmpty()) {
            statBuilder.addPoint(new DataSourceListBo(point, dataSources));
            counters.mapped(AgentStatType.DATASOURCE);
            any = true;
        }
        return any;
    }

    /**
     * One {@link DataSourceBo} per pool with a used count. OTel and Micrometer report no JDBC URL, so
     * the pool name stands in for the URL and the database name (the OTel Java agent names a HikariCP
     * pool left at its generated name after the URL, {@code host:port/db}); the id is a stable hash of
     * the pool's library and name, so a pool keeps its series across exports and restarts, and two
     * libraries' pools of the same name stay two series. The agent's web chart finds a pool by its
     * JDBC URL alone, so when two libraries' pools share a name their URLs carry the library as well
     * ({@code orders (apache-dbcp-2.0)}); a name used once stays as it is.
     */
    private List<DataSourceBo> dataSources(DataPoint point) {
        if (connectionPools.isEmpty()) {
            return List.of();
        }
        final Map<String, Integer> poolsByName = new HashMap<>();
        for (ConnectionPool pool : connectionPools.values()) {
            if (pool.used != null) {
                poolsByName.merge(pool.name, 1, Integer::sum);
            }
        }
        final List<DataSourceBo> list = new ArrayList<>(connectionPools.size());
        for (ConnectionPool pool : connectionPools.values()) {
            if (pool.used == null) {
                continue;
            }
            final String jdbcUrl = poolsByName.get(pool.name) > 1 ? pool.name + " (" + libraryLabel(pool.library) + ")" : pool.name;
            list.add(new DataSourceBo(point, dataSourceId(pool.library, pool.name), pool.serviceTypeCode, pool.name, jdbcUrl,
                    toInt(pool.used), pool.max != null ? toInt(pool.max) : (int) UNCOLLECTED));
        }
        return list;
    }

    /** The pool library for a person: the OTel scope without its common prefix. */
    static String libraryLabel(String library) {
        if (library.isEmpty()) {
            return "no scope";
        }
        return library.startsWith(OTEL_SCOPE_PREFIX) ? library.substring(OTEL_SCOPE_PREFIX.length()) : library;
    }

    static int dataSourceId(String library, String poolName) {
        return (library + '|' + poolName).hashCode() & Integer.MAX_VALUE;
    }

    private static int toInt(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    /** The non-heap max only when every non-heap pool seen in use reported a limit, like MemoryMXBean. */
    private long nonHeapMaxOrUncollected() {
        if (nonHeapMax == null || !nonHeapPoolsLimited.containsAll(nonHeapPoolsUsed)) {
            return UNCOLLECTED;
        }
        return nonHeapMax;
    }

    private static long orUncollected(@Nullable Long value) {
        return value != null ? value : UNCOLLECTED;
    }
}
