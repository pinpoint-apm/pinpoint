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

package com.navercorp.pinpoint.common.server.scatter;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FuzzyRowFilterTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() {
//        Jdk17Utils.assumeFalse();

        byte[] a1 = {'?', 5};
        byte[] a2 = {'?', 6};
        byte[] fuzzy = {1, 0};
        Pair<byte[], byte[]> fuzzyPair1 = new Pair<>(a1, fuzzy);
        Pair<byte[], byte[]> fuzzyPair2 = new Pair<>(a2, fuzzy);
        FuzzyRowFilter filter = new FuzzyRowFilter(List.of(fuzzyPair1, fuzzyPair2));

        KeyValue keyValue = new KeyValue(new byte[]{0, 1}, 1L);
        Filter.ReturnCode returnCode = filter.filterCell(keyValue);
        Assertions.assertEquals(Filter.ReturnCode.SEEK_NEXT_USING_HINT, returnCode);

        KeyValue keyValue2 = new KeyValue(new byte[]{0, 5}, 1L);
        Filter.ReturnCode returnCode2 = filter.filterCell(keyValue2);
        Assertions.assertEquals(Filter.ReturnCode.INCLUDE, returnCode2);
    }

    @Test
    @Disabled
    public void test_reverseTimeStamp() {
        for (int i = 0; i < 560; i += 1) {
            short j = reverseTimestamp((short) i);
            logger.debug("{} hex:{} rev:{} rhex:{}", i, Integer.toHexString(i), j, Integer.toHexString(j));
        }
    }

    short reverseTimestamp(short time) {
        return (short) (Short.MAX_VALUE - time);
    }
}

