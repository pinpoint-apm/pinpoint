/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.model;

import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author minwoo-jung
 */
public class Range {

    private final long from;
    private final long to;

    private Range(long from, long to) {
        this.from = from;
        this.to = to;
    }

    public static Range newRange(long from, long to) {
        final Range range = new Range(from, to);
        validate(range);
        return range;
    }

    public static Range newUncheckedRange(long from, long to) {
        return new Range(from, to);
    }

    public long getFrom() {
        return from;
    }

    public Instant getFromInstant() {
        return toInstant(from);
    }

    public Instant getToInstant() {
        return toInstant(to);
    }

    public String getFromDateTime() {
        return DateTimeFormatUtils.formatSimple(from);
    }

    public long getTo() {
        return to;
    }

    public String getToDateTime() {
        return DateTimeFormatUtils.formatSimple(to);
    }

    public long getRange() {
        return to - from;
    }

    public static void validate(Range range) {
        if (range.to < range.from) {
            throw new IllegalArgumentException("invalid range:" + range);
        }
    }

    private Instant toInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (from != range.from) return false;
        if (to != range.to) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (from ^ (from >>> 32));
        result = 31 * result + (int) (to ^ (to >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Range{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", range=").append(getRange());
        sb.append('}');
        return sb.toString();
    }

    public String prettyToString() {
        final StringBuilder sb = new StringBuilder("Range{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", range s=").append(TimeUnit.MILLISECONDS.toSeconds(getRange()));
        sb.append('}');
        return sb.toString();
    }
}
