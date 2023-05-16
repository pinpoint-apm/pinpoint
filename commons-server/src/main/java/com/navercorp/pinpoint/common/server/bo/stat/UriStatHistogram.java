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

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class UriStatHistogram {

    private long total;
    private long max = 0;
    private int[] timestampHistogram;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public int[] getTimestampHistogram() {
        return timestampHistogram;
    }

    public void setTimestampHistogram(int[] timestampHistogram) {
        if (timestampHistogram == null || timestampHistogram.length != 8) {
            throw new IllegalArgumentException("timestampHistogram");
        }

        this.timestampHistogram = timestampHistogram;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriStatHistogram that = (UriStatHistogram) o;

        if (total != that.total) return false;
        if (max != that.max) return false;
        return Arrays.equals(timestampHistogram, that.timestampHistogram);
    }

    @Override
    public int hashCode() {
        int result;
        result = (int) (total ^ (total >>> 32));
        result = 31 * result + (int) (max ^ (max >>> 32));
        result = 31 * result + Arrays.hashCode(timestampHistogram);
        return result;
    }

    @Override
    public String toString() {
        return "UriStatHistogram{" +
                "total=" + total +
                ", max=" + max +
                ", timestampHistogram=" + Arrays.toString(timestampHistogram) +
                '}';
    }
}
