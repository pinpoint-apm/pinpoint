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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.function.LongSupplier;

/**
 * Maps the OTLP metrics of one Resource (one JVM) to the {@link AgentStatBo} the native agent would
 * have sent, so the Inspector storage path ({@code AgentStatService}) and the web's chart
 * definitions are reused unchanged. Transport- and Spring-free; the OTLP trace collector hosts it.
 *
 * <p>Rules:
 * <ul>
 *     <li>Metric names are resolved to a {@link MetricRole} through the {@link MetricVocabulary}
 *     (OTel semantic conventions, Micrometer); a name without a role is dropped and counted. The
 *     mapper never stores arbitrary metrics.</li>
 *     <li>One collection cycle becomes one stat point per chart family ({@link InspectorStatSnapshot}),
 *     stamped with the latest {@code time_unix_nano} of its points. Points are not grouped by exact
 *     timestamp: Micrometer stamps each meter with its own wall-clock read and, under DELTA, aligns
 *     histograms to the step boundary, so one export carries several instants. Points are taken in
 *     time order and a series that is already in the open cycle starts the next one (an upstream
 *     batch of several cycles); a series that first appears later joins the cycle open at that
 *     time.</li>
 *     <li>Durations are converted to millis by the metric's unit ({@link DurationUnits}); ratios
 *     (0..1) and bytes are kept.</li>
 *     <li>Fields Inspector stores as running totals ({@code gcOldCount/Time}, {@code unLoaded}) take
 *     CUMULATIVE input as is; DELTA input is re-accumulated per agent in the {@link SeriesStateCache}
 *     (all series of the field in one total), and an export that carries no point of the field (the
 *     OTel SDK sends none under DELTA when nothing was recorded) repeats the last total.</li>
 *     <li>Only charted families are produced: {@code jvmGcDetailed} (young GC, per-pool ratios) has no
 *     chart in the web and is not generated; young-collector series are {@code ignored_series}.</li>
 *     <li>{@code responseTime} needs the interval's observations: DELTA histograms are used as is,
 *     CUMULATIVE ones are differenced against the previous point in the cache (no previous point:
 *     dropped as {@link InspectorDropReason#NO_PREVIOUS_POINT}).</li>
 *     <li>{@code dataSource} is one entry per connection pool: connections in use and the pool
 *     maximum, the pool name in place of the JDBC URL (neither OTel nor Micrometer reports one) and the
 *     pool library as the service type ({@link ConnectionPoolTypes}). Idle-state series are
 *     {@code ignored_series}.</li>
 *     <li>transaction, activeTrace and deadlock have no OTel counterpart and are not produced.</li>
 *     <li>A point stamped later than the collector's clock by more than the allowed skew is dropped as
 *     {@link InspectorDropReason#INVALID_POINT} before it touches any state: the per-series state only
 *     moves forward in time, so one such point (a client clock that jumped ahead) would make every
 *     later point of the series look old for as long as the agent keeps exporting.</li>
 * </ul>
 */
public final class OtlpMetricInspectorMapper {

    /** {@link #findProcessStartTime} result when the Resource carries no start-time metric. */
    public static final long NO_START_TIME = -1;

    // Same sanity window as the trace path's process.creation.time resolver.
    private static final long MIN_VALID_EPOCH_MILLIS = 946_684_800_000L; // 2000-01-01T00:00:00Z
    private static final long MAX_FUTURE_SKEW_MILLIS = TimeUnit.MINUTES.toMillis(5);

    // agent-level DELTA totals (appended to the identity's series key prefix)
    private static final String GC_OLD_TOTAL = "total:gc.old";
    private static final String CLASS_UNLOADED_TOTAL = "total:class.unloaded";
    // per role, the source whose points are used (appended to the identity's series key prefix)
    private static final String SOURCE_OWNER = "owner:";

    private final MetricVocabulary vocabulary;
    private final SeriesStateCache stateCache;
    private final GcNameClassifier gcClassifier = new GcNameClassifier();
    private final LongSupplier clock;

    public OtlpMetricInspectorMapper(MetricVocabulary vocabulary, SeriesStateCache stateCache) {
        this(vocabulary, stateCache, System::currentTimeMillis);
    }

    /** @param clock the collector's time in epoch millis, the reference of the future-skew checks */
    OtlpMetricInspectorMapper(MetricVocabulary vocabulary, SeriesStateCache stateCache, LongSupplier clock) {
        this.vocabulary = Objects.requireNonNull(vocabulary, "vocabulary");
        this.stateCache = Objects.requireNonNull(stateCache, "stateCache");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public OtlpMetricInspectorMapper(MetricDialect dialect, SeriesStateCache stateCache) {
        this(MetricVocabulary.of(dialect), stateCache);
    }

    public MetricVocabulary getVocabulary() {
        return vocabulary;
    }

    /**
     * The process start time a dialect reports as a metric (Micrometer {@code process.start.time}),
     * in epoch millis, or {@link #NO_START_TIME}. Cheap (name lookups only); meant to be called
     * before {@link #map} so the identity carries the real start time.
     */
    public long findProcessStartTime(List<Metric> metrics) {
        for (Metric metric : metrics) {
            if (vocabulary.roleOf(metric.getName()) != MetricRole.START_TIME) {
                continue;
            }
            final List<MetricPoint> points = OtlpMetricPointReader.read(metric);
            if (points == null) {
                continue;
            }
            for (MetricPoint point : points) {
                final MetricVocabulary.Match match = vocabulary.resolve(point.name(), point.attributes());
                if (match == null || point.kind() == PointKind.HISTOGRAM) {
                    continue;
                }
                final double millisPerUnit = DurationUnits.millisPerUnit(point.unit(), match.dialect().defaultDurationMillisPerUnit());
                if (Double.isNaN(millisPerUnit) || !Double.isFinite(point.value())) {
                    continue;
                }
                final long epochMillis = Math.round(point.value() * millisPerUnit);
                if (epochMillis >= MIN_VALID_EPOCH_MILLIS && epochMillis <= clock.getAsLong() + MAX_FUTURE_SKEW_MILLIS) {
                    return epochMillis;
                }
            }
        }
        return NO_START_TIME;
    }

    /**
     * Maps the metrics of one Resource, keeping each metric's instrumentation scope in its series
     * identity (see {@link #map(InspectorAgentIdentity, List)} for the rules).
     */
    public MappingResult map(InspectorAgentIdentity identity, ResourceMetrics resourceMetrics) {
        Objects.requireNonNull(resourceMetrics, "resourceMetrics");
        final List<ScopedMetric> metrics = new ArrayList<>();
        for (ScopeMetrics scopeMetrics : resourceMetrics.getScopeMetricsList()) {
            final String scope = OtlpMetricPointReader.scopeKey(scopeMetrics.getScope());
            for (Metric metric : scopeMetrics.getMetricsList()) {
                metrics.add(new ScopedMetric(scope, metric));
            }
        }
        return mapScoped(identity, metrics);
    }

    /**
     * Maps metrics whose instrumentation scope is not known. Two scopes reporting the same metric name
     * and attributes are different series; this entry point treats every metric as one scope.
     */
    public MappingResult map(InspectorAgentIdentity identity, List<Metric> metrics) {
        Objects.requireNonNull(metrics, "metrics");
        final List<ScopedMetric> scoped = new ArrayList<>(metrics.size());
        for (Metric metric : metrics) {
            scoped.add(new ScopedMetric(MetricPoint.NO_SCOPE, metric));
        }
        return mapScoped(identity, scoped);
    }

    private MappingResult mapScoped(InspectorAgentIdentity identity, List<ScopedMetric> metrics) {
        Objects.requireNonNull(identity, "identity");

        final MappingResult.Counters counters = new MappingResult.Counters();
        List<ResolvedPoint> resolved = new ArrayList<>();
        final IntConsumer noRecordedValue = count -> counters.dropped(InspectorDropReason.NO_RECORDED_VALUE, count);
        final long latestValidTimeMs = clock.getAsLong() + MAX_FUTURE_SKEW_MILLIS;

        for (ScopedMetric scopedMetric : metrics) {
            final Metric metric = scopedMetric.metric();
            final int pointCount = OtlpMetricPointReader.countPoints(metric);
            counters.received(pointCount);
            if (vocabulary.roleOf(metric.getName()) == null) {
                counters.dropped(InspectorDropReason.UNKNOWN_METRIC, pointCount);
                continue;
            }
            final List<MetricPoint> points = OtlpMetricPointReader.read(metric, scopedMetric.scope(), noRecordedValue);
            if (points == null) {
                counters.dropped(InspectorDropReason.UNSUPPORTED_TYPE, pointCount);
                continue;
            }
            for (MetricPoint point : points) {
                final MetricVocabulary.Match match = vocabulary.resolve(point.name(), point.attributes());
                if (match == null) {
                    counters.dropped(InspectorDropReason.UNKNOWN_METRIC, 1);
                    continue;
                }
                if (point.timeMs() > latestValidTimeMs) {
                    // stamped in the future: it must not become a series' latest point
                    counters.dropped(InspectorDropReason.INVALID_POINT, 1);
                    continue;
                }
                resolved.add(new ResolvedPoint(match, point));
            }
        }
        resolved = keepOneSourcePerRole(resolved, identity.seriesKeyPrefix(), counters);
        resolved = preferGcCycles(resolved, counters);
        if (resolved.isEmpty()) {
            return counters.build(null);
        }

        // Time order: the state cache must advance with the series, and a repeated series marks the
        // next collection cycle.
        resolved.sort(Comparator.comparingLong(resolvedPoint -> resolvedPoint.point().timeMs()));
        final String keyPrefix = identity.seriesKeyPrefix();
        // the agent-level DELTA totals as they stood before this export, carried forward cycle by cycle
        HistogramDelta oldGcTotal = stateCache.groupTotal(keyPrefix + GC_OLD_TOTAL);
        HistogramDelta unloadedTotal = stateCache.groupTotal(keyPrefix + CLASS_UNLOADED_TOTAL);

        final List<InspectorStatSnapshot> cycles = new ArrayList<>(1);
        Set<String> seriesInCycle = new HashSet<>();
        InspectorStatSnapshot snapshot = null;
        int from = 0;
        while (from < resolved.size()) {
            // The points of one instant are placed together: when any of them repeats a series of the
            // open cycle, the whole instant starts the next cycle, whatever order they arrived in.
            final long instant = resolved.get(from).point().timeMs();
            int to = from;
            boolean startsCycle = snapshot == null;
            while (to < resolved.size() && resolved.get(to).point().timeMs() == instant) {
                if (seriesInCycle.contains(resolved.get(to).point().seriesKey())) {
                    startsCycle = true;
                }
                to++;
            }
            for (int i = from; i < to; i++) {
                final ResolvedPoint resolvedPoint = resolved.get(i);
                final MetricPoint point = resolvedPoint.point();
                if (startsCycle || !seriesInCycle.add(point.seriesKey())) {
                    // the open cycle already has this series: this point starts the next cycle
                    startsCycle = false;
                    snapshot = new InspectorStatSnapshot();
                    cycles.add(snapshot);
                    seriesInCycle = new HashSet<>();
                    seriesInCycle.add(point.seriesKey());
                }
                snapshot.observe(point.timeMs());
                final InspectorDropReason dropped = accept(snapshot, resolvedPoint.match(), point, keyPrefix);
                if (dropped != null) {
                    counters.dropped(dropped, 1);
                } else {
                    counters.accepted(resolvedPoint.match().dialect().name());
                }
            }
            from = to;
        }

        // A DELTA field absent from a cycle (nothing recorded in the interval) keeps the total as it stood
        // at that cycle, not the total after the whole export. What is stored then goes through the
        // agent's stored-total history, so an increment that arrives after a later export (retry,
        // reordering upstream) never makes the stored totals fall in time order.
        for (InspectorStatSnapshot cycle : cycles) {
            cycle.checkCpuSeriesAgainstPreviousCycle(stateCache, keyPrefix);
            HistogramDelta oldGc = null;
            if (cycle.hasOldGcTotal()) {
                oldGc = new HistogramDelta(cycle.getGcOldCount(), cycle.getGcOldTimeMs(), -1);
                oldGcTotal = oldGc;
            } else if (oldGcTotal != null && !cycle.hasOldGc()) {
                oldGc = oldGcTotal;
            }
            if (oldGc != null) {
                final HistogramDelta stored = stateCache.storedTotal(keyPrefix + GC_OLD_TOTAL, cycle.getTimeMs(), oldGc);
                cycle.setOldGcTotal(stored.count(), Math.round(stored.sum()));
            }
            HistogramDelta unloaded = null;
            if (cycle.hasClassUnloadedTotal()) {
                unloaded = new HistogramDelta(0, cycle.getClassUnloaded(), -1);
                unloadedTotal = unloaded;
            } else if (unloadedTotal != null && !cycle.hasClassUnloaded()) {
                unloaded = unloadedTotal;
            }
            if (unloaded != null) {
                final HistogramDelta stored = stateCache.storedTotal(keyPrefix + CLASS_UNLOADED_TOTAL, cycle.getTimeMs(), unloaded);
                cycle.setClassUnloaded(Math.round(stored.sum()));
            }
        }

        final AgentStatBo.Builder builder = AgentStatBo.newBuilder(identity.serviceName(), identity.applicationName(),
                identity.agentId(), identity.agentStartTime());
        boolean any = false;
        for (InspectorStatSnapshot cycle : cycles) {
            final AgentStatBo.Builder.StatBuilder statBuilder = builder.newStatBuilder(cycle.getTimeMs());
            if (cycle.build(statBuilder, counters)) {
                any = true;
            }
        }
        return counters.build(any ? builder.build() : null);
    }

    private record ResolvedPoint(MetricVocabulary.Match match, MetricPoint point) {
    }

    private record ScopedMetric(String scope, Metric metric) {
    }

    /**
     * One source per Inspector value. A source is an instrumentation scope with its dialect. When an
     * agent's Resource carries the same value (heap, threads, GC, ...) from more than one source — the
     * Micrometer bridge or the JMX Insight JVM rules next to the runtime-telemetry metrics — adding
     * them would count the JVM twice, whatever their names and attributes, so per role only the
     * points of one source are used and the others are {@link InspectorDropReason#DUPLICATE_SOURCE}.
     * Ranking: the runtime-telemetry scopes, then the {@code otel} dialect, then {@code micrometer},
     * then the source name. The choice is remembered per agent and role ({@link SeriesStateCache#sourceOwner}),
     * so a source that leaves a series out of one export (DELTA without recordings) does not hand the
     * value to another source for that export.
     * <p>
     * Server request durations and connection pools of different scopes of one dialect are different
     * requests or pools (one scope per server or pool library) and add up; of two dialects they are the
     * same ones measured twice, so only the dialect is chosen there.
     * The start time role is not a chart and is left alone.
     */
    private List<ResolvedPoint> keepOneSourcePerRole(List<ResolvedPoint> resolved, String keyPrefix, MappingResult.Counters counters) {
        if (resolved.isEmpty()) {
            return resolved;
        }
        final Map<MetricRole, Source> best = new EnumMap<>(MetricRole.class);
        long latestMs = 0;
        for (ResolvedPoint resolvedPoint : resolved) {
            latestMs = Math.max(latestMs, resolvedPoint.point().timeMs());
            final MetricRole role = resolvedPoint.match().role();
            if (role == MetricRole.START_TIME) {
                continue;
            }
            best.merge(role, Source.of(resolvedPoint), Source::better);
        }
        final Map<MetricRole, String> owners = new EnumMap<>(MetricRole.class);
        for (Map.Entry<MetricRole, Source> entry : best.entrySet()) {
            final Source candidate = entry.getValue();
            owners.put(entry.getKey(), stateCache.sourceOwner(keyPrefix + SOURCE_OWNER + entry.getKey().name(),
                    candidate.name(), candidate.rank(), latestMs));
        }
        final List<ResolvedPoint> kept = new ArrayList<>(resolved.size());
        for (ResolvedPoint resolvedPoint : resolved) {
            final String owner = owners.get(resolvedPoint.match().role());
            if (owner != null && !owner.equals(Source.of(resolvedPoint).name())) {
                counters.dropped(InspectorDropReason.DUPLICATE_SOURCE, 1);
                continue;
            }
            kept.add(resolvedPoint);
        }
        return kept;
    }

    /**
     * A runtime that reports its GC cycles as their own metric (Go {@code go.memory.gc.cycles} with
     * {@code go.memory.gc.pause.duration}) counts them from there; a pause histogram of the same dialect
     * that also counts cycles (Go's deprecated {@code process.runtime.go.gc.pause_ns}) would count them
     * twice and is a {@link InspectorDropReason#DUPLICATE_SOURCE}.
     */
    private static List<ResolvedPoint> preferGcCycles(List<ResolvedPoint> resolved, MappingResult.Counters counters) {
        Set<String> cycleDialects = null;
        for (ResolvedPoint resolvedPoint : resolved) {
            final MetricRole role = resolvedPoint.match().role();
            if (role == MetricRole.GC_CYCLES || role == MetricRole.GC_PAUSE_TIME) {
                if (cycleDialects == null) {
                    cycleDialects = new HashSet<>(2);
                }
                cycleDialects.add(resolvedPoint.match().dialect().name());
            }
        }
        if (cycleDialects == null) {
            return resolved;
        }
        final List<ResolvedPoint> kept = new ArrayList<>(resolved.size());
        for (ResolvedPoint resolvedPoint : resolved) {
            if (resolvedPoint.match().role() == MetricRole.GC_DURATION && cycleDialects.contains(resolvedPoint.match().dialect().name())) {
                counters.dropped(InspectorDropReason.DUPLICATE_SOURCE, 1);
                continue;
            }
            kept.add(resolvedPoint);
        }
        return kept;
    }

    /**
     * A source of one role's points: its name ({@code scope#dialect}, or {@code #dialect} for server
     * durations and connection pools) and rank.
     */
    private record Source(String name, int rank) {

        static final String RUNTIME_TELEMETRY_SCOPE = "io.opentelemetry.runtime-telemetry";

        static Source of(ResolvedPoint resolvedPoint) {
            final MetricPoint point = resolvedPoint.point();
            final String dialect = resolvedPoint.match().dialect().name();
            final int dialectRank = OtelSemconvDialect.NAME.equals(dialect) ? 1 : 2;
            if (isSourcePerDialect(resolvedPoint.match().role())) {
                return new Source('#' + dialect, dialectRank);
            }
            final int rank = point.scope().startsWith(RUNTIME_TELEMETRY_SCOPE) ? 0 : dialectRank;
            return new Source(point.scope() + '#' + dialect, rank);
        }

        private static boolean isSourcePerDialect(MetricRole role) {
            return role == MetricRole.HTTP_SERVER_DURATION
                    || role == MetricRole.CONNECTION_POOL_USED
                    || role == MetricRole.CONNECTION_POOL_MAX;
        }

        static Source better(Source a, Source b) {
            if (a.rank != b.rank) {
                return a.rank < b.rank ? a : b;
            }
            return a.name.compareTo(b.name) <= 0 ? a : b;
        }
    }

    /** @return the reason the point produced nothing, or {@code null} when it was folded in */
    private @Nullable InspectorDropReason accept(InspectorStatSnapshot snapshot, MetricVocabulary.Match match, MetricPoint point, String keyPrefix) {
        switch (match.role()) {
            case MEMORY_USED:
                return memory(snapshot, match.dialect(), point, true);
            case MEMORY_LIMIT:
                return memory(snapshot, match.dialect(), point, false);
            case GC_DURATION:
                return gcDuration(snapshot, match.dialect(), point, keyPrefix);
            case GC_CYCLES:
                return gcCycles(snapshot, match.dialect(), point, keyPrefix);
            case GC_PAUSE_TIME:
                return gcPauseTime(snapshot, match.dialect(), point, keyPrefix);
            case CPU_JVM:
                return number(point, snapshot::setCpuJvm);
            case CPU_SYSTEM:
                return number(point, snapshot::setCpuSystem);
            case PROCESS_CPU_TIME:
            case SYSTEM_CPU_TIME:
            case RUNTIME_CPU_TIME:
                return cpuTime(snapshot, match, point, keyPrefix);
            case CPU_COUNT:
                return count(point, snapshot::setCpuCount);
            case THREAD_COUNT:
                return count(point, snapshot::addThreadCount);
            case CLASS_COUNT:
                return count(point, snapshot::addClassCount);
            case CLASS_UNLOADED:
                return classUnloaded(snapshot, point, keyPrefix);
            case BUFFER_COUNT:
                return buffer(point, snapshot::addDirectBuffer, snapshot::addMappedBuffer);
            case BUFFER_MEMORY_USED:
                return buffer(point, snapshot::addDirectBufferMemory, snapshot::addMappedBufferMemory);
            case FILE_DESCRIPTOR:
                return count(point, snapshot::addFileDescriptors);
            case HTTP_SERVER_DURATION:
                return httpDuration(snapshot, match.dialect(), point, keyPrefix);
            case CONNECTION_POOL_USED:
                return connectionPool(snapshot, match.dialect(), point, true);
            case CONNECTION_POOL_MAX:
                return connectionPool(snapshot, match.dialect(), point, false);
            case START_TIME:
                // Consumed by findProcessStartTime; a valid number is "accepted", not a chart value.
                return Double.isFinite(point.value()) && point.kind() != PointKind.HISTOGRAM ? null : InspectorDropReason.INVALID_POINT;
            default:
                return InspectorDropReason.UNKNOWN_METRIC;
        }
    }

    private @Nullable InspectorDropReason memory(InspectorStatSnapshot snapshot, MetricDialect dialect, MetricPoint point, boolean used) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!Double.isFinite(point.value())) {
            return InspectorDropReason.INVALID_POINT;
        }
        MetricVocabulary.MemoryType type = vocabulary.memoryType(point);
        if (type == null) {
            // a runtime that reports its memory limit without a type (Go's go.memory.limit)
            type = dialect.unlabeledMemoryType();
        }
        if (type == null && point.attribute(dialect.memoryTypeKey()) != null) {
            // a type the charts do not show (CPython's virtual memory size)
            return InspectorDropReason.IGNORED_SERIES;
        }
        if (type == null) {
            return InspectorDropReason.INVALID_POINT;
        }
        final boolean heap = type == MetricVocabulary.MemoryType.HEAP;
        final String poolName = vocabulary.memoryPoolName(point);
        if (heap && poolName != null) {
            snapshot.noteHeapPoolGcType(gcClassifier.heapPoolGcType(poolName));
        }
        if (point.value() < 0) {
            if (used) {
                return InspectorDropReason.INVALID_POINT;
            }
            // Micrometer reports jvm.memory.max=-1 for a pool without a limit (MemoryUsage.getMax()):
            // "no limit", not a value. Leave it out of the max sum, like a pool OTel omits.
            if (!heap) {
                snapshot.noteNonHeapPoolWithoutLimit(poolName);
            }
            return null;
        }
        final long bytes = Math.round(point.value());
        if (used) {
            if (heap) {
                snapshot.addHeapUsed(bytes);
            } else {
                snapshot.addNonHeapUsed(bytes, poolName);
            }
        } else {
            if (heap && poolName != null && gcClassifier.isSharedHeapPool(poolName)) {
                snapshot.addSharedHeapMax(bytes);
            } else if (heap) {
                snapshot.addHeapMax(bytes);
            } else {
                snapshot.addNonHeapMax(bytes, poolName);
            }
        }
        return null;
    }

    private @Nullable InspectorDropReason gcDuration(InspectorStatSnapshot snapshot, MetricDialect dialect, MetricPoint point, String keyPrefix) {
        final HistogramValue histogram = point.histogram();
        if (histogram == null) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        final String gcName = gcName(dialect, point);
        if (gcName == null) {
            return InspectorDropReason.INVALID_POINT;
        }
        final double millisPerUnit = millisPerUnit(dialect, point);
        if (Double.isNaN(millisPerUnit) || !Double.isFinite(histogram.sum())) {
            return InspectorDropReason.INVALID_POINT;
        }
        final GcClassification gc = gcClassifier.classify(gcName);
        // the young collector still tells the GC family (old collections may be rare)
        snapshot.noteGcType(gc.gcType());
        if (!gc.old()) {
            return InspectorDropReason.IGNORED_SERIES;
        }
        if (point.temporality() == Temporality.DELTA) {
            // every old-collector series of this agent adds into one total (already in millis)
            final HistogramDelta total = stateCache.groupRunningTotal(keyPrefix + GC_OLD_TOTAL, point.seriesKey(),
                    point.timeMs(), histogram.count(), histogram.sum() * millisPerUnit);
            snapshot.setOldGcTotal(total.count(), Math.round(total.sum()));
        } else {
            snapshot.addOldGc(histogram.count(), Math.round(histogram.sum() * millisPerUnit));
        }
        return null;
    }

    /**
     * The collector a GC point belongs to: its name attribute as the dialect reads it, else the dialect's
     * single collector (Go), else {@code null}.
     */
    private @Nullable String gcName(MetricDialect dialect, MetricPoint point) {
        String value = null;
        for (String key : dialect.gcNameKeys()) {
            value = point.attribute(key);
            if (value != null) {
                break;
            }
        }
        if (value == null) {
            value = vocabulary.gcName(point);
        }
        if (value != null) {
            return dialect.gcCollectorName(value);
        }
        // a runtime with one collector and no name attribute (Go)
        return dialect.unlabeledGcName();
    }

    /**
     * Completed GC cycles (a running total): the old GC count — every cycle of a non-generational
     * collector (Go, no collector attribute), the old collector's of a generational one (CPython's
     * generation 2); the other collectors are {@code ignored_series}.
     */
    private @Nullable InspectorDropReason gcCycles(InspectorStatSnapshot snapshot, MetricDialect dialect, MetricPoint point, String keyPrefix) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!isFiniteCount(point)) {
            return InspectorDropReason.INVALID_POINT;
        }
        final String gcName = gcName(dialect, point);
        if (gcName != null) {
            final GcClassification gc = gcClassifier.classify(gcName);
            snapshot.noteGcType(gc.gcType());
            if (!gc.old()) {
                return InspectorDropReason.IGNORED_SERIES;
            }
        }
        final long cycles = Math.round(point.value());
        if (point.isRunningTotal() && point.temporality() == Temporality.DELTA) {
            final HistogramDelta total = stateCache.groupRunningTotal(keyPrefix + GC_OLD_TOTAL, point.seriesKey(), point.timeMs(), cycles, 0);
            snapshot.setOldGcTotal(total.count(), Math.round(total.sum()));
        } else {
            snapshot.addOldGc(cycles, 0);
        }
        return null;
    }

    /**
     * GC pause time as the old GC time: a pause histogram's sum (its count, pauses rather than cycles, is
     * not used) or a running total of pause time (.NET, every generation).
     */
    private @Nullable InspectorDropReason gcPauseTime(InspectorStatSnapshot snapshot, MetricDialect dialect, MetricPoint point, String keyPrefix) {
        final HistogramValue histogram = point.histogram();
        final double pause;
        if (histogram != null) {
            pause = histogram.sum();
        } else if (point.isRunningTotal()) {
            pause = point.value();
        } else {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        final double millisPerUnit = millisPerUnit(dialect, point);
        if (Double.isNaN(millisPerUnit) || !Double.isFinite(pause) || pause < 0) {
            return InspectorDropReason.INVALID_POINT;
        }
        final double timeMs = pause * millisPerUnit;
        if (point.temporality() == Temporality.DELTA) {
            final HistogramDelta total = stateCache.groupRunningTotal(keyPrefix + GC_OLD_TOTAL, point.seriesKey(), point.timeMs(), 0, timeMs);
            snapshot.setOldGcTotal(total.count(), Math.round(total.sum()));
        } else {
            snapshot.addOldGc(0, Math.round(timeMs));
        }
        return null;
    }

    /**
     * One series of a CPU running total: its increase over the interval (DELTA as sent, CUMULATIVE
     * differenced against the previous point; the first point has none and is
     * {@link InspectorDropReason#NO_PREVIOUS_POINT}, which also leaves that metric out of the cycle's
     * ratio).
     */
    private @Nullable InspectorDropReason cpuTime(InspectorStatSnapshot snapshot, MetricVocabulary.Match match, MetricPoint point, String keyPrefix) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!isFiniteCount(point)) {
            return InspectorDropReason.INVALID_POINT;
        }
        final Boolean idle = match.dialect().isIdleCpuTime(point);
        if (idle == null && match.role() != MetricRole.PROCESS_CPU_TIME) {
            // without the mode, idle time cannot be told apart and the ratio would be wrong
            return InspectorDropReason.INVALID_POINT;
        }
        final double millisPerUnit = millisPerUnit(match.dialect(), point);
        if (Double.isNaN(millisPerUnit)) {
            // a unit that is not a duration is not CPU time
            return InspectorDropReason.INVALID_POINT;
        }
        // the ratios combine metrics of possibly different units and the elapsed time: seconds for all
        final double secondsPerUnit = millisPerUnit / DurationUnits.MILLIS_PER_SECOND;
        final boolean cumulative = !(point.isRunningTotal() && point.temporality() == Temporality.DELTA);
        final Double increase;
        final long startMs;
        if (!cumulative) {
            increase = point.value() * secondsPerUnit;
            startMs = Math.max(0, point.startTimeMs());
        } else {
            final SeriesStateCache.CounterIncrease counterIncrease = stateCache.counterDelta(keyPrefix + point.seriesKey(), point);
            increase = counterIncrease != null ? counterIncrease.value() * secondsPerUnit : null;
            // the increase runs from the series' previous point, which may be older than the others'
            startMs = counterIncrease != null ? point.timeMs() - counterIncrease.elapsedMs() : 0;
        }
        final InspectorStatSnapshot.CpuTimeIncrease cpuTime = new InspectorStatSnapshot.CpuTimeIncrease(
                point.seriesKey(), increase, startMs, point.timeMs(), cumulative, Boolean.TRUE.equals(idle));
        switch (match.role()) {
            case PROCESS_CPU_TIME:
                snapshot.addProcessCpuTime(cpuTime);
                break;
            case SYSTEM_CPU_TIME:
                snapshot.addSystemCpuTime(cpuTime);
                break;
            default:
                snapshot.addRuntimeCpuTime(cpuTime);
                break;
        }
        return increase == null ? InspectorDropReason.NO_PREVIOUS_POINT : null;
    }

    private @Nullable InspectorDropReason httpDuration(InspectorStatSnapshot snapshot, MetricDialect dialect, MetricPoint point, String keyPrefix) {
        final HistogramValue histogram = point.histogram();
        if (histogram == null) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        final double millisPerUnit = millisPerUnit(dialect, point);
        if (Double.isNaN(millisPerUnit) || !Double.isFinite(histogram.sum())) {
            return InspectorDropReason.INVALID_POINT;
        }
        final HistogramDelta delta;
        if (point.temporality() == Temporality.DELTA) {
            final double max = histogram.hasMax() ? histogram.max() : histogram.approximateMax();
            delta = new HistogramDelta(histogram.count(), histogram.sum(), max);
        } else {
            delta = stateCache.delta(keyPrefix + point.seriesKey(), point);
            if (delta == null) {
                return InspectorDropReason.NO_PREVIOUS_POINT;
            }
        }
        snapshot.addHttp(delta.count(), delta.sum() * millisPerUnit, delta.max() < 0 ? delta.max() : delta.max() * millisPerUnit);
        return null;
    }

    /**
     * One pool's connections in use or maximum, read through the point's own dialect (no pool metric
     * name is shared between dialects). A count split by state keeps only the in-use series.
     */
    private static @Nullable InspectorDropReason connectionPool(InspectorStatSnapshot snapshot, MetricDialect dialect,
                                                                MetricPoint point, boolean used) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!isFiniteCount(point)) {
            return InspectorDropReason.INVALID_POINT;
        }
        final String poolName = firstAttribute(point, dialect.connectionPoolNameKeys());
        if (poolName == null || poolName.isEmpty()) {
            return InspectorDropReason.INVALID_POINT;
        }
        final short serviceTypeCode = ConnectionPoolTypes.serviceTypeCode(point.scope(), point.name());
        final String library = scopeName(point.scope());
        final long connections = Math.round(point.value());
        if (!used) {
            snapshot.addConnectionPoolMax(library, poolName, serviceTypeCode, connections);
            return null;
        }
        if (!dialect.connectionStateKeys().isEmpty()) {
            final String state = firstAttribute(point, dialect.connectionStateKeys());
            if (state == null) {
                return InspectorDropReason.INVALID_POINT;
            }
            if (!state.equals(dialect.connectionStateUsed())) {
                // idle connections: not a dataSource field
                return InspectorDropReason.IGNORED_SERIES;
            }
        }
        snapshot.addConnectionPoolUsed(library, poolName, serviceTypeCode, connections);
        return null;
    }

    /**
     * The instrumentation scope without its version ({@link OtlpMetricPointReader#scopeKey} appends
     * {@code @version}; JS scope names themselves start with {@code @}), so a pool keeps its identity
     * across instrumentation upgrades.
     */
    static String scopeName(String scope) {
        final int at = scope.lastIndexOf('@');
        return at > 0 ? scope.substring(0, at) : scope;
    }

    private static @Nullable String firstAttribute(MetricPoint point, List<String> keys) {
        for (String key : keys) {
            final String value = point.attribute(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** Millis per unit of a duration point; NaN when the unit is not a known duration unit. */
    private static double millisPerUnit(MetricDialect dialect, MetricPoint point) {
        return DurationUnits.millisPerUnit(point.unit(), dialect.defaultDurationMillisPerUnit());
    }

    private @Nullable InspectorDropReason buffer(MetricPoint point, LongSink direct, LongSink mapped) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!isFiniteCount(point)) {
            return InspectorDropReason.INVALID_POINT;
        }
        final MetricVocabulary.BufferPool pool = vocabulary.bufferPool(point);
        if (pool == null) {
            return InspectorDropReason.INVALID_POINT;
        }
        switch (pool) {
            case DIRECT:
                direct.accept(Math.round(point.value()));
                return null;
            case MAPPED:
                mapped.accept(Math.round(point.value()));
                return null;
            default:
                // JDK 14+ reports a third pool, "mapped - 'non-volatile memory'"; Inspector has no chart for it.
                return InspectorDropReason.IGNORED_SERIES;
        }
    }

    private @Nullable InspectorDropReason classUnloaded(InspectorStatSnapshot snapshot, MetricPoint point, String keyPrefix) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!isFiniteCount(point)) {
            return InspectorDropReason.INVALID_POINT;
        }
        if (point.isRunningTotal() && point.temporality() == Temporality.DELTA) {
            final HistogramDelta total = stateCache.groupRunningTotal(keyPrefix + CLASS_UNLOADED_TOTAL, point.seriesKey(),
                    point.timeMs(), 0, point.value());
            snapshot.setClassUnloaded(Math.round(total.sum()));
            return null;
        }
        snapshot.addClassUnloaded(Math.round(point.value()));
        return null;
    }

    private static @Nullable InspectorDropReason number(MetricPoint point, DoubleSink sink) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!Double.isFinite(point.value())) {
            return InspectorDropReason.INVALID_POINT;
        }
        sink.accept(point.value());
        return null;
    }

    private static @Nullable InspectorDropReason count(MetricPoint point, LongSink sink) {
        if (point.kind() == PointKind.HISTOGRAM) {
            return InspectorDropReason.UNSUPPORTED_TYPE;
        }
        if (!isFiniteCount(point)) {
            return InspectorDropReason.INVALID_POINT;
        }
        sink.accept(Math.round(point.value()));
        return null;
    }

    /** Counts and sizes are finite and never negative (a Micrometer gauge that could not read is NaN). */
    private static boolean isFiniteCount(MetricPoint point) {
        return Double.isFinite(point.value()) && point.value() >= 0;
    }

    @FunctionalInterface
    private interface LongSink {
        void accept(long value);
    }

    @FunctionalInterface
    private interface DoubleSink {
        void accept(double value);
    }
}
