/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.id;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionCounterTest {

    private AtomicIdGenerator idGenerator;
    private TransactionCounter transactionCounter;

    @BeforeEach
    public void setUp() throws Exception {
        this.idGenerator = new AtomicIdGenerator();
        this.transactionCounter = new DefaultTransactionCounter(this.idGenerator);
    }

    @Test
    public void testNoTransaction_SAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getSampledNewCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testNoTransaction_SAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getSampledContinuationCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testNoTransaction_UNSAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getUnSampledNewCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testNoTransaction_UNSAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 0L;
        // When
        final long actualCount = this.transactionCounter.getUnSampledContinuationCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_SAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextTransactionId();
        final long actualCount = this.transactionCounter.getSampledNewCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_SAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextContinuedTransactionId();
        final long actualCount = this.transactionCounter.getSampledContinuationCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_UNSAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextDisabledId();
        final long actualCount = this.transactionCounter.getUnSampledNewCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testSingleTransaction_UNSAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 1L;
        // When
        this.idGenerator.nextContinuedDisabledId();
        final long actualCount = this.transactionCounter.getUnSampledContinuationCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_SAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; i++) {
            this.idGenerator.nextTransactionId();
        }
        final long actualCount = this.transactionCounter.getSampledNewCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_SAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; i++) {
            this.idGenerator.nextContinuedTransactionId();
        }
        final long actualCount = this.transactionCounter.getSampledContinuationCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_UNSAMPLED_NEW() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; i++) {
            this.idGenerator.nextDisabledId();
        }
        final long actualCount = this.transactionCounter.getUnSampledNewCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_UNSAMPLED_CONTINUATION() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; i++) {
            this.idGenerator.nextContinuedDisabledId();
        }
        final long actualCount = this.transactionCounter.getUnSampledContinuationCount();
        // Then
        assertEquals(expectedTransactionCount, actualCount);
    }

    @Test
    public void testMultipleTransaction_SAMPLED_NEW_SKIP() {
        // Given
        final long expectedTransactionCount = 99L;
        // When
        for (int i = 0; i < expectedTransactionCount; i++) {
            this.idGenerator.nextSkippedId();
        }
        final long actualCount = this.transactionCounter.getSkippedNewCount();
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
        final long expectedSkippedNewCount = 0L;
        final long expectedSkippedContinuationCount = 9L;
        final long expectedTotalCount = expectedSampledNewCount + expectedSampledContinuationCount + expectedUnsampledNewCount + expectedUnsampledContinuationCount + expectedSkippedNewCount + expectedSkippedContinuationCount;
        // When
        for (int i = 0; i < expectedSampledNewCount; i++) {
            this.idGenerator.nextTransactionId();
        }
        for (int i = 0; i < expectedSampledContinuationCount; i++) {
            this.idGenerator.nextContinuedTransactionId();
        }
        for (int i = 0; i < expectedUnsampledNewCount; i++) {
            this.idGenerator.nextDisabledId();
        }
        for (int i = 0; i < expectedUnsampledContinuationCount; i++) {
            this.idGenerator.nextContinuedDisabledId();
        }
        for (int i = 0; i < expectedSkippedNewCount; i++) {
            this.idGenerator.nextSkippedId();
        }
        for (int i = 0; i < expectedSkippedContinuationCount; i++) {
            this.idGenerator.nextContinuedSkippedId();
        }
        final long actualSampledNewCount = this.transactionCounter.getSampledNewCount();
        final long actualSampledContinuationCount = this.transactionCounter.getSampledContinuationCount();
        final long actualUnsampledNewCount = this.transactionCounter.getUnSampledNewCount();
        final long actualUnsampledContinuationCount = this.transactionCounter.getUnSampledContinuationCount();
        final long actualSkippedNewCount = this.transactionCounter.getSkippedNewCount();
        final long actualSkippedContinuationCount = this.transactionCounter.getSkippedContinuationCount();
        final long actualTotalCount = this.transactionCounter.getTotalTransactionCount();
        // Then
        assertEquals(expectedSampledNewCount, actualSampledNewCount);
        assertEquals(expectedSampledContinuationCount, actualSampledContinuationCount);
        assertEquals(expectedUnsampledNewCount, actualUnsampledNewCount);
        assertEquals(expectedUnsampledContinuationCount, actualUnsampledContinuationCount);
        assertEquals(expectedSkippedNewCount, actualSkippedNewCount);
        assertEquals(expectedSkippedContinuationCount, actualSkippedContinuationCount);
        assertEquals(expectedTotalCount, actualTotalCount);
    }

}
