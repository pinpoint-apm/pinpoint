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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionCounter;
import com.navercorp.pinpoint.profiler.context.MockTraceContextFactory;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import org.junit.After;
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

    private DefaultApplicationContext applicationContext;

    @Before
    public void setUp() {

        ProfilerConfig profilerConfig = Mockito.spy(new DefaultProfilerConfig());
        Mockito.when(profilerConfig.isTraceAgentActiveThread()).thenReturn(true);

        Mockito.when(profilerConfig.isSamplingEnable()).thenReturn(true);
        Mockito.when(profilerConfig.getSamplingRate()).thenReturn(SAMPLING_RATE);

        this.applicationContext = MockTraceContextFactory.newMockApplicationContext(profilerConfig);
        applicationContext.start();

        this.traceContext = applicationContext.getTraceContext();
        this.transactionCounter = new DefaultTransactionCounter(applicationContext.getInjector().getInstance(IdGenerator.class));
        this.activeTraceRepository = applicationContext.getInjector().getInstance(ActiveTraceRepository.class);
    }

    @After
    public void tearDown() throws Exception {
        if (applicationContext != null) {
            applicationContext.close();
        }
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
        ExecutorService executorService = Executors.newFixedThreadPool(expectedTotalTransactionCount);
        ListenableFuture<List<TraceThreadTuple>> futures = executeTransactions(
                executorService, awaitLatch, executeLatch,
                expectedSampledNewCount, expectedUnsampledNewCount, expectedSampledContinuationCount, expectedUnsampledContinuationCount);
        executeLatch.await(5, TimeUnit.SECONDS);
        List<ActiveTraceSnapshot> activeTraceInfos = this.activeTraceRepository.snapshot();
        awaitLatch.countDown();
        List<TraceThreadTuple> executedTraces = futures.get(5, TimeUnit.SECONDS);
        Map<Long, TraceThreadTuple> executedTraceMap = new HashMap<Long, TraceThreadTuple>(executedTraces.size());
        for (TraceThreadTuple tuple : executedTraces) {
            executedTraceMap.put(tuple.id, tuple);
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
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

    private ListenableFuture<List<TraceThreadTuple>> executeTransactions(
            ExecutorService executorService, CountDownLatch awaitLatch, CountDownLatch executeLatch,
            int sampledNewCount, int unsampledNewCount, int sampledContinuationCount, int unsampledContinuationCount) {

        int totalNewCount = sampledNewCount + unsampledNewCount;
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(executorService);
        final List<ListenableFuture<TraceThreadTuple>> futures = new ArrayList<ListenableFuture<TraceThreadTuple>>();
        for (int i = 0; i < totalNewCount; i++) {
            futures.add(executeNewTrace(executor, awaitLatch, executeLatch));
        }
        for (int i = 0; i < sampledContinuationCount; i++) {
            futures.add(executeSampledContinuedTrace(executor, awaitLatch, executeLatch, i));
        }
        for (int i = 0; i < unsampledContinuationCount; i++) {
            futures.add(executeUnsampledContinuedTrace(executor, awaitLatch, executeLatch));
        }
        return Futures.allAsList(futures);
    }

    private ListenableFuture<TraceThreadTuple> executeNewTrace(ListeningExecutorService executorService, final CountDownLatch awaitLatch, final CountDownLatch executeLatch) {
        Callable<TraceThreadTuple> task = new Callable<TraceThreadTuple>() {
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
        };
        return (ListenableFuture<TraceThreadTuple>) executorService.submit(task);
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
                throw new NullPointerException("trace");
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
