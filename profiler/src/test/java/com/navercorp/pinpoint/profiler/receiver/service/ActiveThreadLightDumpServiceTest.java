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
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
            List<ActiveTraceInfo> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

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
            List<ActiveTraceInfo> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

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

            List<ActiveTraceInfo> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

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
            List<ActiveTraceInfo> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);
            List<ActiveTraceInfo> copied = shuffle(activeTraceInfoList);

            int targetThreadNameSize = 3;
            List<String> threadNameList = extractThreadNameList(copied.subList(0, targetThreadNameSize), targetThreadNameSize);

            int targetTraceIdSize = 3;
            List<Long> localTraceIdList = extractLocalTraceIdList(copied.subList(targetThreadNameSize, CREATE_SIZE), targetTraceIdSize);
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
            List<ActiveTraceInfo> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

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
            waitingJobList.add(new WaitingJob(100));
        }
        return waitingJobList;
    }

    private List<ActiveTraceInfo> createMockActiveTraceInfoList(int createActiveTraceRepositorySize, long currentTimeMillis, long diff, List<WaitingJob> waitingJobList) {
        List<ActiveTraceInfo> activeTraceInfoList = new ArrayList<ActiveTraceInfo>(createActiveTraceRepositorySize);
        for (int i = 0; i < createActiveTraceRepositorySize; i++) {
            ActiveTraceInfo activeTraceInfo = createActiveTraceInfo(currentTimeMillis + (diff * i), waitingJobList.get(i));
            activeTraceInfoList.add(activeTraceInfo);
        }
        return activeTraceInfoList;
    }

    private ActiveTraceInfo createActiveTraceInfo(long startTime, Runnable runnable) {
        Thread thread = pinpointThreadFactory.newThread(runnable);
        thread.start();
        return new ActiveTraceInfo(idGenerator.incrementAndGet(), startTime, thread);
    }

    private List<Long> getOldTimeList(int maxCount) {
        List<Long> startTimeMillisList = new ArrayList<Long>(maxCount);
        for (int i = 0; i < maxCount; i++) {
            startTimeMillisList.add(DEFAULT_TIME_MILLIS + (TIME_DIFF_INTERVAL * i));
        }
        return startTimeMillisList;
    }

    private List<String> extractThreadNameList(List<ActiveTraceInfo> activeTraceInfoList, int size) {
        List<ActiveTraceInfo> copied = shuffle(activeTraceInfoList);

        List<String> threadNameList = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            threadNameList.add(copied.get(i).getThread().getName());
        }

        return threadNameList;
    }

    private List<Long> extractLocalTraceIdList(List<ActiveTraceInfo> activeTraceInfoList, int size) {
        List<ActiveTraceInfo> copied = shuffle(activeTraceInfoList);

        List<Long> localTraceIdList = new ArrayList<Long>(size);
        for (int i = 0; i < size; i++) {
            localTraceIdList.add(copied.get(i).getLocalTraceId());
        }

        return localTraceIdList;
    }

    private <E> List<E> shuffle(List<E> list) {
        ArrayList<E> copied = new ArrayList<E>(list);
        Collections.shuffle(copied, ThreadLocalRandom.current());
        return copied;
    }

    private ActiveThreadLightDumpService createService(List<ActiveTraceInfo> activeTraceInfoList) {
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

        private final long timeIntervalMillis;
        private boolean close = false;

        public WaitingJob(long timeIntervalMillis) {
            this.timeIntervalMillis = timeIntervalMillis;
        }

        @Override
        public void run() {
            while (!close) {
                try {
                    Thread.sleep(timeIntervalMillis);
                } catch (InterruptedException e) {
                    close = true;
                }
            }
        }

        public void close() {
            this.close = true;
        }

    }

}
