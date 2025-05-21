/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.timeseries.window;

import com.navercorp.pinpoint.common.timeseries.time.Range;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 
 * @author netspider
 */
public class TimeWindow implements Iterable<Long>, TimeWindowFunction {

    private final long windowSlotSize;

    private final long from;
    private final long to;

    private final int windowRangeCount;

    public TimeWindow(Range range) {
        this(range, TimeWindowDownSampler.SAMPLER);
    }

    public TimeWindow(Range range, TimeWindowSampler sampler) {
        this(Objects.requireNonNull(range).getFrom(),
                range.getTo(), sampler);
    }

    TimeWindow(long rangeFrom, long rangeTo, TimeWindowSampler sampler) {
        Objects.requireNonNull(sampler, "sampler");
        this.windowSlotSize = sampler.getWindowSize(duration(rangeTo, rangeFrom));
        this.from = this.refineTimestamp(rangeFrom);
        this.to = this.refineTimestamp(rangeTo);
        this.windowRangeCount = computeWindowRangeCount(duration(to, from), windowSlotSize);
    }

    private static long duration(long to, long from) {
        return to - from;
    }

    /**
     * @throws ArithmeticException if the {@code count} overflows an int
     */
    private static int computeWindowRangeCount(long duration, long slotSize) {
        long count = (duration / slotSize) + 1;
        return Math.toIntExact(count);
    }

    @Override
    public Iterator<Long> iterator() {
        final int size = this.getWindowRangeCount();
        final long increment = getWindowSlotSize();
        return new TimeSeriesIterator(size, from, increment);
    }

    public List<Long> getTimeseriesWindows() {
        final int size = this.getWindowRangeCount();
        final long increment = getWindowSlotSize();
        return new TimeSeriesVirtualList(size, from, increment);
    }

    /**
     * converts the timestamp to the matching window slot's reference timestamp
     * 
     * @param timestamp
     * @return
     */
    @Override
    public long refineTimestamp(long timestamp) {
        return (timestamp / windowSlotSize) * windowSlotSize;
    }

    public Range getWindowRange() {
        return Range.between(from, to);
    }

    public long getWindowSlotSize() {
        return windowSlotSize;
    }

    public int getWindowRangeCount() {
        return windowRangeCount;
    }

    public Range getWindowSlotRange() {
        long scanTo = to + getWindowSlotSize();
        return  Range.between(from, scanTo);
    }


    public int getWindowIndex(long time) {
        long index = (time - from) / this.windowSlotSize;
        return (int) index;
    }

    @Override
    public String toString() {
        return "TimeWindow{" +
                "windowSlotSize=" + windowSlotSize +
                ", from=" + from +
                ", to=" + to +
                ", windowRangeCount=" + windowRangeCount +
                '}';
    }
}
