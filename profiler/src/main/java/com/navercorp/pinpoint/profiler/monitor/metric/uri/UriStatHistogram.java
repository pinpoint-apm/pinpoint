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

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class UriStatHistogram {

    private static int HISTOGRAM_BUCKET_SIZE = UriStatHistogramBucket.values().length;

    private static byte BUCKET_VERSION = UriStatHistogramBucket.getBucketVersion();

    private int count;
    private long total;
    private long max = 0;
    private int[] timestampHistogram = new int[HISTOGRAM_BUCKET_SIZE];

    public void add(long elapsed) {
        count++;
        total += elapsed;

        this.max = Math.max(max, elapsed);

        UriStatHistogramBucket uriStatHistogramBucket = UriStatHistogramBucket.getValue(elapsed);
        timestampHistogram[uriStatHistogramBucket.getIndex()] = ++timestampHistogram[uriStatHistogramBucket.getIndex()];
    }

    private boolean isEmpty() {
        return count == 0;
    }

    public int getCount() {
        return count;
    }

    public long getTotal() {
        return total;
    }

    public long getMax() {
        return max;
    }

    public int[] getTimestampHistogram() {
        return timestampHistogram;
    }

    public byte getBucketVersion() {
        return BUCKET_VERSION;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UriStatHistogram{");
        sb.append("count=").append(count);
        sb.append(", total=").append(total);
        sb.append(", max=").append(max);
        sb.append(", timestampHistogram=").append(Arrays.toString(timestampHistogram));
        sb.append('}');
        return sb.toString();
    }

}
