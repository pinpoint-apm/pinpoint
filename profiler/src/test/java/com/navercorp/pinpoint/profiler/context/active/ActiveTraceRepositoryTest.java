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

package com.navercorp.pinpoint.profiler.context.active;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionCounter;
import com.navercorp.pinpoint.profiler.context.MockTraceContextFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;
import org.mockito.Mockito;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceRepositoryTest {

    private static final int SAMPLING_RATE = 3;

    private TraceContext traceContext;
    private TransactionCounter transactionCounter;
    private ActiveTraceRepository activeTraceRepository;

    @Before
    public void setUp() {


        final ProfilerConfig profilerConfig = Mockito.mock(ProfilerConfig.class);
        Mockito.when(profilerConfig.isTraceAgentActiveThread()).thenReturn(true);

        Mockito.when(profilerConfig.isSamplingEnable()).thenReturn(true);
        Mockito.when(profilerConfig.getSamplingRate()).thenReturn(SAMPLING_RATE);

        MockTraceContextFactory mockTraceContextFactory = MockTraceContextFactory.newTestTraceContextFactory(profilerConfig);

        this.traceContext = mockTraceContextFactory.getTraceContext();
        this.transactionCounter = new DefaultTransactionCounter(mockTraceContextFactory.getIdGenerator());
        this.activeTraceRepository = mockTraceContextFactory.getActiveTraceRepository();
    }

    @Test
    public void verifyActiveTraceCollectionAndTransactionCount() throws Exception {
        // Given
        final int newTransactionCount = 50;
        @SuppressWarnings("unused")
        final int expectedSampledNewCount = newTransactionCount / SAMPLING_RATE + (newTransactionCount % SAMPLING_RATE > 0 ? 1 : 0);
        final int expectedUnsampledNewCount = newTransactionCount - expectedSampledNewCount;
        final int expectedSampledContinuationCount = 20;
        final int expectedUnsampledContinuationCount = 30;
        final int expectedTotalTransactionCount = expectedSampledNewCount + expectedUnsampledNewCount + expectedSampledContinuationCount + expectedUnsampledContinuationCount;

        final CountDownLatch awaitLatch = new CountDownLatch(1);
        final CountDownLatch executeLatch = new CountDownLatch(expectedTotalTransactionCount);

        // When
        ListenableFuture<List<TraceThreadTuple>> futures = executeTransactions(awaitLatch, executeLatch, newTransactionCount, expectedSampledContinuationCount, expectedUnsampledContinuationCount);
        executeLatch.await();
        List<ActiveTraceSnapshot> activeTraceInfos = this.activeTraceRepository.collect();
        awaitLatch.countDown();
        List<TraceThreadTuple> executedTraces = futures.get();
        Map<Long, TraceThreadTuple> executedTraceMap = new HashMap<Long, TraceThreadTuple>(executedTraces.size());
        for (TraceThreadTuple tuple : executedTraces) {
            executedTraceMap.put(tuple.id, tuple);
        }

        // Then
        assertEquals(expectedSampledNewCount, transactionCounter.getSampledNewCount());
        assertEquals(expectedUnsampledNewCount, transactionCounter.getUnSampledNewCount());
        assertEquals(expectedSampledContinuationCount, transactionCounter.getSampledContinuationCount());
        assertEquals(expectedUnsampledContinuationCount, transactionCounter.getUnSampledContinuationCount());
        assertEquals(expectedTotalTransactionCount, transactionCounter.getTotalTransactionCount());
        
        for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfos) {
            TraceThreadTuple executedTrace = executedTraceMap.get(activeTraceInfo.getLocalTransactionId());
            assertEquals(executedTrace.getId(), activeTraceInfo.getLocalTransactionId());
            assertEquals(executedTrace.getStartTime(), activeTraceInfo.getStartTime());
            assertEquals(executedTrace.getThreadId(), activeTraceInfo.getThreadId());
        }
    }

    private ListenableFuture<List<TraceThreadTuple>> executeTransactions(CountDownLatch awaitLatch, CountDownLatch executeLatch, int newTransactionCount, int sampledContinuationCount, int unsampledContinuationCount) {
        final int totalTransactionCount = newTransactionCount + sampledContinuationCount + unsampledContinuationCount;
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(totalTransactionCount));
        final List<ListenableFuture<TraceThreadTuple>> futures = new ArrayList<ListenableFuture<TraceThreadTuple>>();
        for (int i = 0; i < newTransactionCount; ++i) {
            futures.add(executeNewTrace(executor, awaitLatch, executeLatch));
        }
        for (int i = 0; i < sampledContinuationCount; ++i) {
            futures.add(executeSampledContinuedTrace(executor, awaitLatch, executeLatch, i));
        }
        for (int i = 0; i < unsampledContinuationCount; ++i) {
            futures.add(executeUnsampledContinuedTrace(executor, awaitLatch, executeLatch));
        }
        return Futures.allAsList(futures);
    }

    private ListenableFuture<TraceThreadTuple> executeNewTrace(ListeningExecutorService executorService, final CountDownLatch awaitLatch, final CountDownLatch executeLatch) {
        return executorService.submit(new Callable<TraceThreadTuple>() {
            @Override
            public TraceThreadTuple call() throws Exception {
                try {
                    long id = Thread.currentThread().getId();
                    return new TraceThreadTuple(traceContext.newTraceObject(), id);
                } finally {
                    executeLatch.countDown();
                    awaitLatch.await();
                    traceContext.removeTraceObject();
                }
            }
        });
    }

    private ListenableFuture<TraceThreadTuple> executeSampledContinuedTrace(ListeningExecutorService executorService, final CountDownLatch awaitLatch, final CountDownLatch executeLatch, final long id) {
        return executorService.submit(new Callable<TraceThreadTuple>() {
            @Override
            public TraceThreadTuple call() throws Exception {
                try {
                    TraceId agentId1 = new DefaultTraceId("agentId", 0L, id);
                    Trace agentId = traceContext.continueTraceObject(agentId1);
                    return new TraceThreadTuple(agentId, Thread.currentThread().getId());
                } finally {
                    executeLatch.countDown();
                    awaitLatch.await();
                    traceContext.removeTraceObject();
                }
            }
        });
    }

    private ListenableFuture<TraceThreadTuple> executeUnsampledContinuedTrace(ListeningExecutorService executorService, final CountDownLatch awaitLatch, final CountDownLatch executeLatch) {
        return executorService.submit(new Callable<TraceThreadTuple>() {
            @Override
            public TraceThreadTuple call() throws Exception {
                try {
                    long id = Thread.currentThread().getId();
                    return new TraceThreadTuple(traceContext.disableSampling(), id);
                } finally {
                    executeLatch.countDown();
                    awaitLatch.await();
                    traceContext.removeTraceObject();
                }
            }
        });
    }

    private static class TraceThreadTuple {
        private final long id;
        private final long startTime;
        private final long threadId;

        private TraceThreadTuple(Trace trace, long threadId) {
            if (trace == null) {
                throw new NullPointerException("trace must not be null");
            }
            this.id = trace.getId();
            this.startTime = trace.getStartTime();
            this.threadId = threadId;
        }

        public long getId() {
            return id;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getThreadId() {
            return threadId;
        }
    }

}
