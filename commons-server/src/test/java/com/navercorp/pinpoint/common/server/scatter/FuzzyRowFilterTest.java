package com.navercorp.pinpoint.common.server.scatter;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class FuzzyRowFilterTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() {
        byte[] a1 = {'?', 5};
        byte[] a2 = {'?', 6};
        byte[] fuzzy = {1, 0};
        Pair<byte[], byte[]> fuzzyPair1 = new Pair<>(a1, fuzzy);
        Pair<byte[], byte[]> fuzzyPair2 = new Pair<>(a2, fuzzy);
        FuzzyRowFilter filter = new FuzzyRowFilter(Arrays.asList(fuzzyPair1, fuzzyPair2));

        KeyValue keyValue = new KeyValue(new byte[]{0, 1}, 1L);
        Filter.ReturnCode returnCode = filter.filterKeyValue(keyValue);
        Assert.assertEquals(returnCode, Filter.ReturnCode.SEEK_NEXT_USING_HINT);

        KeyValue keyValue2 = new KeyValue(new byte[]{0, 5}, 1L);
        Filter.ReturnCode returnCode2 = filter.filterKeyValue(keyValue2);
        Assert.assertEquals(returnCode2, Filter.ReturnCode.INCLUDE);
    }

    @Test
    public void test_reverseTimeStamp() {
        for (int i = 0; i < 560; i += 1) {
            short j = reverseTimestamp((short) i);
            logger.debug(i + " hex:" + Integer.toHexString(i) + " rev:" + j + " rhex:" + Integer.toHexString(j));
        }
    }

    short reverseTimestamp(short time) {
        return (short) (Short.MAX_VALUE - time);
    }
}

