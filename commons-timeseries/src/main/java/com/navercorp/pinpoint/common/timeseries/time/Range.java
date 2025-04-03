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

package com.navercorp.pinpoint.common.timeseries.time;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.time.Instant.ofEpochMilli;

/**
 * @author emeroad
 * @author netspider
 */
public final class Range {

    private final long from;
    private final long to;

    private Range(long from, long to) {
        this.from = from;
        this.to = to;
    }

    public static Range between(long from, long to) {
        final Range range = new Range(from, to);
        range.validate();
        return range;
    }

    public static Range between(Instant from, Instant to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return between(from.toEpochMilli(), to.toEpochMilli());
    }

    public static Range unchecked(long from, long to) {
        return unchecked(ofEpochMilli(from), ofEpochMilli(to));
    }

    public static Range unchecked(Instant from, Instant to) {
        return new Range(from.toEpochMilli(), to.toEpochMilli());
    }

    public long getFrom() {
        return from;
    }

    @JsonIgnore
    public Instant getFromInstant() {
        return ofEpochMilli(from);
    }

    public String getFromDateTime() {
        return DateTimeUtils.formatSimple(ofEpochMilli(from));
    }

    public long getTo() {
        return to;
    }

    @JsonIgnore
    public Instant getToInstant() {
        return ofEpochMilli(to);
    }

    public String getToDateTime() {
        return DateTimeUtils.formatSimple(to);
    }

    @JsonIgnore
    public long getRange() {
        return to - from;
    }

    public long durationMillis() {
        return to - from;
    }

    public Duration duration() {
        return Duration.ofMillis(durationMillis());
//        return Duration.between(ofEpochMilli(from), ofEpochMilli(to));
    }

    public void validate() {
        if (from > to) {
            throw new IllegalArgumentException("invalid range:" + this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;
        return from == range.from && to == range.to;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(from);
        result = 31 * result + Long.hashCode(to);
        return result;
    }

    @Override
    public String toString() {
        return "Range{" +
                DateTimeUtils.formatSimple(from) +
                ' ' +
                getSign(from, to) +
                " " + DateTimeUtils.formatSimple(to) +
                " duration=" + duration() +
                '}';
    }

    static char getSign(long from, long to) {
        int compareTo = Long.compare(from, to);
        if (compareTo < 0) {
            return '<';
        } else if (compareTo == 0) {
            return '=';
        } else {
            return '>';
        }
    }

    public String prettyToString() {
        return "Range{" + "from=" + DateTimeUtils.formatSimple(from) +
                ", to=" + DateTimeUtils.formatSimple(to) +
                ", duration=" + TimeUnit.MILLISECONDS.toSeconds(durationMillis()) +
                '}';
    }
}
