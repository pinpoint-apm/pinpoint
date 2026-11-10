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

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SeriesStateCacheTest {

    private static final List<Double> BOUNDS = List.of(0.1, 0.5, 1.0);

    private final SeriesStateCache cache = new SeriesStateCache(100, Duration.ofMinutes(10));

    private static MetricPoint cumulative(long timeMs, long startMs, long count, double sum, List<Long> buckets) {
        return new MetricPoint("h", "s", Map.of(), timeMs, startMs, PointKind.HISTOGRAM, Temporality.CUMULATIVE, true, 0,
                new HistogramValue(count, sum, true, 99, BOUNDS, buckets));
    }

    private static MetricPoint delta(long timeMs, long count, double sum) {
        return new MetricPoint("h", "s", Map.of(), timeMs, 0, PointKind.HISTOGRAM, Temporality.DELTA, true, 0,
                HistogramValue.of(count, sum));
    }

    @Test
    void delta_needsPreviousPoint_thenDifferences() {
        assertThat(cache.delta("k", cumulative(1000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)))).isNull();

        final HistogramDelta delta = cache.delta("k", cumulative(2000, 1, 15, 4.5, List.of(5L, 6L, 4L, 0L)));
        assertThat(delta).isNotNull();
        assertThat(delta.count()).isEqualTo(5);
        assertThat(delta.sum()).isEqualTo(2.5);
        // highest bucket that grew is (0.5, 1.0]; the lifetime max field (99) is ignored
        assertThat(delta.max()).isEqualTo(1.0);
        assertThat(cache.estimatedSize()).isEqualTo(1);
    }

    @Test
    void delta_overflowBucketGrowth_isAtLeastTheMean() {
        cache.delta("k", cumulative(1000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)));
        // one observation of 3 s landed above the last bound (1.0): the max must not be below the mean
        final HistogramDelta delta = cache.delta("k", cumulative(2000, 1, 11, 5.0, List.of(4L, 4L, 2L, 1L)));
        assertThat(delta.max()).isEqualTo(3.0);
        // a small overflow observation keeps the last bound as the estimate
        final HistogramDelta small = cache.delta("k", cumulative(3000, 1, 12, 5.5, List.of(4L, 4L, 2L, 2L)));
        assertThat(small.max()).isEqualTo(1.0);
    }

    @Test
    void delta_noChange_isEmpty() {
        cache.delta("k", cumulative(1000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)));
        assertThat(cache.delta("k", cumulative(2000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)))).isSameAs(HistogramDelta.EMPTY);
    }

    @Test
    void delta_startTimeChange_isNewBaseline() {
        cache.delta("k", cumulative(1000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)));
        assertThat(cache.delta("k", cumulative(2000, 2, 3, 0.3, List.of(1L, 1L, 1L, 0L)))).isNull();
        // and the restarted series continues from there
        final HistogramDelta delta = cache.delta("k", cumulative(3000, 2, 4, 0.4, List.of(2L, 1L, 1L, 0L)));
        assertThat(delta).isNotNull();
        assertThat(delta.count()).isEqualTo(1);
        assertThat(delta.sum()).isCloseTo(0.1, within(1e-9));
        assertThat(delta.max()).isEqualTo(0.1);
    }

    @Test
    void delta_counterWentBackwards_isNewBaseline() {
        cache.delta("k", cumulative(1000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)));
        assertThat(cache.delta("k", cumulative(2000, 1, 8, 1.0, List.of(4L, 4L, 0L, 0L)))).isNull();
    }

    @Test
    void delta_outOfOrderPoint_isIgnoredAndDoesNotMoveBaseline() {
        cache.delta("k", cumulative(2000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)));
        assertThat(cache.delta("k", cumulative(1000, 1, 5, 1.0, List.of(2L, 2L, 1L, 0L)))).isNull();
        // a retry of the baseline point itself changes nothing either
        assertThat(cache.delta("k", cumulative(2000, 1, 10, 2.0, List.of(4L, 4L, 2L, 0L)))).isNull();
        // the next point is differenced against the kept baseline (10, 2.0), not the stale one
        final HistogramDelta next = cache.delta("k", cumulative(3000, 1, 20, 5.0, List.of(8L, 8L, 4L, 0L)));
        assertThat(next).isNotNull();
        assertThat(next.count()).isEqualTo(10);
        assertThat(next.sum()).isCloseTo(3.0, within(1e-9));
    }

    @Test
    void delta_withoutBuckets_maxIsMean() {
        cache.delta("k", new MetricPoint("h", "s", Map.of(), 1000, 1, PointKind.HISTOGRAM, Temporality.CUMULATIVE, true, 0, HistogramValue.of(2, 1.0)));
        final HistogramDelta delta = cache.delta("k", new MetricPoint("h", "s", Map.of(), 2000, 1, PointKind.HISTOGRAM, Temporality.CUMULATIVE, true, 0, HistogramValue.of(4, 1.6)));
        assertThat(delta).isNotNull();
        assertThat(delta.count()).isEqualTo(2);
        assertThat(delta.sum()).isCloseTo(0.6, within(1e-9));
        assertThat(delta.max()).isCloseTo(0.3, within(1e-9));
    }

    @Test
    void groupRunningTotal_addsEverySeriesOfTheGroup_evenAtTheSameInstant() {
        // two old-collector series (different action/cause attributes) of one export
        assertThat(cache.groupRunningTotal("g", "s1", 1000, 2, 100)).isEqualTo(new HistogramDelta(2, 100, -1));
        assertThat(cache.groupRunningTotal("g", "s2", 1000, 1, 30)).isEqualTo(new HistogramDelta(3, 130, -1));
        // next export, start_time of the interval moved on: still accumulates
        assertThat(cache.groupRunningTotal("g", "s1", 2000, 3, 200)).isEqualTo(new HistogramDelta(6, 330, -1));
        assertThat(cache.groupTotal("g")).isEqualTo(new HistogramDelta(6, 330, -1));
        assertThat(cache.groupTotal("other")).isNull();
    }

    @Test
    void groupRunningTotal_ignoresARepeatedPoint_butAddsALateOne() {
        cache.groupRunningTotal("g", "s1", 1000, 2, 100);
        cache.groupRunningTotal("g", "s1", 2000, 3, 200);
        // a retried export (same series, same time) is not added twice
        assertThat(cache.groupRunningTotal("g", "s1", 2000, 3, 200)).isEqualTo(new HistogramDelta(5, 300, -1));
        assertThat(cache.groupRunningTotal("g", "s1", 1000, 2, 100)).isEqualTo(new HistogramDelta(5, 300, -1));
        // an interval that arrives after a later one (reordering upstream) is a real increment
        assertThat(cache.groupRunningTotal("g", "s1", 1500, 9, 900)).isEqualTo(new HistogramDelta(14, 1200, -1));
        assertThat(cache.groupRunningTotal("g", "s1", 1500, 9, 900)).isEqualTo(new HistogramDelta(14, 1200, -1));
        // another series at an older time is new to the group and counts
        assertThat(cache.groupRunningTotal("g", "s2", 1500, 1, 10)).isEqualTo(new HistogramDelta(15, 1210, -1));
    }

    @Test
    void groupRunningTotal_prunesSeriesQuietForLongerThanTheRetention() {
        final long minute = Duration.ofMinutes(1).toMillis();
        cache.groupRunningTotal("g", "cause-a", 0, 1, 10);
        cache.groupRunningTotal("g", "cause-b", 5 * minute, 1, 10);
        assertThat(cache.groupSeriesCount("g")).isEqualTo(2);

        // cause-a has been quiet for 11 minutes (retention 10): its time is dropped, the total stays
        assertThat(cache.groupRunningTotal("g", "cause-b", 11 * minute, 1, 10)).isEqualTo(new HistogramDelta(3, 30, -1));
        assertThat(cache.groupSeriesCount("g")).isEqualTo(1);

        // a point that much older than the group's latest is ignored rather than counted again
        assertThat(cache.groupRunningTotal("g", "cause-a", 0, 1, 10)).isEqualTo(new HistogramDelta(3, 30, -1));
        // a pruned series that occurs again is new and counts
        assertThat(cache.groupRunningTotal("g", "cause-a", 12 * minute, 1, 10)).isEqualTo(new HistogramDelta(4, 40, -1));
        assertThat(cache.groupSeriesCount("g")).isEqualTo(2);
    }

    @Test
    void storedTotal_inTimeOrder_isTheTotal() {
        assertThat(cache.storedTotal("g", 1000, new HistogramDelta(1, 10, -1))).isEqualTo(new HistogramDelta(1, 10, -1));
        assertThat(cache.storedTotal("g", 2000, new HistogramDelta(3, 30, -1))).isEqualTo(new HistogramDelta(3, 30, -1));
    }

    @Test
    void storedTotal_ofALateCycle_staysBetweenItsStoredNeighbours() {
        cache.storedTotal("g", 1000, new HistogramDelta(1, 10, -1));
        // the t=3000 export arrived first, without the increment of t=2000
        cache.storedTotal("g", 3000, new HistogramDelta(1, 10, -1));
        // t=2000 arrives late with the total that now includes it: storing 2 would fall to 1 at t=3000
        assertThat(cache.storedTotal("g", 2000, new HistogramDelta(2, 25, -1))).isEqualTo(new HistogramDelta(1, 10, -1));
        // the next cycle in time order carries it
        assertThat(cache.storedTotal("g", 4000, new HistogramDelta(2, 25, -1))).isEqualTo(new HistogramDelta(2, 25, -1));
    }

    @Test
    void storedTotal_ofARepeatedCycle_isWhatItGotTheFirstTime() {
        cache.storedTotal("g", 1000, new HistogramDelta(1, 10, -1));
        cache.storedTotal("g", 2000, new HistogramDelta(1, 10, -1));
        // a retry of t=2000 after more increments were added: the same row again, not a different value
        assertThat(cache.storedTotal("g", 2000, new HistogramDelta(4, 40, -1))).isEqualTo(new HistogramDelta(1, 10, -1));
    }

    @Test
    void storedTotal_forgetsCyclesOlderThanTheRetention() {
        final long minute = Duration.ofMinutes(1).toMillis();
        cache.storedTotal("g", 0, new HistogramDelta(5, 50, -1));
        cache.storedTotal("g", 11 * minute, new HistogramDelta(6, 60, -1));
        // t=0 is gone, so a cycle at t=0 is no longer a repeat; it is still kept below its later neighbour
        assertThat(cache.storedTotal("g", 0, new HistogramDelta(9, 90, -1))).isEqualTo(new HistogramDelta(6, 60, -1));
    }

    @Test
    void restartedAgent_getsItsOwnSeriesKeys() {
        // a restart shows as a new agent start time, which is part of the key prefix
        assertThat(new InspectorAgentIdentity("DEFAULT", "app", "agent", 100).seriesKeyPrefix())
                .isNotEqualTo(new InspectorAgentIdentity("DEFAULT", "app", "agent", 200).seriesKeyPrefix());
    }

    @Test
    void seriesKey_isNameAndSortedAttributes() {
        final MetricPoint point = new MetricPoint("h", "s", Map.of("route", "/a", "method", "GET"), 1, 0, PointKind.GAUGE, Temporality.UNSPECIFIED, false, 1, null);
        assertThat(point.seriesKey()).isEqualTo("h{method=GET}{route=/a}");
        assertThat(new InspectorAgentIdentity("DEFAULT", "app", "agent", 0).seriesKeyPrefix()).isEqualTo("DEFAULT/app/agent@0|");
    }

    @Test
    void approximateMax_ofHistogramValue() {
        assertThat(new HistogramValue(0, 0, false, 0, BOUNDS, List.of(0L, 0L, 0L, 0L)).approximateMax()).isEqualTo(-1);
        assertThat(new HistogramValue(3, 0.9, false, 0, BOUNDS, List.of(1L, 2L, 0L, 0L)).approximateMax()).isEqualTo(0.5);
        // overflow bucket only: the mean (5.0) is above the last bound (1.0), so it wins
        assertThat(new HistogramValue(1, 5, false, 0, BOUNDS, List.of(0L, 0L, 0L, 1L)).approximateMax()).isEqualTo(5.0);
        assertThat(new HistogramValue(2, 1.2, false, 0, BOUNDS, List.of(0L, 0L, 1L, 1L)).approximateMax()).isEqualTo(1.0);
        assertThat(HistogramValue.of(4, 2.0).approximateMax()).isEqualTo(0.5);
    }
}
