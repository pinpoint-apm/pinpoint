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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Per-series state that bridges OTLP temporality and what Inspector stores.
 * <ul>
 *     <li>{@link #delta(String, MetricPoint)}: a CUMULATIVE histogram (the SDK default) to the
 *     interval's observations, for {@code responseTime}. Needs the previous point of the series; the
 *     first point after a start, a process restart ({@code start_time} changed) or an eviction
 *     yields nothing and is reported as {@link InspectorDropReason#NO_PREVIOUS_POINT}.</li>
 *     <li>{@link #groupRunningTotal}: DELTA increments back to a running total, for the fields
 *     Inspector stores cumulatively ({@code gcOldCount}, the web applies the delta). All series of one
 *     agent-level total (every old-collector series, whatever its action/cause attributes) add into
 *     one group, de-duplicated per series by point time, so a series the SDK leaves out of an export because
 *     nothing was recorded (the OTel SDK does that under DELTA) does not make the total fall back to
 *     zero: {@link #groupTotal} hands the caller the last total. A DELTA point's {@code start_time}
 *     is the start of its interval and advances with every export (OTel SDK and Micrometer alike), so
 *     it says nothing about restarts; a restart is a new agent start time, which the caller folds
 *     into the key. State loss restarts the total from this process's view, which the web shows as
 *     one odd delta point.</li>
 *     <li>{@link #storedTotal}: what is actually stored for a cycle. The web turns the stored totals
 *     into per-point deltas and reads a fall as a restart (the whole value becomes one delta), so the
 *     stored totals of an agent must never fall in time order. An increment can arrive after a later
 *     export was stored (an upstream queue retrying, reordering); the later point then holds a total
 *     without it. The group keeps the totals it handed out per cycle time and fits every new one
 *     between its stored neighbours: a cycle in time order stores the current total, a late cycle the
 *     value that keeps the neighbours in order, a repeated cycle what it got the first time. A late
 *     increment therefore shows at the next cycle instead of its own, and the sum stays right.</li>
 * </ul>
 * For {@code delta} a point that does not advance the series' time (a retried export, out-of-order
 * delivery) leaves the baseline untouched. For the group totals only a point already seen (same series,
 * same time: a retried export) is left out; a late point with a time not seen before is a real
 * increment that arrived out of order (Micrometer sends a zero point every export, so a later zero
 * point can arrive before the earlier increment) and is added. Every transition runs as one atomic {@code compute} on the
 * series' entry, so overlapping requests for the same series (a retry racing the original) cannot
 * lose an update. Bounded by size and idle time; an agent that stops sending is forgotten after the
 * idle period. Inside a group the per-series point times follow the same period: times older than
 * that are pruned (a series without any is dropped), and a point older than the group's latest by that
 * much is ignored (the collector would not store it either), so a group holds only the recent past.
 */
public final class SeriesStateCache {

    private final Cache<String, SeriesState> cache;
    private final Cache<String, GroupState> groups;
    private final Cache<String, SourceOwner> owners;
    private final Cache<String, SeriesSet> seriesSets;
    private final long retentionMs;

    public SeriesStateCache(long maximumSize, Duration expireAfterAccess) {
        Objects.requireNonNull(expireAfterAccess, "expireAfterAccess");
        this.retentionMs = expireAfterAccess.toMillis();
        this.cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .build();
        this.groups = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .build();
        this.owners = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .build();
        this.seriesSets = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .build();
    }

    public long estimatedSize() {
        return cache.estimatedSize() + groups.estimatedSize() + owners.estimatedSize() + seriesSets.estimatedSize();
    }

    /**
     * The source that owns {@code key} (one Inspector role of one agent) after this export, whose best
     * source is {@code candidate} (a lower {@code rank} is better; equal ranks compare by source name).
     * The owner is remembered: a better candidate takes over at once, a worse one is refused while the
     * owner was seen within the retention period, so a source that leaves a series out of one export
     * (the OTel SDK sends no point for a DELTA series without recordings) does not hand the role to
     * another source for that export. An owner not seen for the retention period gives way.
     *
     * @return the owning source; the candidate's points are used only when it is the candidate
     */
    public String sourceOwner(String key, String candidate, int rank, long timeMs) {
        Objects.requireNonNull(candidate, "candidate");
        final SourceOwner owner = owners.asMap().compute(key, (k, current) -> {
            if (current == null || isBetter(rank, candidate, current) || timeMs - current.lastSeenMs() > retentionMs) {
                return new SourceOwner(candidate, rank, timeMs);
            }
            if (current.source().equals(candidate)) {
                return new SourceOwner(candidate, rank, Math.max(current.lastSeenMs(), timeMs));
            }
            return current;
        });
        return owner.source();
    }

    private static boolean isBetter(int rank, String source, SourceOwner current) {
        if (rank != current.rank()) {
            return rank < current.rank();
        }
        return source.compareTo(current.source()) < 0;
    }

    /**
     * @return the interval's observations, or {@code null} when there is no usable previous point
     * (the point is stored as the new baseline in that case) or the point does not advance the series
     * (the baseline is kept)
     */
    public @Nullable HistogramDelta delta(String key, MetricPoint point) {
        final HistogramValue current = point.histogram();
        if (current == null) {
            return null;
        }
        final HistogramDelta[] result = new HistogramDelta[1];
        cache.asMap().compute(key, (k, previous) -> {
            final SeriesState state = SeriesState.of(point, current);
            if (previous == null) {
                return state;
            }
            if (previous.timeMs >= point.timeMs()) {
                // retried or out-of-order point: already covered, keep the baseline where it is
                return previous;
            }
            if (previous.startTimeMs != point.startTimeMs()) {
                // process restart: new baseline
                return state;
            }
            final long count = current.count() - previous.count;
            final double sum = current.sum() - previous.sum;
            if (count < 0 || sum < 0) {
                // counter went backwards without a start_time change: treat as a reset
                return state;
            }
            result[0] = count == 0 ? HistogramDelta.EMPTY
                    : new HistogramDelta(count, sum, bucketMax(previous.bucketCounts, current, sum / count));
            return state;
        });
        return result[0];
    }

    /**
     * Whether the cycle at {@code timeMs} has every series that the agent's previous cycle under
     * {@code key} had; {@code series} becomes the one remembered. The first cycle, and a cycle not
     * newer than the remembered one (a retried or out-of-order export, which leaves it as it is),
     * cover it.
     */
    public boolean coversPreviousSeries(String key, long timeMs, Set<String> series) {
        Objects.requireNonNull(series, "series");
        final boolean[] covers = {true};
        seriesSets.asMap().compute(key, (k, previous) -> {
            if (previous != null && previous.timeMs() >= timeMs) {
                return previous;
            }
            if (previous != null) {
                covers[0] = series.containsAll(previous.series());
            }
            return new SeriesSet(timeMs, Set.copyOf(series));
        });
        return covers[0];
    }

    private record SeriesSet(long timeMs, Set<String> series) {
    }

    /** A running total's increase and the time it took. */
    public record CounterIncrease(double value, long elapsedMs) {
    }

    /**
     * The increase of a CUMULATIVE running total (a monotonic Sum) since the previous point of the
     * series, with the same baseline rules as {@link #delta}: {@code null} when there is no usable
     * previous point (the point becomes the baseline: first point, process restart, a counter that went
     * backwards) or the point does not advance the series (a retried or out-of-order export).
     */
    public @Nullable CounterIncrease counterDelta(String key, MetricPoint point) {
        final CounterIncrease[] result = new CounterIncrease[1];
        cache.asMap().compute(key, (k, previous) -> {
            final SeriesState state = new SeriesState(point.startTimeMs(), point.timeMs(), 0, point.value(), List.of());
            if (previous == null) {
                return state;
            }
            if (previous.timeMs >= point.timeMs()) {
                return previous;
            }
            if (previous.startTimeMs != point.startTimeMs()) {
                return state;
            }
            final double increase = point.value() - previous.sum;
            if (increase < 0) {
                return state;
            }
            result[0] = new CounterIncrease(increase, point.timeMs() - previous.timeMs);
            return state;
        });
        return result[0];
    }

    /**
     * Adds one DELTA increment of {@code seriesKey} to the agent-level total {@code groupKey} and
     * returns the total so far. A point already seen for its series (same time: a retried export), or
     * older than the group's latest point by more than the retention, is not added; a late point with a
     * new time is. {@code sum} is in the unit the caller stores.
     */
    public HistogramDelta groupRunningTotal(String groupKey, String seriesKey, long timeMs, long count, double sum) {
        final GroupState state = groups.asMap().compute(groupKey, (k, previous) -> {
            if (previous == null) {
                return new GroupState(Map.of(seriesKey, new TreeSet<>(Set.of(timeMs))), timeMs, count, sum, EMPTY_STORED);
            }
            final long latestTimeMs = Math.max(previous.latestTimeMs, timeMs);
            final long horizon = latestTimeMs - retentionMs;
            if (timeMs < horizon) {
                // older than every series time still kept: it can no longer be told from a duplicate
                return previous;
            }
            final NavigableSet<Long> seen = previous.timesBySeries.get(seriesKey);
            if (seen != null && seen.contains(timeMs)) {
                // a retried export
                return previous;
            }
            final Map<String, NavigableSet<Long>> times = new HashMap<>();
            for (Map.Entry<String, NavigableSet<Long>> entry : previous.timesBySeries.entrySet()) {
                // times past the retention are dropped, and a series without any (a GC cause that
                // stopped occurring) with them
                final NavigableSet<Long> kept = new TreeSet<>(entry.getValue().tailSet(horizon, true));
                if (!kept.isEmpty()) {
                    times.put(entry.getKey(), kept);
                }
            }
            times.computeIfAbsent(seriesKey, key -> new TreeSet<>()).add(timeMs);
            return new GroupState(times, latestTimeMs, previous.count + count, previous.sum + sum, previous.stored);
        });
        return new HistogramDelta(state.count, state.sum, -1);
    }

    /**
     * The total to store for the cycle at {@code timeMs}, given the agent-level {@code total} the
     * caller has for it: clamped between the totals already stored for the nearest earlier and later
     * cycles, or the very value stored before for the same cycle time (a retry stores the same row).
     * Stored totals older than the retention are forgotten.
     */
    public HistogramDelta storedTotal(String groupKey, long timeMs, HistogramDelta total) {
        Objects.requireNonNull(total, "total");
        final HistogramDelta[] result = new HistogramDelta[1];
        groups.asMap().compute(groupKey, (k, previous) -> {
            final GroupState state = previous != null ? previous
                    : new GroupState(Map.of(), timeMs, total.count(), total.sum(), EMPTY_STORED);
            final HistogramDelta same = state.stored.get(timeMs);
            if (same != null) {
                result[0] = same;
                return state;
            }
            long count = total.count();
            double sum = total.sum();
            final Map.Entry<Long, HistogramDelta> earlier = state.stored.lowerEntry(timeMs);
            if (earlier != null) {
                count = Math.max(count, earlier.getValue().count());
                sum = Math.max(sum, earlier.getValue().sum());
            }
            final Map.Entry<Long, HistogramDelta> later = state.stored.higherEntry(timeMs);
            if (later != null) {
                count = Math.min(count, later.getValue().count());
                sum = Math.min(sum, later.getValue().sum());
            }
            final HistogramDelta value = new HistogramDelta(count, sum, -1);
            final NavigableMap<Long, HistogramDelta> stored = new TreeMap<>(state.stored);
            stored.put(timeMs, value);
            stored.headMap(stored.lastKey() - retentionMs, false).clear();
            result[0] = value;
            return new GroupState(state.timesBySeries, state.latestTimeMs, state.count, state.sum, stored);
        });
        return result[0];
    }

    /** The agent-level total so far, or {@code null} when no increment has been seen (or it expired). */
    public @Nullable HistogramDelta groupTotal(String groupKey) {
        final GroupState state = groups.getIfPresent(groupKey);
        return state == null ? null : new HistogramDelta(state.count, state.sum, -1);
    }

    /**
     * Upper bound of the highest bucket that grew between the two cumulative points. The cumulative
     * {@code max} field is a lifetime maximum, so it is never used here. When only the overflow bucket
     * grew, its upper bound is unknown: the estimate is the last finite bound or the interval mean,
     * whichever is larger, so the stored max is never below the average.
     */
    private static double bucketMax(List<Long> previousCounts, HistogramValue current, double mean) {
        final List<Long> counts = current.bucketCounts();
        if (counts.isEmpty() || previousCounts.size() != counts.size()) {
            return mean;
        }
        final List<Double> bounds = current.bounds();
        for (int i = counts.size() - 1; i >= 0; i--) {
            if (counts.get(i) - previousCounts.get(i) > 0) {
                if (i < bounds.size()) {
                    return bounds.get(i);
                }
                return bounds.isEmpty() ? mean : Math.max(bounds.get(bounds.size() - 1), mean);
            }
        }
        return mean;
    }

    private record SeriesState(long startTimeMs, long timeMs, long count, double sum, List<Long> bucketCounts) {
        static SeriesState of(MetricPoint point, HistogramValue value) {
            return new SeriesState(point.startTimeMs(), point.timeMs(), value.count(), value.sum(), value.bucketCounts());
        }
    }

    private record SourceOwner(String source, int rank, long lastSeenMs) {
    }

    private static final NavigableMap<Long, HistogramDelta> EMPTY_STORED = Collections.emptyNavigableMap();

    /**
     * One agent-level running total, per contributing series the times of its recent points, and the
     * totals handed out for storage per cycle time (the recent ones, see {@link #storedTotal}).
     */
    private record GroupState(Map<String, NavigableSet<Long>> timesBySeries, long latestTimeMs, long count, double sum,
                              NavigableMap<Long, HistogramDelta> stored) {
    }

    /** The number of series times a group keeps, for tests. */
    int groupSeriesCount(String groupKey) {
        final GroupState state = groups.getIfPresent(groupKey);
        return state == null ? 0 : state.timesBySeries.size();
    }
}
