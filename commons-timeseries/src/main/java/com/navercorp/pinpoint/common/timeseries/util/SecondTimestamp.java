/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.timeseries.util;

/**
 * Based on 2010 seconds timestamp
 */
public final class SecondTimestamp {
    private SecondTimestamp() {
    }

    /**
     * The timestamp in milliseconds for January 1, 2010, 00:00:00 UTC.
     */
    private static final long BASE_TIMESTAMP_2010 = 1262304000000L;

    /**
     * Safely converts a long timestamp (in milliseconds, 1970-epoch based) to an int timestamp
     * representing seconds relative to a 2010-01-01 epoch.
     * Throws an exception if the converted value is outside the representable range of an int.
     *
     * @param millisTimestamp The long timestamp in milliseconds since the 1970 epoch.
     * @return The int timestamp in seconds relative to the 2010 epoch.
     * @throws ArithmeticException if the converted value cannot be safely cast to an int (approx. after 2078).
     */
    public static int convertSecondTimestamp(long millisTimestamp) {
        return convertSecondTimestampFromBaseTimestamp(millisTimestamp, BASE_TIMESTAMP_2010);
    }

    static int convertSecondTimestampFromBaseTimestamp(long millisTimestamp, long baseTimestamp) {
        // Calculate the relative milliseconds from the 2010 base timestamp.
        long relativeMillis = millisTimestamp - baseTimestamp;

        // Convert to seconds.
        long seconds = relativeMillis / 1000;

        if (seconds > Integer.MAX_VALUE || seconds < Integer.MIN_VALUE) {
            throw new ArithmeticException("Cannot safely cast to int: " + seconds);
        }
        return (int) seconds;
    }

    /**
     * Restores the original 1970-epoch based long timestamp (in milliseconds)
     * from an int timestamp that represents seconds relative to the 2010-01-01 epoch.
     *
     * @param secondsSince2010 The int value representing seconds since the 2010 epoch.
     * @return The restored long timestamp in milliseconds since the 1970 epoch.
     */
    public static long restoreSecondTimestamp(int secondsSince2010) {
        return restoreSecondTimestampFromBaseTimestamp(secondsSince2010, BASE_TIMESTAMP_2010);
    }

    static long restoreSecondTimestampFromBaseTimestamp(int secondsTimestamp, long baseTimestamp) {
        // Cast int to long and multiply by 1000 to get the relative milliseconds.
        long relativeMillis = (long) secondsTimestamp * 1000;

        // Add the base timestamp (2010) to restore the original 1970-epoch based timestamp.
        return relativeMillis + baseTimestamp;
    }

}
