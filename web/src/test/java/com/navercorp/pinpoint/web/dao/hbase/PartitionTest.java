package com.navercorp.pinpoint.web.dao.hbase;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PartitionTest {

    private List<Integer> original = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);


    @Test
    public void splitTransactionIdList() throws Exception {

        assertPartition(1);
        assertPartition(2);
        assertPartition(3);
        assertPartition(5);
        assertPartition(10);
        assertPartition(11);
    }


    private void assertPartition(int size) throws NoSuchFieldException, IllegalAccessException {
        List<List<Integer>> daoImpl = splitTransactionIdList(original, size);
        List<List<Integer>> guava = Lists.partition(original, size);
        Assert.assertEquals(guava , daoImpl);
    }

    static <V> List<List<V>> splitTransactionIdList(List<V> transactionIdList, int maxTransactionIdListSize) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }

        List<List<V>> splitTransactionIdList = new ArrayList<>();

        int index = 0;
        int endIndex = transactionIdList.size();
        while (index < endIndex) {
            int subListEndIndex = Math.min(index + maxTransactionIdListSize, endIndex);
            splitTransactionIdList.add(transactionIdList.subList(index, subListEndIndex));
            index = subListEndIndex;
        }

        return splitTransactionIdList;
    }

}