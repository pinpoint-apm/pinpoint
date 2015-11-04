/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.tps.metric;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Gauge;
import com.navercorp.pinpoint.profiler.context.TestableTransactionCounter;
import com.navercorp.pinpoint.profiler.context.TransactionCounter.SamplingType;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

/**
 * @author HyunGil Jeong
 */
public class TransactionMetricSetTest {

    private TestableTransactionCounter transactionCounter;

    private Gauge<Long> sampledNewGauge;
    private Gauge<Long> sampledContinuationGauge;
    private Gauge<Long> unsampledNewGauge;
    private Gauge<Long> unsampledContinuationGuage;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.transactionCounter = new TestableTransactionCounter();
        TransactionMetricSet metricSet = new TransactionMetricSet(this.transactionCounter);
        this.sampledNewGauge = (Gauge<Long>) metricSet.getMetrics().get(MetricMonitorValues.TRANSACTION_SAMPLED_NEW);
        this.sampledContinuationGauge = (Gauge<Long>) metricSet.getMetrics().get(MetricMonitorValues.TRANSACTION_SAMPLED_CONTINUATION);
        this.unsampledNewGauge = (Gauge<Long>) metricSet.getMetrics().get(MetricMonitorValues.TRANSACTION_UNSAMPLED_NEW);
        this.unsampledContinuationGuage = (Gauge<Long>) metricSet.getMetrics().get(MetricMonitorValues.TRANSACTION_UNSAMPLED_CONTINUATION);
    }

    @Test
    public void initialTransactionCountsShouldBeZero() {
        final long expectedInitialTransactionCount = 0L;
        final long initialSampledNewCount = this.sampledNewGauge.getValue();
        final long initialSampledContinuationCount = this.sampledContinuationGauge.getValue();
        final long initialUnsampledNewCount = this.unsampledNewGauge.getValue();
        final long initialUnsampledContinuationCount = this.unsampledContinuationGuage.getValue();
        assertEquals(expectedInitialTransactionCount, initialSampledNewCount);
        assertEquals(expectedInitialTransactionCount, initialSampledContinuationCount);
        assertEquals(expectedInitialTransactionCount, initialUnsampledNewCount);
        assertEquals(expectedInitialTransactionCount, initialUnsampledContinuationCount);
    }

    @Test
    public void checkCalculationFor_0_Transaction() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 0L;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_CONTINUATION, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_NEW, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_CONTINUATION, expectedNumberOfTransactions);
        // Then
        assertEquals(expectedNumberOfTransactions, (long) this.sampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.sampledContinuationGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledContinuationGuage.getValue());
    }

    @Test
    public void checkCalculationFor_1_Transaction() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 1L;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_CONTINUATION, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_NEW, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_CONTINUATION, expectedNumberOfTransactions);
        // Then
        assertEquals(expectedNumberOfTransactions, (long) this.sampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.sampledContinuationGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledContinuationGuage.getValue());
    }

    @Test
    public void checkCalculationFor_100_Transaction() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 100L;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_CONTINUATION, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_NEW, expectedNumberOfTransactions);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_CONTINUATION, expectedNumberOfTransactions);
        // Then
        assertEquals(expectedNumberOfTransactions, (long) this.sampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.sampledContinuationGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledContinuationGuage.getValue());
    }

    @Test
    public void negative_Transaction_should_return_0() throws Exception {
        // Given
        final long expectedNumberOfTransactions = 0L;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, -1000L);
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_CONTINUATION, -1000L);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_NEW, -1000L);
        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_CONTINUATION, -1000L);
        // Then
        assertEquals(expectedNumberOfTransactions, (long) this.sampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.sampledContinuationGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledNewGauge.getValue());
        assertEquals(expectedNumberOfTransactions, (long) this.unsampledContinuationGuage.getValue());
    }
    
    @Test
    public void checkContinuousTransactions() throws Exception {
        // Given
        final int testCnt = 10;
        final long expectedNumberOfTransactionsPerCollection = 100L;
        // When
        initializeGauge();
        for (int i = 0; i < testCnt; ++i) {
            this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_CONTINUATION, expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_NEW, expectedNumberOfTransactionsPerCollection);
            this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_CONTINUATION, expectedNumberOfTransactionsPerCollection);
            // Then
            assertEquals(expectedNumberOfTransactionsPerCollection, (long) this.sampledNewGauge.getValue());
            assertEquals(expectedNumberOfTransactionsPerCollection, (long) this.sampledContinuationGauge.getValue());
            assertEquals(expectedNumberOfTransactionsPerCollection, (long) this.unsampledNewGauge.getValue());
            assertEquals(expectedNumberOfTransactionsPerCollection, (long) this.unsampledContinuationGuage.getValue());
        }
    }

    private void initializeGauge() {
        this.sampledNewGauge.getValue();
        this.sampledContinuationGauge.getValue();
        this.unsampledNewGauge.getValue();
        this.unsampledContinuationGuage.getValue();
    }

}
