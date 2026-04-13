/*
 * Copyright 2025 NAVER Corp.
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

import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Objects;

/**
 * @author emeroad
 */
public final class Timestamp {
    private static final Timestamp ZERO = new Timestamp(0);

    // ISO 8601 Basic format: 20260413T053000Z or 20260413T053000.999Z
    public static final DateTimeFormatter BASIC_ISO_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyyMMdd'T'HHmmss")
            .optionalStart()
            .appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
            .optionalEnd()
            .appendPattern("X")
            .toFormatter();

    private final long epochMillis;

    private Timestamp(long epochMillis) {
        if (epochMillis < 0) {
            throw new IllegalArgumentException("epochMillis must not be negative: " + epochMillis);
        }
        this.epochMillis = epochMillis;
    }

    public static Timestamp zero() {
        return ZERO;
    }

    public static Timestamp now() {
        return ofEpochMilli(System.currentTimeMillis());
    }

    public static Timestamp ofEpochMilli(long epochMillis) {
        return new Timestamp(epochMillis);
    }

    public static Timestamp valueOf(String dateTime) {
        Objects.requireNonNull(dateTime, "dateTime");

        if (StringUtils.isNumeric(dateTime)) {
            return new Timestamp(Long.parseLong(dateTime));
        }
        long epochMillis = parseDateTime(dateTime);
        return new Timestamp(epochMillis);
    }

    private static long parseDateTime(String dateTime) {
        int tIndex = dateTime.indexOf('T');
        if (tIndex == 8) {
            // ISO 8601 Basic: 20260413T053000Z or 20260413T053000.999Z
            return OffsetDateTime.parse(dateTime, BASIC_ISO_FORMAT).toInstant().toEpochMilli();
        }
        // ISO 8601 Extended: 2026-04-13T05:30:00Z
        return OffsetDateTime.parse(dateTime).toInstant().toEpochMilli();
    }

    public long getEpochMillis() {
        return epochMillis;
    }

    public Instant toInstant() {
        return Instant.ofEpochMilli(epochMillis);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Timestamp timestamp = (Timestamp) o;
        return epochMillis == timestamp.epochMillis;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(epochMillis);
    }

    @Override
    public String toString() {
        return "Timestamp{" +
                Instant.ofEpochMilli(epochMillis) +
                ", epochMillis=" + epochMillis +
                '}';
    }
}
