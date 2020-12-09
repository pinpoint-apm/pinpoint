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

package com.navercorp.pinpoint.common.trace;

/**
 * @author Taejin Koo
 */
public enum UriStatHistogramBucket {

    UNDER_100(0, 100, 0),
    RANGE_100_300(100, 300, 1),
    RANGE_300_500(300, 500, 2),
    RANGE_500_1000(500, 1000, 3),
    RANGE_1000_3000(1000, 3000, 4),
    RANGE_3000_5000(3000, 5000, 5),
    RANGE_5000_8000(5000, 8000, 6),
    OVER_8000MS(8000, Long.MAX_VALUE, 7);

    private final long from;
    private final long to;
    private final int index;
    private final String description;

    UriStatHistogramBucket(long from, long to, int index) {
        this.from = from;
        this.to = to;
        this.index = index;

        if (to == Long.MAX_VALUE) {
            description = from + " ~ ";
        } else {
            description = from + " ~ " + to;
        }
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

    public String getDesc() {
        return description;
    }

    public static UriStatHistogramBucket getValue(long elapsed) {
        for (UriStatHistogramBucket histogram : values()) {
            if (elapsed < histogram.getTo()) {
                return histogram;
            }
        }

        return OVER_8000MS;
    }

    public static UriStatHistogramBucket getValueByIndex(int index) {
        for (UriStatHistogramBucket histogram : values()) {
            if (histogram.getIndex() == index) {
                return histogram;
            }
        }

        throw new IllegalArgumentException("Can not find index. index:" + index);
    }

    public static int[] createNewArrayValue() {
        return new int[values().length];
    }

    public static byte getBucketVersion() {
        return 0;
    }
}
