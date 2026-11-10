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

import java.util.Locale;
import java.util.Map;

/**
 * Duration units as they appear in the OTLP {@code Metric.unit} field. OTel SDKs use the UCUM
 * abbreviations ({@code s}, {@code ms}), Micrometer writes the {@link java.util.concurrent.TimeUnit}
 * name in lower case ({@code milliseconds}). An unknown unit is never guessed: silently treating
 * {@code milliseconds} as seconds would store GC and response times a thousand times too large.
 */
public final class DurationUnits {

    public static final double MILLIS_PER_SECOND = 1_000d;
    public static final double MILLIS_PER_MILLI = 1d;

    private static final Map<String, Double> MILLIS_PER_UNIT = Map.ofEntries(
            Map.entry("s", MILLIS_PER_SECOND),
            Map.entry("sec", MILLIS_PER_SECOND),
            Map.entry("second", MILLIS_PER_SECOND),
            Map.entry("seconds", MILLIS_PER_SECOND),
            Map.entry("ms", MILLIS_PER_MILLI),
            Map.entry("millisecond", MILLIS_PER_MILLI),
            Map.entry("milliseconds", MILLIS_PER_MILLI),
            Map.entry("us", 1d / 1_000d),
            Map.entry("µs", 1d / 1_000d),
            Map.entry("μs", 1d / 1_000d),
            Map.entry("microsecond", 1d / 1_000d),
            Map.entry("microseconds", 1d / 1_000d),
            Map.entry("ns", 1d / 1_000_000d),
            Map.entry("nanosecond", 1d / 1_000_000d),
            Map.entry("nanoseconds", 1d / 1_000_000d),
            Map.entry("min", 60 * MILLIS_PER_SECOND),
            Map.entry("minute", 60 * MILLIS_PER_SECOND),
            Map.entry("minutes", 60 * MILLIS_PER_SECOND),
            Map.entry("h", 3_600 * MILLIS_PER_SECOND),
            Map.entry("hour", 3_600 * MILLIS_PER_SECOND),
            Map.entry("hours", 3_600 * MILLIS_PER_SECOND)
    );

    private DurationUnits() {
    }

    /**
     * Millis per one unit, or {@code defaultMillisPerUnit} when the metric carries no unit (the
     * convention's default), or {@link Double#NaN} when the unit is present but not a duration unit
     * this class knows. Case-insensitive; UCUM braces such as {@code {request}} are not durations.
     */
    public static double millisPerUnit(String unit, double defaultMillisPerUnit) {
        if (unit == null || unit.isEmpty()) {
            return defaultMillisPerUnit;
        }
        final Double factor = MILLIS_PER_UNIT.get(unit.toLowerCase(Locale.ROOT));
        if (factor == null) {
            return Double.NaN;
        }
        return factor;
    }

    public static boolean isKnown(String unit) {
        return unit != null && MILLIS_PER_UNIT.containsKey(unit.toLowerCase(Locale.ROOT));
    }
}
