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

package com.navercorp.pinpoint.web.vo.activethread;

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.realtime.dto.ActiveThreadDump;
import com.navercorp.pinpoint.thrift.sender.message.ThreadStateGrpcMessageConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpListTest {

    private static final int CREATE_DUMP_SIZE = 10;

    private final PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory(this.getClass().getSimpleName());
    private static final ThreadStateGrpcMessageConverter threadStateConverter = new ThreadStateGrpcMessageConverter();

    @Test
    public void basicFunctionTest1() {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_DUMP_SIZE);
        try {
            Thread[] threads = createThread(waitingJobList);

            AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(threads);

            assertThat(activeThreadDumpList.getAgentActiveThreadDumpRepository()).hasSize(CREATE_DUMP_SIZE);
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void basicFunctionTest2() {
        List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_DUMP_SIZE);
        try {
            Thread[] threads = createThread(waitingJobList);

            AgentActiveThreadDumpList activeThreadDumpList = createThreadLightDumpList(threads);

            List<AgentActiveThreadDump> sortOldestAgentActiveThreadDumpRepository = activeThreadDumpList.getSortOldestAgentActiveThreadDumpRepository();

            long before = 0;
            for (AgentActiveThreadDump dump : sortOldestAgentActiveThreadDumpRepository) {
                long startTime = dump.getStartTime();
                if (before > startTime) {
                    Assertions.fail();
                }
                before = startTime;
            }
        } finally {
            clearResource(waitingJobList);
        }
    }

    @Test
    public void checkUnmodifiableList() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            List<WaitingJob> waitingJobList = createWaitingJobList(CREATE_DUMP_SIZE);
            try {
                Thread[] threads = createThread(waitingJobList);

                AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(threads);

                List<AgentActiveThreadDump> agentActiveThreadDumpRepository = activeThreadDumpList.getAgentActiveThreadDumpRepository();
                agentActiveThreadDumpRepository.remove(0);
            } finally {
                clearResource(waitingJobList);
            }
        });
    }

    private List<WaitingJob> createWaitingJobList(int createActiveTraceRepositorySize) {
        List<WaitingJob> waitingJobList = new ArrayList<>();
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
        List<ActiveThreadDump> activeThreadDumpList = new ArrayList<>();
        for (Thread thread : threads) {
            ActiveThreadDump dump = new ActiveThreadDump();
            dump.setStartTime(System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(100000));
            dump.setThreadDump(createThreadDump(thread));
            activeThreadDumpList.add(dump);
        }

        AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
        return factory.create1(activeThreadDumpList);
    }

    private AgentActiveThreadDumpList createThreadLightDumpList(Thread[] threads) {
        List<ActiveThreadDump> activeThreadLightDumpList = new ArrayList<>();
        for (Thread thread : threads) {
            ActiveThreadDump dump = new ActiveThreadDump();
            dump.setStartTime(System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(100000));
            dump.setThreadDump(createThreadLightDump(thread));
            activeThreadLightDumpList.add(dump);
        }

        AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
        return factory.create2(activeThreadLightDumpList);
    }

    private static ThreadDumpBo createThreadDump(Thread thread) {
        ThreadDumpMetricSnapshot snapshot = Objects.requireNonNull(ThreadDumpUtils.createThreadDump(thread));
        return createThreadDump(snapshot);
    }

    public static ThreadDumpBo createThreadDump(ThreadDumpMetricSnapshot snapshot) {
        ThreadDumpBo dump = new ThreadDumpBo();
        dump.setThreadName(snapshot.getThreadName());
        dump.setThreadId(snapshot.getThreadId());
        dump.setThreadState(ThreadState.findByValue(threadStateConverter.toMessage(snapshot.getThreadState()).getNumber()));
        dump.setStackTraceList(snapshot.getStackTrace());
        dump.setLockedSynchronizerList(snapshot.getLockedSynchronizers());
        dump.setLockedMonitorInfoList(snapshot.getLockedMonitors().stream()
                .map(el -> new MonitorInfoBo(el.getStackDepth(), el.getStackFrame()))
                .toList());
        dump.setLockName(snapshot.getLockName());
        dump.setLockOwnerId(snapshot.getLockOwnerId());
        dump.setLockOwnerName(snapshot.getLockOwnerName());
        dump.setSuspended(snapshot.isSuspended());
        dump.setWaitedCount(snapshot.getWaitedCount());
        dump.setWaitedTime(snapshot.getWaitedTime());
        dump.setBlockedCount(snapshot.getBlockedCount());
        dump.setBlockedTime(snapshot.getBlockedTime());
        dump.setInNative(snapshot.isInNative());
        return dump;
    }

    private ThreadDumpBo createThreadLightDump(Thread thread) {
        ThreadDumpBo dump = new ThreadDumpBo();
        dump.setThreadName(thread.getName());
        dump.setThreadId(thread.getId());
        dump.setThreadState(ThreadState.findByValue(threadStateConverter.toMessage(thread.getState()).getNumber()));
        return dump;
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
