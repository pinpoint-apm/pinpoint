/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.TableName;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public class BulkCounterTest {

    private final TableNameProvider tableNameProvider = new TableNameProvider() {
        @Override
        public TableName getTableName(String tableName) {
            return TableName.valueOf(tableName);
        }
    };

    @Test
    public void singleTable() {
        // Given
        final BulkCounter bulkCounter = new BulkCounter();

        TableName tableA = tableNameProvider.getTableName("A");
        RowInfo rowInfo0_0 = createRowInfo(0, 0);
        RowInfo rowInfo0_1 = createRowInfo(0, 1);

        List<TestData> testDatas = new ArrayList<>();
        testDatas.addAll(new TestDataSet(tableA, rowInfo0_0, 100).getTestDatas());
        testDatas.addAll(new TestDataSet(tableA, rowInfo0_1, 200).getTestDatas());
        Collections.shuffle(testDatas);

        // When
        for (TestData testData : testDatas) {
            bulkCounter.increment(testData.getTableName(), testData.getRowInfo());
        }

        // Then
        Map<TableName, Map<RowInfo, Long>> countMap = bulkCounter.getAndReset();
        Map<RowInfo, Long> tableACountMap = countMap.get(tableA);
        Assert.assertEquals(100, (long) tableACountMap.get(rowInfo0_0));
        Assert.assertEquals(200, (long) tableACountMap.get(rowInfo0_1));
    }

    @Test
    public void multipleTables() {
        // Given
        final BulkCounter bulkCounter = new BulkCounter();

        TableName tableA = tableNameProvider.getTableName("A");
        TableName tableB = tableNameProvider.getTableName("B");
        RowInfo rowInfo0_0 = createRowInfo(0, 0);
        RowInfo rowInfo0_1 = createRowInfo(0, 1);
        RowInfo rowInfo1_0 = createRowInfo(1, 0);
        RowInfo rowInfo1_1 = createRowInfo(1, 1);

        List<TestData> testDatas = new ArrayList<>();
        testDatas.addAll(new TestDataSet(tableA, rowInfo0_0, 100).getTestDatas());
        testDatas.addAll(new TestDataSet(tableA, rowInfo0_1, 200).getTestDatas());
        testDatas.addAll(new TestDataSet(tableA, rowInfo1_0, 300).getTestDatas());
        testDatas.addAll(new TestDataSet(tableA, rowInfo1_1, 400).getTestDatas());
        testDatas.addAll(new TestDataSet(tableB, rowInfo0_0, 500).getTestDatas());
        testDatas.addAll(new TestDataSet(tableB, rowInfo0_1, 600).getTestDatas());
        testDatas.addAll(new TestDataSet(tableB, rowInfo1_0, 700).getTestDatas());
        testDatas.addAll(new TestDataSet(tableB, rowInfo1_1, 800).getTestDatas());
        Collections.shuffle(testDatas);

        // When
        for (TestData testData : testDatas) {
            bulkCounter.increment(testData.getTableName(), testData.getRowInfo());
        }

        // Then
        Map<TableName, Map<RowInfo, Long>> countMap = bulkCounter.getAndReset();
        Map<RowInfo, Long> tableACountMap = countMap.get(tableA);
        Assert.assertEquals(100, (long) tableACountMap.get(rowInfo0_0));
        Assert.assertEquals(200, (long) tableACountMap.get(rowInfo0_1));
        Assert.assertEquals(300, (long) tableACountMap.get(rowInfo1_0));
        Assert.assertEquals(400, (long) tableACountMap.get(rowInfo1_1));

        Map<RowInfo, Long> tableBCountMap = countMap.get(tableB);
        Assert.assertEquals(500, (long) tableBCountMap.get(rowInfo0_0));
        Assert.assertEquals(600, (long) tableBCountMap.get(rowInfo0_1));
        Assert.assertEquals(700, (long) tableBCountMap.get(rowInfo1_0));
        Assert.assertEquals(800, (long) tableBCountMap.get(rowInfo1_1));
    }

    @Test
    public void singleTableConcurrent() throws Exception {
        // Given
        final BulkCounter bulkCounter = new BulkCounter();

        TableName tableA = tableNameProvider.getTableName("A");
        RowInfo rowInfo0_0 = createRowInfo(0, 0);
        final long rowInfo0_0_callCount = 1000000;
        RowInfo rowInfo0_1 = createRowInfo(0, 1);
        final long rowInfo0_1_callCount = 1000001;

        List<TestData> testDatas = new ArrayList<>();
        testDatas.addAll(new TestDataSet(tableA, rowInfo0_0, rowInfo0_0_callCount).getTestDatas());
        testDatas.addAll(new TestDataSet(tableA, rowInfo0_1, rowInfo0_1_callCount).getTestDatas());
        Collections.shuffle(testDatas);

        // When
        final int numIncrementers = 16;
        List<List<TestData>> testDataPartitions = Lists.partition(testDatas, testDatas.size() / (numIncrementers - 1));
        final CountDownLatch completeLatch = new CountDownLatch(testDataPartitions.size());
        final CountDownLatch flusherLatch = new CountDownLatch(1);

        FutureTask<Map<TableName, Map<RowInfo, Long>>> flushTask = new FutureTask<>(new Flusher(completeLatch, flusherLatch, bulkCounter));
        new Thread(flushTask, "Flusher").start();

        int counter = 0;
        for (List<TestData> testDataPartition : testDataPartitions) {
            Incrementer incrementer = new Incrementer(completeLatch, bulkCounter, testDataPartition);
            new Thread(incrementer, "Incrementer-" + counter++).start();
        }

        flusherLatch.await(30L, TimeUnit.SECONDS);

        // Then
        Map<TableName, Map<RowInfo, Long>> resultMap = flushTask.get(5L, TimeUnit.SECONDS);
        Map<RowInfo, Long> tableACountMap = resultMap.get(tableA);
        Assert.assertEquals(rowInfo0_0_callCount, (long) tableACountMap.get(rowInfo0_0));
        Assert.assertEquals(rowInfo0_1_callCount, (long) tableACountMap.get(rowInfo0_1));
    }

    @Test
    public void multipleTablesConcurrent() throws Exception {
        // Given
        final BulkCounter bulkCounter = new BulkCounter();

        final int numTables = 50;
        List<TableName> tableNames = new ArrayList<>(numTables);
        for (int i = 0; i < numTables; i++) {
            tableNames.add(tableNameProvider.getTableName(i + ""));
        }
        final int numRowIds = 100;
        final int numColumnIds = 20;
        final int numRowInfos = numRowIds * numColumnIds;
        List<RowInfo> rowInfos = new ArrayList<>(numRowInfos);
        for (int i = 0; i < numRowIds; i++) {
            for (int j = 0; j < numColumnIds; j++) {
                rowInfos.add(createRowInfo(i, j));
            }
        }

        final int callCount = 200;
        final int numTestDatas = numTables * numRowInfos * callCount;
        final List<TestData> testDatas = new ArrayList<>(numTestDatas);
        for (TableName tableName : tableNames) {
            for (RowInfo rowInfo : rowInfos) {
                testDatas.addAll(new TestDataSet(tableName, rowInfo, callCount).getTestDatas());
            }
        }
        Collections.shuffle(testDatas);

        // When
        final int numIncrementers = 16;
        List<List<TestData>> testDataPartitions = Lists.partition(testDatas, testDatas.size() / (numIncrementers - 1));
        final CountDownLatch incrementorLatch = new CountDownLatch(testDataPartitions.size());
        final CountDownLatch flusherLatch = new CountDownLatch(1);

        FutureTask<Map<TableName, Map<RowInfo, Long>>> flushTask = new FutureTask<>(new Flusher(incrementorLatch, flusherLatch, bulkCounter));
        new Thread(flushTask, "Flusher").start();

        int counter = 0;
        for (List<TestData> testDataPartition : testDataPartitions) {
            Incrementer incrementer = new Incrementer(incrementorLatch, bulkCounter, testDataPartition);
            new Thread(incrementer, "Incrementer-" + counter++).start();
        }

        flusherLatch.await(30L, TimeUnit.SECONDS);

        // Then
        Map<TableName, Map<RowInfo, Long>> resultMap = flushTask.get(5L, TimeUnit.SECONDS);
        Assert.assertEquals(numTables, resultMap.keySet().size());
        for (Map<RowInfo, Long> rowCounters : resultMap.values()) {
            Assert.assertEquals(numRowInfos, rowCounters.keySet().size());
            for (long rowCount : rowCounters.values()) {
                Assert.assertEquals(callCount, rowCount);
            }
        }
    }

    private static class Incrementer implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final CountDownLatch completeLatch;
        private final BulkCounter bulkCounter;
        private final List<TestData> testDatas;

        private Incrementer(CountDownLatch completeLatch, BulkCounter bulkCounter, List<TestData> testDatas) {
            this.completeLatch = completeLatch;
            this.bulkCounter = bulkCounter;
            this.testDatas = testDatas;
        }

        @Override
        public void run() {
            for (TestData testData : testDatas) {
                bulkCounter.increment(testData.getTableName(), testData.getRowInfo());
            }
            logger.debug("[{}] finished", Thread.currentThread().getName());
            completeLatch.countDown();
        }
    }

    private static class Flusher implements Callable<Map<TableName, Map<RowInfo, Long>>> {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final CountDownLatch awaitLatch;
        private final CountDownLatch completeLatch;
        private final BulkCounter bulkCounter;

        private Flusher(CountDownLatch awaitLatch, CountDownLatch completeLatch, BulkCounter bulkCounter) {
            this.awaitLatch = awaitLatch;
            this.completeLatch = completeLatch;
            this.bulkCounter = bulkCounter;
        }

        private void flushToMap(Map<TableName, Map<RowInfo, Long>> resultMap) {
            for (Map.Entry<TableName, Map<RowInfo, Long>> counterMapEntry : bulkCounter.getAndReset().entrySet()) {
                TableName tableName = counterMapEntry.getKey();
                Map<RowInfo, Long> counters = counterMapEntry.getValue();

                Map<RowInfo, Long> mergedRowCounts = resultMap.computeIfAbsent(tableName, tn -> new HashMap<>());
                for (Map.Entry<RowInfo, Long> counterEntry : counters.entrySet()) {
                    RowInfo rowInfo = counterEntry.getKey();
                    long rowCount = counterEntry.getValue();
                    long previousMergedRowCount = mergedRowCounts.getOrDefault(rowInfo, 0L);
                    long mergedRowCount = previousMergedRowCount + rowCount;
                    mergedRowCounts.put(rowInfo, mergedRowCount);
                }
            }
            logger.debug("[{}] flushed", Thread.currentThread().getName());
        }

        @Override
        public Map<TableName, Map<RowInfo, Long>> call() {
            Map<TableName, Map<RowInfo, Long>> resultMap = new HashMap<>();
            try {
                do {
                    flushToMap(resultMap);
                } while (!awaitLatch.await(10L, TimeUnit.MILLISECONDS));
                flushToMap(resultMap);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Collections.emptyMap();
            } finally {
                completeLatch.countDown();
            }
            logger.debug("[{}] completed", Thread.currentThread().getName());
            return resultMap;
        }
    }

    private static RowInfo createRowInfo(int rowId, int columnId) {
        final RowKey rowKey = new TestRowKey(rowId);
        final ColumnName columnName = new TestColumnName(columnId);
        return new RowInfo() {
            @Override
            public RowKey getRowKey() {
                return rowKey;
            }

            @Override
            public ColumnName getColumnName() {
                return columnName;
            }

            @Override
            public String toString() {
                return rowKey + "(" + columnName + ")";
            }
        };
    }

    private static class TestDataSet {

        private final Set<TestData> testDatas;

        private TestDataSet(TableName tableName, RowInfo rowInfo, long count) {
            testDatas = new HashSet<>();
            for (int i = 0; i < count; i++) {
                TestData testData = new TestData(tableName, rowInfo);
                testDatas.add(testData);
            }
        }

        public Set<TestData> getTestDatas() {
            return testDatas;
        }
    }

    private static class TestData {

        private final TableName tableName;
        private final RowInfo rowInfo;

        private TestData(TableName tableName, RowInfo rowInfo) {
            this.tableName = tableName;
            this.rowInfo = rowInfo;
        }

        public TableName getTableName() {
            return tableName;
        }

        public RowInfo getRowInfo() {
            return rowInfo;
        }
    }

    private static class TestRowKey implements RowKey {

        private final int id;

        private TestRowKey(int id) {
            this.id = id;
        }

        @Override
        public byte[] getRowKey() {
            return BytesUtils.intToVar32(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestRowKey that = (TestRowKey) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private static class TestColumnName implements ColumnName {

        private final int id;

        private TestColumnName(int id) {
            this.id = id;
        }

        @Override
        public byte[] getColumnName() {
            return BytesUtils.intToVar32(id);
        }

        @Override
        public long getCallCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCallCount(long callCount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestColumnName that = (TestColumnName) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

}
