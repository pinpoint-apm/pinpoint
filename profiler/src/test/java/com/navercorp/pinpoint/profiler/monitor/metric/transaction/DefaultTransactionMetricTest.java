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

import com.codahale.metrics.Gauge;
import com.navercorp.pinpoint.profiler.context.TestableTransactionCounter;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

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
        final long initialSampledNewCount = transactionMetric.sampledNew();
        final long initialSampledContinuationCount = transactionMetric.sampledContinuation();
        final long initialUnsampledNewCount = transactionMetric.unsampledNew();
        final long initialUnsampledContinuationCount = transactionMetric.unsampledContinuation();
        assertEquals(expectedInitialTransactionCount, initialSampledNewCount);
        assertEquals(expectedInitialTransactionCount, initialSampledContinuationCount);
        assertEquals(expectedInitialTransactionCount, initialUnsampledNewCount);
        assertEquals(expectedInitialTransactionCount, initialUnsampledContinuationCount);
    }

    @Test
    public void checkCalculationFor_0_Transaction() throws Exception {
        // Given
        final Long expectedNumberOfTransactions = 0L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactions);
        // Then
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledContinuation());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledContinuation());
    }

    @Test
    public void checkCalculationFor_1_Transaction() throws Exception {
        // Given
        final Long expectedNumberOfTransactions = 1L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactions);
        // Then
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledContinuation());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledContinuation());
    }

    @Test
    public void checkCalculationFor_100_Transaction() throws Exception {
        // Given
        final Long expectedNumberOfTransactions = 100L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactions);
        this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactions);
        // Then
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledContinuation());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledContinuation());
    }

    @Test
    public void negative_Transaction_should_return_0() throws Exception {
        // Given
        final Long expectedNumberOfTransactions = 0L;
        initTransactionMetric();
        // When
        this.transactionCounter.addSampledNewCount(-1000L);
        this.transactionCounter.addSampledContinuationCount(-1000L);
        this.transactionCounter.addUnSampledNewCount(-1000L);
        this.transactionCounter.addUnSampledContinuationCount(-1000L);
        // Then
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.sampledContinuation());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledNew());
        assertEquals(expectedNumberOfTransactions, transactionMetric.unsampledContinuation());
    }
    
    @Test
    public void checkContinuousTransactions() throws Exception {
        // Given
        final int testCnt = 10;
        final Long expectedNumberOfTransactionsPerCollection = 100L;
        initTransactionMetric();
        // When
        for (int i = 0; i < testCnt; ++i) {
            this.transactionCounter.addSampledNewCount(expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addSampledContinuationCount(expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addUnSampledNewCount(expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addUnSampledContinuationCount(expectedNumberOfTransactionsPerCollection);
            // Then
            assertEquals(expectedNumberOfTransactionsPerCollection, transactionMetric.sampledNew());
            assertEquals(expectedNumberOfTransactionsPerCollection, transactionMetric.sampledContinuation());
            assertEquals(expectedNumberOfTransactionsPerCollection, transactionMetric.unsampledNew());
            assertEquals(expectedNumberOfTransactionsPerCollection, transactionMetric.unsampledContinuation());
        }
    }

    private void initTransactionMetric() {
        transactionMetric.sampledNew();
        transactionMetric.sampledContinuation();
        transactionMetric.unsampledNew();
        transactionMetric.unsampledContinuation();
    }
}
