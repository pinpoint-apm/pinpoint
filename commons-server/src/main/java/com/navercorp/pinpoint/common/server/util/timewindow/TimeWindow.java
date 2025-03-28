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

package com.navercorp.pinpoint.common.server.util.timewindow;

import com.navercorp.pinpoint.common.server.util.time.Range;

import java.time.Instant;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 
 * @author netspider
 * 
 */
public class TimeWindow implements Iterable<Long>, TimeWindowFunction {

    private final long windowSlotSize;

    private final Range range;

    private final Range windowRange;

    private final int windowRangeCount;

    public TimeWindow(Range range) {
        this(range, TimeWindowDownSampler.SAMPLER);
    }

    public TimeWindow(Range range, TimeWindowSampler sampler) {
        this.range = Objects.requireNonNull(range, "range");
        Objects.requireNonNull(sampler, "sampler");
        this.windowSlotSize = sampler.getWindowSize(range);
        this.windowRange = createWindowRange();
        this.windowRangeCount = computeWindowRangeCount(windowRange.durationMillis(), windowSlotSize);
    }

    /**
     * @throws ArithmeticException if the {@code count} overflows an int
     */
    private static int computeWindowRangeCount(long duration, long slotSize) {
        long count = (duration / slotSize) + 1;
        return Math.toIntExact(count);
    }

    public Iterator<Long> iterator() {
        return new Itr();
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
        return windowRange;
    }

    public long getWindowSlotSize() {
        return windowSlotSize;
    }

    public int getWindowRangeCount() {
        return windowRangeCount;
    }

    public Range getWindowSlotRange() {
        Instant scanFrom = windowRange.getFromInstant();
        long scanTo = windowRange.getTo() + getWindowSlotSize();
        return  Range.between(scanFrom, Instant.ofEpochMilli(scanTo));
    }

    private Range createWindowRange() {
        long from = refineTimestamp(range.getFrom());
        long to = refineTimestamp(range.getTo());
        return Range.between(from, to);
    }

    public int getWindowIndex(long time) {
        long index = (time - windowRange.getFrom()) / this.windowSlotSize;
        return (int)index;
    }

    private class Itr implements Iterator<Long> {

        private long cursor;
        private final long end;

        public Itr() {
            this.cursor = windowRange.getFrom();
            this.end = windowRange.getTo();
        }

        @Override
        public boolean hasNext() {
            return cursor <= end;
        }

        @Override
        public Long next() {
            long current = cursor;
            if (hasNext()) {
                cursor += windowSlotSize;
                return current;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
