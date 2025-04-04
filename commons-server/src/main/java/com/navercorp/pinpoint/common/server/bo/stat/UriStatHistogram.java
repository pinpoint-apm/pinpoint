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

package com.navercorp.pinpoint.common.server.bo.stat;

import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class UriStatHistogram {
    public static final int URI_HISTOGRAM_SLOT = 8;

    private final long total;
    private final long max;
    private final List<Integer> timestampHistogram;

    public UriStatHistogram(long total, long max, List<Integer> timestampHistogram) {
        this.total = total;
        this.max = max;
        if (CollectionUtils.nullSafeSize(timestampHistogram) != URI_HISTOGRAM_SLOT) {
            throw new IllegalArgumentException("timestampHistogram.length must be 8");
        }
        this.timestampHistogram = timestampHistogram;
    }

    public long getTotal() {
        return total;
    }

    public long getMax() {
        return max;
    }

    public List<Integer> getTimestampHistogram() {
        return timestampHistogram;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UriStatHistogram that = (UriStatHistogram) o;
        return total == that.total && max == that.max && timestampHistogram.equals(that.timestampHistogram);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(total);
        result = 31 * result + Long.hashCode(max);
        result = 31 * result + timestampHistogram.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UriStatHistogram{" +
                "total=" + total +
                ", max=" + max +
                ", timestampHistogram=" + timestampHistogram +
                '}';
    }
}
