/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.UnsampledActiveTraceSnapshot;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ThreadInfo;
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
public class ActiveThreadDumpServiceTest {

    private static final int CREATE_SIZE = 10;

    private static final long DEFAULT_TIME_MILLIS = System.currentTimeMillis() - 1000000;
    private static final long TIME_DIFF_INTERVAL = 100;
    private static final long JOB_TIMEOUT = 1000 * 10;

    private final AtomicInteger idGenerator = new AtomicInteger();
    private final PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory(this.getClass().getSimpleName());

    private final WaitingJobListFactory waitingJobListFactory = new WaitingJobListFactory();

    @After
    public void tearDown() throws Exception {
        waitingJobListFactory.close();
    }

    @Test
    public void basicFunctionTest1() throws Exception {

        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, JOB_TIMEOUT);

        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        ActiveThreadDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadDumpRes response = (TCmdActiveThreadDumpRes) service.requestCommandService(createRequest(0, null, null));

        Assert.assertEquals(CREATE_SIZE, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest2() throws Exception {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, 1000 * 3);

        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        TCmdActiveThreadDump tCmdActiveThreadDump = createRequest(0, null, Arrays.asList(1L));

        ActiveThreadDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadDumpRes response = (TCmdActiveThreadDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assert.assertEquals(1, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest3() throws Exception {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, 1000 * 3);

        int targetThreadNameSize = 3;

        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        List<String> threadNameList = extractThreadNameList(activeTraceInfoList, targetThreadNameSize);
        TCmdActiveThreadDump tCmdActiveThreadDump = createRequest(0, threadNameList, null);

        ActiveThreadDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadDumpRes response = (TCmdActiveThreadDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assert.assertEquals(3, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest4() throws Exception {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, 1000 * 3);



        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);
        List<ActiveTraceSnapshot> copied = shuffle(activeTraceInfoList);

        int targetThreadNameSize = 3;
        List<String> threadNameList = extractThreadNameList(copied.subList(0, targetThreadNameSize), targetThreadNameSize);

        int targetTraceIdSize = 3;
        List<Long> localTraceIdList = extractLocalTraceIdList(copied.subList(targetThreadNameSize, CREATE_SIZE), targetTraceIdSize);
        TCmdActiveThreadDump tCmdActiveThreadDump = createRequest(0, threadNameList, localTraceIdList);

        ActiveThreadDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadDumpRes response = (TCmdActiveThreadDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assert.assertEquals(targetThreadNameSize + targetTraceIdSize, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest5() throws Exception {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, 1000 * 3);


        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        int limit = 3;
        List<Long> oldTimeList = getOldTimeList(limit);

        TCmdActiveThreadDump tCmdActiveThreadDump = createRequest(limit, null, null);

        ActiveThreadDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadDumpRes response = (TCmdActiveThreadDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assert.assertEquals(limit, response.getThreadDumpsSize());

        for (TActiveThreadDump dump : response.getThreadDumps()) {
            Assert.assertTrue(oldTimeList.contains(dump.getStartTime()));
        }

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
        long id = thread.getId();
        return new UnsampledActiveTraceSnapshot(idGenerator.incrementAndGet(), startTime, id);
    }

    private List<Long> getOldTimeList(int maxCount) {
        List<Long> startTimeMillisList = new ArrayList<Long>(maxCount);
        for (int i = 0; i < maxCount; i++) {
            startTimeMillisList.add(DEFAULT_TIME_MILLIS + (TIME_DIFF_INTERVAL * i));
        }
        return startTimeMillisList;
    }

    private List<String> extractThreadNameList(List<ActiveTraceSnapshot> activeTraceInfoList, int size) {
        List<ActiveTraceSnapshot> copied = shuffle(activeTraceInfoList);

        List<String> threadNameList = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {

            final ActiveTraceSnapshot activeTraceSnapshot = copied.get(i);
            ThreadInfo thread = ThreadMXBeanUtils.getThreadInfo(activeTraceSnapshot.getThreadId());
            threadNameList.add(thread.getThreadName());
        }

        return threadNameList;
    }

    private List<Long> extractLocalTraceIdList(List<ActiveTraceSnapshot> activeTraceInfoList, int size) {
        List<ActiveTraceSnapshot> copied = shuffle(activeTraceInfoList);

        List<Long> localTraceIdList = new ArrayList<Long>(size);
        for (int i = 0; i < size; i++) {
            localTraceIdList.add(copied.get(i).getLocalTransactionId());
        }

        return localTraceIdList;
    }

    private <E> List<E> shuffle(List<E> list) {
        List<E> copied = new ArrayList<E>(list);
        Collections.shuffle(copied, ThreadLocalRandom.current());
        return copied;
    }


    private ActiveThreadDumpService createService(List<ActiveTraceSnapshot> activeTraceInfoList) {
        ActiveTraceRepository activeTraceRepository = mock(ActiveTraceRepository.class);
        when(activeTraceRepository.snapshot()).thenReturn(activeTraceInfoList);

        ActiveThreadDumpCoreService activeThreadDump = new ActiveThreadDumpCoreService(activeTraceRepository);
        return new ActiveThreadDumpService(activeThreadDump);
    }



    private TCmdActiveThreadDump createRequest(int limit, List<String> threadNameList, List<Long> localTraceIdList) {
        TCmdActiveThreadDump request = new TCmdActiveThreadDump();
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



}
