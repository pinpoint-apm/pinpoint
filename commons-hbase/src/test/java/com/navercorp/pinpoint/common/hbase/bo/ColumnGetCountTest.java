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

import org.apache.hadoop.hbase.filter.ColumnCountGetFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Taejin Koo
 */
public class ColumnGetCountTest {

    @Test
    public void columnGetCountTest() {
        ColumnGetCount columnGetCount = ColumnGetCount.of(-1);
        Assertions.assertEquals(Integer.MAX_VALUE, columnGetCount.getLimit());

        columnGetCount = ColumnGetCount.of(Integer.MAX_VALUE);
        Assertions.assertEquals(Integer.MAX_VALUE, columnGetCount.getLimit());

        Assertions.assertFalse(columnGetCount.isReachedLimit(Integer.MAX_VALUE));

    }

    @Test
    public void columnGetCountTest2() {
        int countValue = 10;

        ColumnGetCount columnGetCount = ColumnGetCount.of(countValue);
        Assertions.assertEquals(countValue, columnGetCount.getLimit());

        Assertions.assertFalse(columnGetCount.isReachedLimit(0));

        Assertions.assertTrue(columnGetCount.isReachedLimit(++countValue));

    }

    @Test
    void toFilter() {
        ColumnCountGetFilter filter = (ColumnCountGetFilter) ColumnGetCount.toFilter(new ColumnGetCount(1));
        Assertions.assertEquals(1, filter.getLimit());
    }

    @Test
    void toFilter_null() {
        Assertions.assertNull(ColumnGetCount.toFilter(null));
        Assertions.assertNull(ColumnGetCount.toFilter(ColumnGetCount.UNLIMITED_COLUMN_GET_COUNT));
    }
}
