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

package com.navercorp.pinpoint.common.hbase.bo;

import com.navercorp.pinpoint.common.util.Assert;
import org.apache.hadoop.hbase.filter.ColumnCountGetFilter;
import org.apache.hadoop.hbase.filter.Filter;

/**
 * @author Taejin Koo
 */
public class ColumnGetCount {

    public static final int UNLIMITED_COUNT = Integer.MAX_VALUE;
    public static final ColumnGetCount UNLIMITED_COLUMN_GET_COUNT = new ColumnGetCount(UNLIMITED_COUNT);

    private final int limit;

    public static ColumnGetCount of(int limit) {
        if (limit == -1 || limit == UNLIMITED_COUNT) {
            return ColumnGetCount.UNLIMITED_COLUMN_GET_COUNT;
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive or -1(unlimited): " + limit);
        }
        return new ColumnGetCount(limit);
    }

    ColumnGetCount(int limit) {
        Assert.isTrue(limit > 0, "limit must be 'limit >= 0'");
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isReachedLimit(int resultSize) {
        if (limit == UNLIMITED_COUNT) {
            return false;
        }
        return resultSize >= limit;
    }

    public static Filter toFilter(ColumnGetCount columnGetCount) {
        if (columnGetCount == null) {
            return null;
        }
        if (columnGetCount.getLimit() != UNLIMITED_COUNT) {
            return new ColumnCountGetFilter(columnGetCount.getLimit());
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnGetCount that = (ColumnGetCount) o;

        return limit == that.limit;
    }

    @Override
    public int hashCode() {
        return limit;
    }

}
