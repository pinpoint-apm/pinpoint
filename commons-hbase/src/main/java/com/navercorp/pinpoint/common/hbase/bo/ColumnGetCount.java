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

/**
 * @author Taejin Koo
 */
public class ColumnGetCount {

    public static final ColumnGetCount UNLIMITED_COLUMN_GET_COUNT = new UnlimitedColumnGetCount();

    private final int limit;
    private int resultSize;

    public ColumnGetCount(int limit) {
        Assert.isTrue(limit > 0, "limit must be 'limit >= 0'");
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isreachedLimit() {
        return resultSize >= limit;
    }

    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
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

    private static class UnlimitedColumnGetCount extends ColumnGetCount {

        private UnlimitedColumnGetCount() {
            super(Integer.MAX_VALUE);
        }

        @Override
        public boolean isreachedLimit() {
            return false;
        }

        @Override
        public void setResultSize(int resultSize) {
            // skip
        }

    }

}
