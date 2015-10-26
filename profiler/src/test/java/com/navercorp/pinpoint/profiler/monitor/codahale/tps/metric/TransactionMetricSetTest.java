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

    private static final int ACCEPTABLE_DIFF_PERCENTAGE = 1;

    private TestableTransactionCounter transactionCounter;
    private Gauge<Integer> tpsGauge;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        this.transactionCounter = new TestableTransactionCounter();
        TransactionMetricSet metricSet = new TransactionMetricSet(this.transactionCounter);
        this.tpsGauge = (Gauge<Integer>)metricSet.getMetrics().get(MetricMonitorValues.TRANSACTION_PER_SECOND);
    }

    @Test
    public void initialTpsShouldBeZero() {
        int initialTps = this.tpsGauge.getValue();
        assertEquals(0, initialTps);
    }

    @Test
    public void checkCalculationFor_0_Tps() throws Exception {
        // Given
        final int expectedTps = 0;
        final int expectedExecutionTimeInSeconds = 1;
        final int expectedNumberOfTransactions = expectedTps * expectedExecutionTimeInSeconds;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        Thread.sleep(expectedExecutionTimeInSeconds * 1000);
        final int actualTps = this.tpsGauge.getValue();
        // Then
        assertApproximatelyEquals(expectedTps, actualTps);
    }

    @Test
    public void checkCalculationFor_1_Tps() throws Exception {
        // Given
        final int expectedTps = 1;
        final int expectedExecutionTimeInSeconds = 1;
        final int expectedNumberOfTransactions = expectedTps * expectedExecutionTimeInSeconds;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        Thread.sleep(expectedExecutionTimeInSeconds * 1000);
        final int actualTps = this.tpsGauge.getValue();
        // Then
        assertApproximatelyEquals(expectedTps, actualTps);
    }

    @Test
    public void checkCalculationFor_100_Tps() throws Exception {
        // Given
        final int expectedTps = 100;
        final int expectedExecutionTimeInSeconds = 1;
        final int expectedNumberOfTransactions = expectedTps * expectedExecutionTimeInSeconds;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        Thread.sleep(expectedExecutionTimeInSeconds * 1000);
        final int actualTps = this.tpsGauge.getValue();
        // Then
        assertApproximatelyEquals(expectedTps, actualTps);
    }

    @Test
    public void checkCalculationFor_1000_Tps() throws Exception {
        // Given
        final int expectedTps = 1000;
        final int expectedExecutionTimeInSeconds = 2;
        final int expectedNumberOfTransactions = expectedTps * expectedExecutionTimeInSeconds;
        // When
        initializeGauge();
        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedNumberOfTransactions);
        Thread.sleep(expectedExecutionTimeInSeconds * 1000);
        final int actualTps = this.tpsGauge.getValue();
        // Then
        assertApproximatelyEquals(expectedTps, actualTps);
    }

    @Test
    public void checkContinuousTpsCalculation() throws Exception {
        // Given
        final long oneSecond = 1 * 1000L;
        final int expectedTpsForFirstSecond = 1;
        final int expectedTpsForSecondSecond = 1000;
        final int expectedTpsForThirdSecond = 500;
        final int expectedTpsForFourthSecond = 0;
        final int expectedTpsForFifthSecond = 999;
        // When
        initializeGauge();

        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedTpsForFirstSecond);
        Thread.sleep(oneSecond);
        final int actualTpsForFirstSecond = this.tpsGauge.getValue();

        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_CONTINUATION, expectedTpsForSecondSecond);
        Thread.sleep(oneSecond);
        final int actualTpsForSecondSecond = this.tpsGauge.getValue();

        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_NEW, expectedTpsForThirdSecond);
        Thread.sleep(oneSecond);
        final int actualTpsForThirdSecond = this.tpsGauge.getValue();

        this.transactionCounter.addTransactionCount(SamplingType.UNSAMPLED_CONTINUATION, expectedTpsForFourthSecond);
        Thread.sleep(oneSecond);
        final int actualTpsForFourthSecond = this.tpsGauge.getValue();

        this.transactionCounter.addTransactionCount(SamplingType.SAMPLED_NEW, expectedTpsForFifthSecond);
        Thread.sleep(oneSecond);
        final int actualTpsForFifthSecond = this.tpsGauge.getValue();
        // Then
        assertApproximatelyEquals(expectedTpsForFirstSecond, actualTpsForFirstSecond);
        assertApproximatelyEquals(expectedTpsForSecondSecond, actualTpsForSecondSecond);
        assertApproximatelyEquals(expectedTpsForThirdSecond, actualTpsForThirdSecond);
        assertApproximatelyEquals(expectedTpsForFourthSecond, actualTpsForFourthSecond);
        assertApproximatelyEquals(expectedTpsForFifthSecond, actualTpsForFifthSecond);
    }

    private void initializeGauge() {
        this.tpsGauge.getValue();
    }

    private void assertApproximatelyEquals(int expected, int actual) {
        int lowerBound = (expected * 100) - (expected * ACCEPTABLE_DIFF_PERCENTAGE);
        int upperBound = (expected * 100) + (expected * ACCEPTABLE_DIFF_PERCENTAGE);
        int actualValueForComparison = actual * 100;
        assertTrue("expected:[" + expected + "], actual:[" + actual + "]", lowerBound <= actualValueForComparison
                && actualValueForComparison <= upperBound);
    }

}
