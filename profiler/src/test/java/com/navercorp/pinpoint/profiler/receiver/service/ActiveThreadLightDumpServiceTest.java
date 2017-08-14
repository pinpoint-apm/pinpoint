/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.UnsampledActiveTraceSnapshot;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ActiveThreadLightDumpServiceTest {

    private static final int CREATE_SIZE = 10;

    private static final long DEFAULT_TIME_MILLIS = System.currentTimeMillis() - 1000000;
    private static final long TIME_DIFF_INTERVAL = 100;

    private final AtomicInteger idGenerator = new AtomicInteger();
    private final PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory(this.getClass().getSimpleName());

    @Before
    public void setup() {
        idGenerator.set(0);
    }

    @Test
    public void basicFunctionTest1() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_SIZE);

        try {
            List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

            ActiveThreadLightDumpService service = createService(activeTraceInfoList);
            TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(createRequest(0, null, null));

            Assert.assertEquals(CREATE_SIZE, response.getThreadDumpsSize());
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void basicFunctionTest2() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_SIZE);

        try {
            List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

            TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(0, null, Arrays.asList(1L));

            ActiveThreadLightDumpService service = createService(activeTraceInfoList);
            TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

            Assert.assertEquals(1, response.getThreadDumpsSize());
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void basicFunctionTest3() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_SIZE);

        try {
            int targetThreadNameSize = 3;

            List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

            List<String> threadNameList = extractThreadNameList(activeTraceInfoList, targetThreadNameSize);
            TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(0, threadNameList, null);

            ActiveThreadLightDumpService service = createService(activeTraceInfoList);
            TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

            Assert.assertEquals(3, response.getThreadDumpsSize());
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void basicFunctionTest4() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_SIZE);

        try {
            List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);
            List<ActiveTraceSnapshot> activeTraceSnapshotList = shuffle(activeTraceInfoList);

            int targetThreadNameSize = 3;
            List<String> threadNameList = extractThreadNameList(activeTraceSnapshotList.subList(0, targetThreadNameSize), targetThreadNameSize);

            int targetTraceIdSize = 3;
            List<Long> localTraceIdList = extractLocalTraceIdList(activeTraceSnapshotList.subList(targetThreadNameSize, CREATE_SIZE), targetTraceIdSize);
            TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(0, threadNameList, localTraceIdList);

            ActiveThreadLightDumpService service = createService(activeTraceInfoList);
            TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

            Assert.assertEquals(targetThreadNameSize + targetTraceIdSize, response.getThreadDumpsSize());
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void basicFunctionTest5() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_SIZE);

        try {
            List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

            int limit = 3;
            List<Long> oldTimeList = getOldTimeList(limit);

            TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(limit, null, null);

            ActiveThreadLightDumpService service = createService(activeTraceInfoList);
            TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

            Assert.assertEquals(limit, response.getThreadDumpsSize());

            for (TActiveThreadLightDump dump : response.getThreadDumps()) {
                Assert.assertTrue(oldTimeList.contains(dump.getStartTime()));
            }
        } finally {
            clearResource(waitingJobList);
        }
    }

    private List<WaitingJob> createWaitingJobList(int createActiveTraceRepositorySize) {
        List<WaitingJob> waitingJobList = new ArrayList<WaitingJob>();
        for (int i = 0; i < createActiveTraceRepositorySize; i++) {
            WaitingJob latchJob = new WaitingJob(1000 * 10);
            waitingJobList.add(latchJob);
        }
        return waitingJobList;
    }

    private List<ActiveTraceSnapshot> createMockActiveTraceInfoList(int createActiveTraceRepositorySize, long currentTimeMillis, long diff, List<WaitingJob> waitingJobList) {
        List<ActiveTraceSnapshot> activeTraceInfoList = new ArrayList<ActiveTraceSnapshot>(createActiveTraceRepositorySize);
        for (int i = 0; i < createActiveTraceRepositorySize; i++) {
            ActiveTraceSnapshot activeTraceInfo = createActiveTraceInfo(currentTimeMillis + (diff * i), waitingJobList.get(i));
            activeTraceInfoList.add(activeTraceInfo);
        }
        return activeTraceInfoList;
    }

    private ActiveTraceSnapshot createActiveTraceInfo(long startTime, Runnable runnable) {
        Thread thread = pinpointThreadFactory.newThread(runnable);
        thread.start();
        return new UnsampledActiveTraceSnapshot(idGenerator.incrementAndGet(), startTime, thread.getId());
    }

    private List<Long> getOldTimeList(int maxCount) {
        List<Long> startTimeMillisList = new ArrayList<Long>(maxCount);
        for (int i = 0; i < maxCount; i++) {
            startTimeMillisList.add(DEFAULT_TIME_MILLIS + (TIME_DIFF_INTERVAL * i));
        }
        return startTimeMillisList;
    }

    private List<String> extractThreadNameList(List<ActiveTraceSnapshot> activeTraceInfoList, int size) {
        List<ActiveTraceSnapshot> activeTraceSnapshotList = shuffle(activeTraceInfoList);

        List<String> threadNameList = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            final ActiveTraceSnapshot activeTraceSnapshot = activeTraceSnapshotList.get(i);
            final long threadId = activeTraceSnapshot.getThreadId();
            ThreadInfo thread = ThreadMXBeanUtils.findThread(threadId);
            threadNameList.add(thread.getThreadName());
        }

        return threadNameList;
    }

    private List<Long> extractLocalTraceIdList(List<ActiveTraceSnapshot> activeTraceInfoList, int size) {
        List<ActiveTraceSnapshot> activeTraceSnapshotList = shuffle(activeTraceInfoList);

        List<Long> localTraceIdList = new ArrayList<Long>(size);
        for (int i = 0; i < size; i++) {
            localTraceIdList.add(activeTraceSnapshotList.get(i).getLocalTransactionId());
        }

        return localTraceIdList;
    }

    private <E> List<E> shuffle(List<E> list) {
        List<E> result = new ArrayList<E>(list);
        Collections.shuffle(result, ThreadLocalRandom.current());
        return result;
    }

    private ActiveThreadLightDumpService createService(List<ActiveTraceSnapshot> activeTraceInfoList) {
        ActiveTraceRepository activeTraceRepository = mock(ActiveTraceRepository.class);
        when(activeTraceRepository.collect()).thenReturn(activeTraceInfoList);

        return new ActiveThreadLightDumpService(activeTraceRepository);
    }

    private TCmdActiveThreadLightDump createRequest(int limit, List<String> threadNameList, List<Long> localTraceIdList) {
        TCmdActiveThreadLightDump request = new TCmdActiveThreadLightDump();
        if (limit > 0) {
            request.setLimit(limit);
        }
        if (threadNameList != null) {
            request.setThreadNameList(threadNameList);
        }
        if (localTraceIdList != null) {
            request.setLocalTraceIdList(localTraceIdList);
        }
        return request;
    }

    private void clearResource(List<WaitingJob> waitingJobList) {
        if (waitingJobList == null) {
            return;
        }

        for (WaitingJob waitingJob : waitingJobList) {
            waitingJob.close();
        }
    }

    private static class WaitingJob implements Runnable {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final long timeIntervalMillis;

        public WaitingJob(long timeIntervalMillis) {
            this.timeIntervalMillis = timeIntervalMillis;
        }

        @Override
        public void run() {
            try {
                latch.await(timeIntervalMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void close() {
            latch.countDown();
        }

    }

}
