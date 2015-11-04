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

package com.navercorp.pinpoint.profiler.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.profiler.context.TransactionCounter.SamplingType;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionCounterTest {

    private IdGenerator idGenerator;
    private TransactionCounter transactionCounter;

    @Before
    public void setUp() throws Exception {
        this.idGenerator = new IdGenerator();
        this.transactionCounter = new DefaultTransactionCounter(this.idGenerator);
    }

    @Test
    public void testNoTransaction_SAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_NEW);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testNoTransaction_SAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_CONTINUATION);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testNoTransaction_UNSAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_NEW);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testNoTransaction_UNSAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_CONTINUATION);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_SAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextTransactionId();
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_NEW);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_SAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextContinuedTransactionId();
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_CONTINUATION);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_UNSAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextDisabledId();
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_NEW);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_UNSAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextContinuedDisabledId();
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_CONTINUATION);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_SAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; ++i) {
            this.idGenerator.nextTransactionId();
        }
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_NEW);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_SAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; ++i) {
            this.idGenerator.nextContinuedTransactionId();
        }
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_CONTINUATION);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_UNSAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; ++i) {
            this.idGenerator.nextDisabledId();
        }
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_NEW);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_UNSAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; ++i) {
            this.idGenerator.nextContinuedDisabledId();
        }
        final long actualCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_CONTINUATION);
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testTotalTransaction() {
        // Given
        final long expectedSampledNewCount = 19L;
        final long expectedSampledContinuationCount = 29L;
        final long expectedUnsampledNewCount = 0L;
        final long expectedUnsampledContinuationCount = 9L;
        final long expectedTotalCount = expectedSampledNewCount + expectedSampledContinuationCount + expectedUnsampledNewCount + expectedUnsampledContinuationCount;
        // When
        for (int i = 0; i < expectedSampledNewCount; ++i) {
            this.idGenerator.nextTransactionId();
        }
        for (int i = 0; i < expectedSampledContinuationCount; ++i) {
            this.idGenerator.nextContinuedTransactionId();
        }
        for (int i = 0; i < expectedUnsampledNewCount; ++i) {
            this.idGenerator.nextDisabledId();
        }
        for (int i = 0; i < expectedUnsampledContinuationCount; ++i) {
            this.idGenerator.nextContinuedDisabledId();
        }
        final long actualSampledNewCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_NEW);
        final long actualSampledContinuationCount = this.transactionCounter.getTransactionCount(SamplingType.SAMPLED_CONTINUATION);
        final long actualUnsampledNewCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_NEW);
        final long actualUnsampledContinuationCount = this.transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_CONTINUATION);
        final long actualTotalCount = this.transactionCounter.getTotalTransactionCount();
        // Then
        assertEquals(expectedSampledNewCount, actualSampledNewCount);
        assertEquals(expectedSampledContinuationCount, actualSampledContinuationCount);
        assertEquals(expectedUnsampledNewCount, actualUnsampledNewCount);
        assertEquals(expectedUnsampledContinuationCount, actualUnsampledContinuationCount);
        assertEquals(expectedTotalCount, actualTotalCount);
    }

}
