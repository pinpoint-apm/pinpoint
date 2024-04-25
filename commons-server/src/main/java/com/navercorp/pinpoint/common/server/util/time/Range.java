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

package com.navercorp.pinpoint.common.server.util.time;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.common.util.Assert;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author netspider
 */
public final class Range {

    private final Instant from;
    private final Instant to;

    private Range(Instant from, Instant to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    public static Range between(long from, long to) {
        return between(toInstant(from), toInstant(to));
    }

    private static Instant toInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    public static Range between(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        final Range range = new Range(from, to);
        range.validate();
        return range;
    }

    /**
     * @deprecated Since 3.0.0 Use {@link #between(Instant, Instant)}
     */
    @Deprecated
    public static Range newRange(TimeUnit timeUnit, long duration, long toTimestamp) {
        Assert.isTrue(duration > 0, "duration must be '> 0'");
        Assert.isTrue(toTimestamp > 0, "toTimestamp must be '> 0'");

        final long durationMillis = timeUnit.toMillis(duration);

        return Range.between(toTimestamp - durationMillis, toTimestamp);
    }

    public static Range unchecked(long from, long to) {
        return unchecked(toInstant(from), toInstant(to));
    }

    public static Range unchecked(Instant from, Instant to) {
        return new Range(from, to);
    }

    /**
     * @deprecated Since 3.0.0 Use {@link #unchecked(long, long)}
     */
    @Deprecated
    public static Range newUncheckedRange(long from, long to) {
        return unchecked(from, to);
    }

    public long getFrom() {
        return from.toEpochMilli();
    }

    @JsonIgnore
    public Instant getFromInstant() {
        return from;
    }

    public String getFromDateTime() {
        return DateTimeFormatUtils.formatSimple(from);
    }

    public long getTo() {
        return to.toEpochMilli();
    }

    @JsonIgnore
    public Instant getToInstant() {
        return to;
    }

    public String getToDateTime() {
        return DateTimeFormatUtils.formatSimple(to);
    }

    @JsonIgnore
    public long getRange() {
        return to.toEpochMilli() - from.toEpochMilli();
    }

    public long durationMillis() {
        return duration().toMillis();
    }

    public Duration duration() {
        return Duration.between(from, to);
    }

    public void validate() {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("invalid range:" + this);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (!from.equals(range.from)) return false;
        return to.equals(range.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Range{" +
                DateTimeFormatUtils.formatSimple(from) +
                ' ' +
                getSign(from, to) +
                " " + DateTimeFormatUtils.formatSimple(to) +
                " duration=" + duration() +
                '}';
    }

    static char getSign(Instant from, Instant to) {
        int compareTo = from.compareTo(to);
        if (compareTo < 0) {
            return '<';
        } else if (compareTo == 0) {
            return '=';
        } else {
            return '>';
        }
    }

    public String prettyToString() {
        return "Range{" + "from=" + DateTimeFormatUtils.formatSimple(from) +
                ", to=" + DateTimeFormatUtils.formatSimple(to) +
                ", duration=" + TimeUnit.MILLISECONDS.toSeconds(durationMillis()) +
                '}';
    }
}
