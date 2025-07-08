/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class RangeDoubleHashTest {

    @Test
    void getHashPrefix() {
        int secondaryMod = 4;
        RangeDoubleHash hash = new RangeDoubleHash(0, 12, ByteHasher.MAX_BUCKETS, secondaryMod);

        int service = 0;
        long application = 1;

        byte[] originalKey = rowkey(service, application);
        int keyModIndex = hash.firstIndex(originalKey);

        Set<Integer> modSet = secondaryModSet(hash, keyModIndex, secondaryMod);
        Assertions.assertEquals(secondaryMod, modSet.size());

        for (int i = 0; i < 100; i++) {
            int secondaryIndex = hash.getHashPrefix(originalKey);
            Assertions.assertTrue(modSet.contains(secondaryIndex));
        }

    }


    @Test
    void getDistributedKey() {

        int secondaryMod = 4;
        RangeDoubleHash hash = new RangeDoubleHash(0, 12, ByteHasher.MAX_BUCKETS, secondaryMod);
        RowKeyDistributor distributor = new RowKeyDistributorByHashPrefix(hash);

        int service = 0;
        long application = 1;

        byte[] originalKey = rowkey(service, application);
        byte[] distributedKey = distributor.getDistributedKey(originalKey);
        byte[] decodedKey = distributor.getOriginalKey(distributedKey);

        Assertions.assertArrayEquals(originalKey, decodedKey);
    }

    @Test
    void getDistributedKey_32Buckets() {

        int secondaryMod = 8;
        RangeDoubleHash hash = new RangeDoubleHash(0, 12, 32, secondaryMod);
        RowKeyDistributor distributor = new RowKeyDistributorByHashPrefix(hash);

        int service = 9;
        long application = 123456789L;

        byte[] originalKey = rowkey(service, application);
        byte[] distributedKey = distributor.getDistributedKey(originalKey);
        byte[] decodedKey = distributor.getOriginalKey(distributedKey);

        Assertions.assertArrayEquals(originalKey, decodedKey);
    }



    @Test
    void getAllPossiblePrefixes() {

        int secondaryMod = 4;
        RangeDoubleHash hash = new RangeDoubleHash(0, 12, ByteHasher.MAX_BUCKETS, secondaryMod);

        int service = 0;
        long application = 1;

        byte[] rowkey = rowkey(service, application);
        byte[] allDistributedKeys = hash.getAllPossiblePrefixes(rowkey);
        Assertions.assertEquals(secondaryMod, allDistributedKeys.length);
    }


    @Test
    void getAllDistributedKeys() {

        int secondaryMod = 4;
        RangeDoubleHash hash = new RangeDoubleHash(0, 12, ByteHasher.MAX_BUCKETS, secondaryMod);
        RowKeyDistributor distributor = new RowKeyDistributorByHashPrefix(hash);

        int service = 0;
        long application = 1;

        byte[] originalKey = rowkey(service, application);
        byte[][] allDistributedKeys = distributor.getAllDistributedKeys(originalKey);

        Assertions.assertEquals(secondaryMod, allDistributedKeys.length);
        for (byte[] distributedKey : allDistributedKeys) {
            byte[] decodedKey = distributor.getOriginalKey(distributedKey);
            Assertions.assertArrayEquals(originalKey, decodedKey);
        }
    }


    private Set<Integer> secondaryModSet(RangeDoubleHash hasher, int keyModIndex, int secondaryMod) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < secondaryMod; i++) {
            byte mod = (byte) hasher.secondaryModIndex(keyModIndex, i);
            set.add((int) mod);
        }
        return set;
    }


    byte[] rowkey(int service, long application) {
        return Bytes.add(Bytes.toBytes(service), Bytes.toBytes(application));

    }

}