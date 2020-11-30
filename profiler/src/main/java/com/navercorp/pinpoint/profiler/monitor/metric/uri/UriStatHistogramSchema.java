/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.uri;

/**
 * @author Taejin Koo
 */
public enum UriStatHistogramSchema {

    VERY_FAST(0, 10, 0),
    FAST_1(10, 30, 1),
    FAST_2(30, 50, 2),
    FAST_3(50, 100, 3),
    NORMAL_1(100, 300, 4),
    NORMAL_2(300, 500, 5),
    NORMAL_3(500, 1000, 6),
    SLOW_1(1000, 3000, 7),
    SLOW_2(3000, 5000, 8),
    VERY_SLOW(5000, Long.MAX_VALUE, 9);

    private final long from;
    private final long to;
    private final int index;

    UriStatHistogramSchema(long from, long to, int index) {
        this.from = from;
        this.to = to;
        this.index = index;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public int getIndex() {
        return index;
    }

    public static UriStatHistogramSchema getValue(long elapsed) {
        for (UriStatHistogramSchema histogram : values()) {
            if (elapsed < histogram.getTo()) {
                return histogram;
            }
        }

        return VERY_SLOW;
    }

}
