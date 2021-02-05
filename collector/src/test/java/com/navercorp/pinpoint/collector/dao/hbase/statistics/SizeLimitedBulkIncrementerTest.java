/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import com.navercorp.pinpoint.collector.dao.hbase.BulkOperationReporter;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementerTestClazz.Flusher;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementerTestClazz.Incrementer;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementerTestClazz.TestData;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementerTestClazz.TestDataSet;
import com.navercorp.pinpoint.collector.dao.hbase.statistics.BulkIncrementerTestClazz.TestVerifier;

import com.sematext.hbase.wd.RowKeyDistributorByHashPrefix;
import org.apache.commons.collections4.ListUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

;

/**
 * @author Taejin Koo
 */
@RunWith(MockitoJUnitRunner.class)
public class SizeLimitedBulkIncrementerTest {

    private static final byte[] CF = Bytes.toBytes("CF");

    private final int bulkLimitSize = 1000;

    private static final BulkIncrementerFactory bulkIncrementerFactory = new BulkIncrementerFactory();

    private final BulkOperationReporter reporter = new BulkOperationReporter();
    private final BulkIncrementer bulkIncrementer = bulkIncrementerFactory.wrap(
            new BulkIncrementer.DefaultBulkIncrementer(new RowKeyMerge(CF)), 1000, reporter);

    @AfterClass
    public static void afterClass() throws Exception {
        bulkIncrementerFactory.close();
    }

    @Mock
    private RowKeyDistributorByHashPrefix rowKeyDistributor;

    @Before
    public void setUp() {
        when(rowKeyDistributor.getDistributedKey(any(byte[].class))).then(invocation -> invocation.getArgument(0));
    }

    @Test
    public void singleTable() {
        // Given
        TableName tableA = TableName.valueOf("A");
        TestDataSet testDataSetA_0_0 = new TestDataSet(tableA, 0, 0, 100);
        TestDataSet testDataSetA_0_1 = new TestDataSet(tableA, 0, 1, 200);

        List<TestData> testDatas = new ArrayList<>();
        testDatas.addAll(testDataSetA_0_0.getTestDatas());
        testDatas.addAll(testDataSetA_0_1.getTestDatas());
        Collections.shuffle(testDatas);

        // When
        for (TestData testData : testDatas) {
            bulkIncrementer.increment(testData.getTableName(), testData.getRowKey(), testData.getColumnName());
        }

        // Then
        Map<TableName, List<Increment>> incrementMap = bulkIncrementer.getIncrements(rowKeyDistributor);
        TestVerifier verifier = new TestVerifier(incrementMap);
        verifier.verify(testDataSetA_0_0);
        verifier.verify(testDataSetA_0_1);
    }

    @Test
    public void multipleTables() {
        // Given
        TableName tableA = TableName.valueOf("a", "A");
        TableName tableB = TableName.valueOf("b", "A");
        TestDataSet testDataSetA_0_0 = new TestDataSet(tableA, 0, 0, 100);
        TestDataSet testDataSetA_0_1 = new TestDataSet(tableA, 0, 1, 200);
        TestDataSet testDataSetA_1_0 = new TestDataSet(tableA, 1, 0, 300);
        TestDataSet testDataSetA_1_1 = new TestDataSet(tableA, 1, 1, 400);
        TestDataSet testDataSetB_0_0 = new TestDataSet(tableB, 0, 0, 500);
        TestDataSet testDataSetB_0_1 = new TestDataSet(tableB, 0, 1, 600);
        TestDataSet testDataSetB_1_0 = new TestDataSet(tableB, 1, 0, 700);
        TestDataSet testDataSetB_1_1 = new TestDataSet(tableB, 1, 1, 800);

        List<TestData> testDatas = new ArrayList<>();
        testDatas.addAll(testDataSetA_0_0.getTestDatas());
        testDatas.addAll(testDataSetA_0_1.getTestDatas());
        testDatas.addAll(testDataSetA_1_0.getTestDatas());
        testDatas.addAll(testDataSetA_1_1.getTestDatas());
        testDatas.addAll(testDataSetB_0_0.getTestDatas());
        testDatas.addAll(testDataSetB_0_1.getTestDatas());
        testDatas.addAll(testDataSetB_1_0.getTestDatas());
        testDatas.addAll(testDataSetB_1_1.getTestDatas());
        Collections.shuffle(testDatas);

        // When
        for (TestData testData : testDatas) {
            bulkIncrementer.increment(testData.getTableName(), testData.getRowKey(), testData.getColumnName());
        }

        // Then
        Map<TableName, List<Increment>> incrementMap = bulkIncrementer.getIncrements(rowKeyDistributor);

        TestVerifier verifier = new TestVerifier(incrementMap);
        verifier.verify(testDataSetA_0_0);
        verifier.verify(testDataSetA_0_1);
        verifier.verify(testDataSetA_1_0);
        verifier.verify(testDataSetA_1_1);
        verifier.verify(testDataSetB_0_0);
        verifier.verify(testDataSetB_0_1);
        verifier.verify(testDataSetB_1_0);
        verifier.verify(testDataSetB_1_1);
    }

    @Test
    public void singleTableConcurrent() throws Exception {
        // Given
        TableName tableA = TableName.valueOf("A");
        TestDataSet testDataSetA_0_0 = new TestDataSet(tableA, 0, 0, 1000000);
        TestDataSet testDataSetA_0_1 = new TestDataSet(tableA, 0, 1, 1000001);

        List<TestData> testDatas = new ArrayList<>();
        testDatas.addAll(testDataSetA_0_0.getTestDatas());
        testDatas.addAll(testDataSetA_0_1.getTestDatas());
        Collections.shuffle(testDatas);

        // When
        final int numIncrementers = 16;
        List<List<BulkIncrementerTestClazz.TestData>> testDataPartitions = ListUtils.partition(testDatas, testDatas.size() / (numIncrementers - 1));
        final CountDownLatch completeLatch = new CountDownLatch(testDataPartitions.size());
        final CountDownLatch flusherLatch = new CountDownLatch(1);

        FutureTask<Map<TableName, List<Increment>>> flushTask = new FutureTask<>(new Flusher(bulkIncrementer, rowKeyDistributor, completeLatch, flusherLatch));
        new Thread(flushTask, "Flusher").start();

        int counter = 0;
        for (List<TestData> testDataPartition : testDataPartitions) {
            Incrementer incrementer = new Incrementer(bulkIncrementer, completeLatch, testDataPartition);
            new Thread(incrementer, "Incrementer-" + counter++).start();
        }

        flusherLatch.await(30L, TimeUnit.SECONDS);

        // Then
        Map<TableName, List<Increment>> incrementMap = flushTask.get(5L, TimeUnit.SECONDS);
        TestVerifier verifier = new TestVerifier(incrementMap);
        verifier.verify(testDataSetA_0_0);
        verifier.verify(testDataSetA_0_1);
    }

    @Test
    public void multipleTablesConcurrent() throws Exception {
        // Given
        final int numTables = 50;
        final int numRowIds = 100;
        final int numColumnIds = 20;
        final int maxCallCount = 200;

        List<TestDataSet> testDataSets = BulkIncrementerTestClazz.createRandomTestDataSetList(numTables, numRowIds, numColumnIds, maxCallCount);
        List<TableName> tableNames = new ArrayList<>(numTables);
        for (int i = 0; i < numTables; i++) {
            tableNames.add(TableName.valueOf(i + ""));
        }

        final int maxNumTestDatas = numTables * numRowIds * numColumnIds * maxCallCount;
        List<TestData> testDatas = new ArrayList<>(maxNumTestDatas);
        for (TestDataSet testDataSet : testDataSets) {
            testDatas.addAll(testDataSet.getTestDatas());
        }
        Collections.shuffle(testDatas);

        // When
        final int numIncrementers = 16;
        List<List<TestData>> testDataPartitions = ListUtils.partition(testDatas, testDatas.size() / (numIncrementers - 1));
        final CountDownLatch incrementorLatch = new CountDownLatch(testDataPartitions.size());
        final CountDownLatch flusherLatch = new CountDownLatch(1);

        FutureTask<Map<TableName, List<Increment>>> flushTask = new FutureTask<>(new Flusher(bulkIncrementer, rowKeyDistributor, incrementorLatch, flusherLatch));
        new Thread(flushTask, "Flusher").start();

        int counter = 0;
        for (List<TestData> testDataPartition : testDataPartitions) {
            Incrementer incrementer = new Incrementer(bulkIncrementer, incrementorLatch, testDataPartition);
            new Thread(incrementer, "Incrementer-" + counter++).start();
        }

        flusherLatch.await(30L, TimeUnit.SECONDS);

        // Then
        Map<TableName, List<Increment>> incrementMap = flushTask.get(5L, TimeUnit.SECONDS);
        TestVerifier verifier = new TestVerifier(incrementMap);
        long actualTotalCount = 0;
        for (TestDataSet testDataSet : testDataSets) {
            TableName expectedTableName = testDataSet.getTableName();
            RowKey expectedRowKey = testDataSet.getRowKey();
            ColumnName expectedColumnName = testDataSet.getColumnName();

            Map<TableName, Map<ByteBuffer, Map<ByteBuffer, Long>>> resultMap = verifier.getResultMap();
            Map<ByteBuffer, Map<ByteBuffer, Long>> rows = resultMap.get(expectedTableName);
            if (rows == null) {
                continue;
            }
            Map<ByteBuffer, Long> keyValues = rows.get(ByteBuffer.wrap(expectedRowKey.getRowKey()));
            if (keyValues == null) {
                continue;
            }
            Long actualCount = keyValues.get(ByteBuffer.wrap(expectedColumnName.getColumnName()));
            if (actualCount == null) {
                continue;
            }
            actualTotalCount += actualCount;
        }

        Assert.assertTrue(actualTotalCount > bulkLimitSize);
    }


}
