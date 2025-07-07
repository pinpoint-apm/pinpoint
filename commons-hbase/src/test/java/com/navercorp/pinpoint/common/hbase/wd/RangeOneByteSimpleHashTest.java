package com.navercorp.pinpoint.common.hbase.wd;

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

        byte[] allPossiblePrefixes = hash.getAllPossiblePrefixes(new byte[]{hashPrefix});
        assertEquals(16, allPossiblePrefixes.length);

        byte[] distributedKey = rowKeyDistributor.getDistributedKey(bytes);
        byte[] originalKey = rowKeyDistributor.getOriginalKey(distributedKey);

        Assertions.assertArrayEquals(bytes, originalKey);

        byte[][] allDistributedKeys = rowKeyDistributor.getAllDistributedKeys(bytes);
        assertEquals(16, allDistributedKeys.length);

    }
}