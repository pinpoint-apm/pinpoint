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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.MonitorInfoMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Collections;

/**
 * @author Taejin Koo
 */
public class ThreadDumpUtils {

    public static ThreadDumpMetricSnapshot createThreadDump(Thread thread) {
        Assert.requireNonNull(thread, "thread");
        final ThreadInfo threadInfo = ThreadMXBeanUtils.getThreadInfo(thread.getId());
        if (threadInfo == null) {
            return null;
        }

        return createThreadDump(threadInfo);
    }

    public static ThreadDumpMetricSnapshot createThreadDump(Thread thread, int stackTraceMaxDepth) {
        Assert.requireNonNull(thread, "thread");
        final ThreadInfo threadInfo = ThreadMXBeanUtils.getThreadInfo(thread.getId(), stackTraceMaxDepth);
        if (threadInfo == null) {
            return null;
        }

        return createThreadDump(threadInfo);
    }

    public static ThreadDumpMetricSnapshot createThreadDump(ThreadInfo threadInfo) {
        final ThreadDumpMetricSnapshot threadDump = new ThreadDumpMetricSnapshot();
        setThreadInfo(threadDump, threadInfo);
        setThreadStatus(threadDump, threadInfo);
        setStackTrace(threadDump, threadInfo);
        setMonitorInfo(threadDump, threadInfo);
        setLockInfo(threadDump, threadInfo);

        return threadDump;
    }

    public static ThreadDumpMetricSnapshot createTThreadDump(long threadId) {
        final ThreadInfo threadInfo = ThreadMXBeanUtils.getThreadInfo(threadId);
        if (threadInfo == null) {
            return null;
        }

        return createThreadDump(threadInfo);
    }

    private static void setThreadInfo(ThreadDumpMetricSnapshot threadDump, ThreadInfo threadInfo) {
        threadDump.setThreadName(threadInfo.getThreadName());
        threadDump.setThreadId(threadInfo.getThreadId());
        threadDump.setBlockedTime(threadInfo.getBlockedTime());
        threadDump.setBlockedCount(threadInfo.getBlockedCount());
        threadDump.setWaitedTime(threadInfo.getWaitedTime());
        threadDump.setWaitedCount(threadInfo.getWaitedCount());
    }

    private static void setThreadStatus(ThreadDumpMetricSnapshot threadDump, ThreadInfo threadInfo) {
        threadDump.setInNative(threadInfo.isInNative());
        threadDump.setSuspended(threadInfo.isSuspended());
        threadDump.setThreadState(threadInfo.getThreadState());
    }

    private static void setStackTrace(ThreadDumpMetricSnapshot threadDump, ThreadInfo threadInfo) {
        StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement element : stackTraceElements) {
                if (element == null) {
                    continue;
                }
                threadDump.addStackTrace(element.toString());
            }
        } else {
            threadDump.setStackTrace(Collections.<String>emptyList());
        }
    }

    private static void setMonitorInfo(ThreadDumpMetricSnapshot threadDump, ThreadInfo threadInfo) {
        MonitorInfo[] monitorInfos = threadInfo.getLockedMonitors();
        if (monitorInfos != null) {
            for (MonitorInfo each : monitorInfos) {
                if (each == null) {
                    continue;
                }
                MonitorInfoMetricSnapshot monitorInfoMetricSnapshot = new MonitorInfoMetricSnapshot();
                monitorInfoMetricSnapshot.setStackDepth(each.getLockedStackDepth());
                monitorInfoMetricSnapshot.setStackFrame(each.getLockedStackFrame().toString());
                threadDump.addLockedMonitor(monitorInfoMetricSnapshot);
            }
        } else {
            threadDump.setLockedMonitors(Collections.<MonitorInfoMetricSnapshot>emptyList());
        }
    }

    private static void setLockInfo(ThreadDumpMetricSnapshot threadDump, ThreadInfo threadInfo) {
        threadDump.setLockName(threadInfo.getLockName());
        threadDump.setLockOwnerId(threadInfo.getLockOwnerId());
        threadDump.setLockOwnerName(threadInfo.getLockOwnerName());

        LockInfo[] lockInfos = threadInfo.getLockedSynchronizers();

        if (lockInfos != null) {
            for (LockInfo lockInfo : lockInfos) {
                if (lockInfo == null) {
                    continue;
                }
                threadDump.addLockedSynchronizer(lockInfo.toString());
            }
        } else {
            threadDump.setLockedSynchronizers(Collections.<String>emptyList());
        }
    }
}
