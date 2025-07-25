/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase.wd;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RangeOneByteSimpleHashTest {


    @Test
    void getHashPrefix() {

        RangeOneByteSimpleHash hash = new RangeOneByteSimpleHash(0, 8, 16);
        RowKeyDistributor rowKeyDistributor = new RowKeyDistributorByHashPrefix(hash);

        byte[] bytes = Bytes.toBytes(7L);
        byte hashPrefix = hash.getHashPrefix(bytes);

        SaltKeyPrefix prefix = hash.getAllPrefixes(new byte[]{hashPrefix});
        assertEquals(16, prefix.size());

        byte[] distributedKey = rowKeyDistributor.getDistributedKey(bytes);
        byte[] originalKey = rowKeyDistributor.getOriginalKey(distributedKey);

        Assertions.assertArrayEquals(bytes, originalKey);

        byte[][] allDistributedKeys = rowKeyDistributor.getAllDistributedKeys(bytes);
        assertEquals(16, allDistributedKeys.length);

    }

    @Test
    void getHashPrefix_prefix0() {
        RangeOneByteSimpleHash hash = new RangeOneByteSimpleHash(0, 8, 16);

        byte[] bytes = Bytes.toBytes(7L);
        byte hashPrefix = hash.getHashPrefix(bytes);
        byte actualHashPrefix = hash.getHashPrefix(bytes, 0);

        assertEquals(hashPrefix, actualHashPrefix);
    }

    @Test
    void getHashPrefix_prefix1_long() {
        RangeOneByteSimpleHash hash = new RangeOneByteSimpleHash(0, 8, 16);

        int prefix = 1;
        long value = Long.MAX_VALUE - 100;
        byte[] bytes = Bytes.toBytes(value);
        byte hashPrefix = hash.getHashPrefix(bytes);

        byte[] prefixBytes = BytesUtils.add((byte) 1, bytes);
        byte actualHashPrefix = hash.getHashPrefix(prefixBytes, prefix);

        assertEquals(hashPrefix, actualHashPrefix);
        assertEquals(value, BytesUtils.bytesToLong(prefixBytes, prefix));
    }

    @Test
    void getHashPrefix_prefix1_int() {
        RangeOneByteSimpleHash hash = new RangeOneByteSimpleHash(0, 8, 16);

        int prefix = 1;
        int value = Integer.MAX_VALUE - 5;
        byte[] bytes = Bytes.toBytes(value);
        byte hashPrefix = hash.getHashPrefix(bytes);

        byte[] prefixBytes = BytesUtils.add((byte) 1, bytes);
        byte actualHashPrefix = hash.getHashPrefix(prefixBytes, prefix);

        assertEquals(hashPrefix, actualHashPrefix);
        assertEquals(value, BytesUtils.bytesToInt(prefixBytes, prefix));
    }
}