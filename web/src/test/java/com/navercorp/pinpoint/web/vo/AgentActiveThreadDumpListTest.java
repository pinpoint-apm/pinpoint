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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandom;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.context.thrift.ThreadDumpThriftMessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.ThreadStateThriftMessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpListTest {

    private static final int CREATE_DUMP_SIZE = 10;

    private final PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory(this.getClass().getSimpleName());
    private ThreadDumpThriftMessageConverter threadDumpThriftMessageConverter = new ThreadDumpThriftMessageConverter();
    private ThreadStateThriftMessageConverter threadStateThriftMessageConverter = new ThreadStateThriftMessageConverter();

    @Test
    public void basicFunctionTest1() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_DUMP_SIZE);
        try {
            Thread[] threads = createThread(waitingJobList);

            AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(threads);

            Assert.assertEquals(CREATE_DUMP_SIZE, activeThreadDumpList.getAgentActiveThreadDumpRepository().size());
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void basicFunctionTest2() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_DUMP_SIZE);
        try {
            Thread[] threads = createThread(waitingJobList);

            AgentActiveThreadDumpList activeThreadDumpList = createThreadLightDumpList(threads);

            List<AgentActiveThreadDump> sortOldestAgentActiveThreadDumpRepository = activeThreadDumpList.getSortOldestAgentActiveThreadDumpRepository();

            long before = 0;
            for (AgentActiveThreadDump dump : sortOldestAgentActiveThreadDumpRepository) {
                long startTime = dump.getStartTime();
                if (before > startTime) {
                    Assert.fail();
                }
                before = startTime;
            }
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void checkUnmodifiableList() throws Exception {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_DUMP_SIZE);
        try {
            Thread[] threads = createThread(waitingJobList);

            AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(threads);

            List<AgentActiveThreadDump> agentActiveThreadDumpRepository = activeThreadDumpList.getAgentActiveThreadDumpRepository();
            agentActiveThreadDumpRepository.remove(0);
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

    private Thread[] createThread(List<WaitingJob> runnableList) {
        Thread[] threads = new Thread[runnableList.size()];

        for (int i = 0; i < runnableList.size(); i++) {
            Thread thread = createThread(runnableList.get(i));
            threads[i] = thread;
        }

        return threads;
    }

    private Thread createThread(Runnable runnable) {
        Thread thread = pinpointThreadFactory.newThread(runnable);
        thread.start();
        return thread;
    }

    private AgentActiveThreadDumpList createThreadDumpList(Thread[] threads) {
        List<TActiveThreadDump> activeThreadDumpList = new ArrayList<>();
        for (Thread thread : threads) {
            TActiveThreadDump tActiveThreadDump = new TActiveThreadDump();
            tActiveThreadDump.setStartTime(System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(100000));

            final ThreadDumpMetricSnapshot threadDumpMetricSnapshot =ThreadDumpUtils.createThreadDump(thread);
            final TThreadDump threadDump = this.threadDumpThriftMessageConverter.toMessage(threadDumpMetricSnapshot);

            tActiveThreadDump.setThreadDump(threadDump);
            activeThreadDumpList.add(tActiveThreadDump);
        }

        AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
        return factory.create1(activeThreadDumpList);
    }

    private AgentActiveThreadDumpList createThreadLightDumpList(Thread[] threads) {
        List<TActiveThreadLightDump> activeThreadLightDumpList = new ArrayList<>();
        for (Thread thread : threads) {
            TActiveThreadLightDump tActiveThreadDump = new TActiveThreadLightDump();
            tActiveThreadDump.setStartTime(System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(100000));
            tActiveThreadDump.setThreadDump(createTThreadLightDump(thread));
            activeThreadLightDumpList.add(tActiveThreadDump);
        }

        AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
        return factory.create2(activeThreadLightDumpList);
    }

    private TThreadLightDump createTThreadLightDump(Thread thread) {
        TThreadLightDump threadDump = new TThreadLightDump();
        threadDump.setThreadName(thread.getName());
        threadDump.setThreadId(thread.getId());

        final TThreadState threadState = this.threadStateThriftMessageConverter.toMessage(thread.getState());
        threadDump.setThreadState(threadState);
        return threadDump;
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
