/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class DeadlockMonitorTask implements Runnable {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DeadlockThreadRegistry deadlockThreadRegistry;
    private final long intervalMillis;

    private AtomicBoolean stop = new AtomicBoolean(false);

    public DeadlockMonitorTask(DeadlockThreadRegistry deadlockThreadRegistry, long intervalMillis) {
        this.deadlockThreadRegistry = deadlockThreadRegistry;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public void run() {
        while (!stop.get()) {
            doTask();
            waitNextTask();
        }
        logger.info("DeadlockMonitorTask stop completed");
    }

    void doTask() {
        long[] deadlockedThreadIds = ThreadMXBeanUtils.findDeadlockedThreads();

        if (ArrayUtils.isEmpty(deadlockedThreadIds)) {
            return;
        }

        boolean foundNewDeadlockedThread = false;
        for (long deadlockedThreadId : deadlockedThreadIds) {
            boolean added = deadlockThreadRegistry.addDeadlockedThread(deadlockedThreadId);
            if (added) {
                foundNewDeadlockedThread = true;
            }
        }

        if (foundNewDeadlockedThread) {
            StringBuilder deadlockOutput = new StringBuilder();
            deadlockOutput.append(LINE_SEPARATOR);
            deadlockOutput.append("================================================================").append(LINE_SEPARATOR);
            deadlockOutput.append("[PINPOINT] Found one Java-level deadlock:").append(LINE_SEPARATOR);
            deadlockOutput.append(LINE_SEPARATOR);
            deadlockOutput.append("If pinpoints affect the deadlock below, please put all the information posted on pinpoint's github.").append(LINE_SEPARATOR);
            deadlockOutput.append("(https://github.com/naver/pinpoint/issues)").append(LINE_SEPARATOR);
            deadlockOutput.append("================================================================").append(LINE_SEPARATOR);

            for (long deadlockedThreadId : deadlockedThreadIds) {
                ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(deadlockedThreadId);
                deadlockOutput.append(createThreadDump(threadInfo));
            }
            deadlockOutput.append("================================================================").append(LINE_SEPARATOR);

            logger.warn(deadlockOutput.toString());
        }
    }

    /**
     * refer to java.lang.management.ThreadInfo.toString {@link ThreadInfo}
     * To find loadClass cause. MAX_FRAME is too short , the length.
     */
    private String createThreadDump(ThreadInfo threadInfo) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" +
                " Id=" + threadInfo.getThreadId() + " " + threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            sb.append(" on " + threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"" + threadInfo.getLockOwnerName() +
                    "\" Id=" + threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');

        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                LockInfo lockInfo = threadInfo.getLockInfo();
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + lockInfo);
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + lockInfo);
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + lockInfo);
                        sb.append('\n');
                        break;
                    default:
                }
            }

            MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    private void waitNextTask() {
        if (!Thread.interrupted()) {
            try {
                Thread.sleep(intervalMillis);
            } catch (InterruptedException ignore) {
                // It only exhaust time to wait using interrupt.
                // The end of the job is confirmed by using the stop field.
            }
        }
    }

    void stop() {
        if (stop.compareAndSet(false, true)) {
            logger.info("DeadlockMonitorTask stop started");
        } else {
            logger.info("DeadlockMonitorTask already stopped");
        }
    }

}
