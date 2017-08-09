/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.transaction;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.profiler.context.TestableTransactionCounter;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionMetricTest {

    private TestableTransactionCounter transactionCounter;

    private TransactionMetric transactionMetric;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.transactionCounter = new TestableTransactionCounter();
        this.transactionMetric = new DefaultTransactionMetric(this.transactionCounter);
    }

    @Test
    public void initialTransactionCountsShouldBeZero() {
        final long expectedInitialTransactionCount = 0L;
        final TransactionMetricSnapshot snapshot = transactionMetric.getSnapshot();
        final long initialSampledNewCount = snapshot.getSampledNewCount();
        final long initialSampledContinuationCount = snapshot.getSampledContinuationCount();
        final long initialUnsampledNewCount = snapshot.getUnsampledNewCount();
        final long initialUnsampledContinuationCount = snapshot.getUnsampledContinuationCount();
        assertEquals(expectedInitialTransactionCount, initialSampledNewCount);
        assertEquals(expectedInitialTransactionCount, initialSampledContinuationCount);
        assertEquals(expectedInitialTransactionCount, initialUnsampledNewCount);
        assertEquals(expectedInitialTransactionCount, initialUnsampledContinuationCount);
    }

    @Test
    public void checkCalculationFor_0_Transaction() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 0L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactions);
        // Then
        final TransactionMetricSnapshot snapshot = transactionMetric.getSnapshot();
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledContinuationCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledContinuationCount());
    }

    @Test
    public void checkCalculationFor_1_Transaction() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 1L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactions);
        // Then
        final TransactionMetricSnapshot snapshot = transactionMetric.getSnapshot();
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledContinuationCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledContinuationCount());
    }

    @Test
    public void checkCalculationFor_100_Transaction() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 100L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactions);
        // Then
        final TransactionMetricSnapshot snapshot = transactionMetric.getSnapshot();
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledContinuationCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledContinuationCount());
    }

    @Test
    public void negative_Transaction_should_return_UNCOLLECTED() throws Exception {
        // Given
        final long expectedNumberOfTransactions = TransactionMetric.UNCOLLECTED;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(-1000L);
        this.transactionCounter.addSampledContinuationCount(-1000L);
        this.transactionCounter.addUnSampledNewCount(-1000L);
        this.transactionCounter.addUnSampledContinuationCount(-1000L);
        // Then
        final TransactionMetricSnapshot snapshot = transactionMetric.getSnapshot();
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getSampledContinuationCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledNewCount());
        assertEquals(expectedNumberOfTransactions, snapshot.getUnsampledContinuationCount());
    }
    
    @Test
    public void checkContinuousTransactions() throws Exception {
        // Given
        final int testCnt = 10;
        final long expectedNumberOfTransactionsPerCollection = 100L;
        initTransactionMetric();
        // When
        for (int i = 0; i < testCnt; ++i) {
            this.transactionCounter.addSampledNewCount(expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactionsPerCollection);
            // Then
            final TransactionMetricSnapshot snapshot = transactionMetric.getSnapshot();
            assertEquals(expectedNumberOfTransactionsPerCollection, snapshot.getSampledNewCount());
            assertEquals(expectedNumberOfTransactionsPerCollection, snapshot.getSampledContinuationCount());
            assertEquals(expectedNumberOfTransactionsPerCollection, snapshot.getUnsampledNewCount());
            assertEquals(expectedNumberOfTransactionsPerCollection, snapshot.getUnsampledContinuationCount());
        }
    }

    private void initTransactionMetric() {
        transactionMetric.getSnapshot();
    }
}
