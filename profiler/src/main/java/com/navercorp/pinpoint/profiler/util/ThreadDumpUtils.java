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
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ThreadDumpUtils {

    private static final Map<Thread.State, TThreadState> THREAD_STATE_MAP = toTThreadStateMap();

    private static Map<Thread.State, TThreadState>  toTThreadStateMap() {

        final EnumMap<Thread.State, TThreadState> threadStateMap = new EnumMap<Thread.State, TThreadState>(Thread.State.class);

        for (Thread.State threadState : Thread.State.values()) {
            final String threadStateName = threadState.name();
            for (TThreadState tState : TThreadState.values()) {
                if (tState.name().equalsIgnoreCase(threadStateName)) {
                    threadStateMap.put(threadState, tState);
                }
            }
        }
        Assert.state(threadStateMap.size() == Thread.State.values().length, "TThreadStateEnumMap create fail. ");
        return threadStateMap;
    }


    public static TThreadDump createTThreadDump(Thread thread) {
        ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(thread);
        if (threadInfo == null) {
            return null;
        }

        return createTThreadDump(threadInfo);
    }

    public static TThreadDump createTThreadDump(Thread thread, int stackTraceMaxDepth) {
        ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(thread, stackTraceMaxDepth);
        if (threadInfo == null) {
            return null;
        }

        return createTThreadDump(threadInfo);
    }

    public static TThreadDump createTThreadDump(ThreadInfo threadInfo) {
        TThreadDump threadDump = new TThreadDump();
        setThreadInfo(threadDump, threadInfo);
        setThreadStatus(threadDump, threadInfo);
        setStackTrace(threadDump, threadInfo);
        setMonitorInfo(threadDump, threadInfo);
        setLockInfo(threadDump, threadInfo);

        return threadDump;
    }

    public static TThreadDump createTThreadDump(long threadId) {
        ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(threadId);
        if (threadInfo == null) {
            return null;
        }

        return createTThreadDump(threadInfo);
    }

    public static TThreadDump createTThreadDump(long threadId, int stackTraceMaxDepth) {
        ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(threadId, stackTraceMaxDepth);
        if (threadInfo == null) {
            return null;
        }

        return createTThreadDump(threadInfo);
    }

    public static TThreadState toTThreadState(Thread.State threadState) {
        if (threadState == null) {
            throw new NullPointerException("threadState must not be null");
        }
        final TThreadState tThreadState = THREAD_STATE_MAP.get(threadState);
        if (tThreadState == null) {
            return TThreadState.UNKNOWN;
        }
        return tThreadState;
    }

    private static void setThreadInfo(TThreadDump threadDump, ThreadInfo threadInfo) {
        threadDump.setThreadName(threadInfo.getThreadName());
        threadDump.setThreadId(threadInfo.getThreadId());
        threadDump.setBlockedTime(threadInfo.getBlockedTime());
        threadDump.setBlockedCount(threadInfo.getBlockedCount());
        threadDump.setWaitedTime(threadInfo.getWaitedTime());
        threadDump.setWaitedCount(threadInfo.getWaitedCount());
    }

    private static void setThreadStatus(TThreadDump threadDump, ThreadInfo threadInfo) {
        threadDump.setInNative(threadInfo.isInNative());
        threadDump.setSuspended(threadInfo.isSuspended());
        threadDump.setThreadState(getThreadState(threadInfo));
    }

    private static void setStackTrace(TThreadDump threadDump, ThreadInfo threadInfo) {
        StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement element : stackTraceElements) {
                if (element == null) {
                    continue;
                }
                threadDump.addToStackTrace(element.toString());
            }
        } else {
            threadDump.setStackTrace(Collections.<String>emptyList());
        }
    }

    private static void setMonitorInfo(TThreadDump threadDump, ThreadInfo threadInfo) {
        MonitorInfo[] monitorInfos = threadInfo.getLockedMonitors();
        if (monitorInfos != null) {
            for (MonitorInfo each : monitorInfos) {
                if (each == null) {
                    continue;
                }
                TMonitorInfo tMonitorInfo = new TMonitorInfo();

                tMonitorInfo.setStackDepth(each.getLockedStackDepth());
                tMonitorInfo.setStackFrame(each.getLockedStackFrame().toString());

                threadDump.addToLockedMonitors(tMonitorInfo);
            }
        } else {
            threadDump.setLockedMonitors(Collections.<TMonitorInfo>emptyList());
        }
    }

    private static void setLockInfo(TThreadDump threadDump, ThreadInfo threadInfo) {
        threadDump.setLockName(threadInfo.getLockName());
        threadDump.setLockOwnerId(threadInfo.getLockOwnerId());
        threadDump.setLockOwnerName(threadInfo.getLockOwnerName());

        LockInfo[] lockInfos = threadInfo.getLockedSynchronizers();

        if (lockInfos != null) {
            for (LockInfo lockInfo : lockInfos) {
                if (lockInfo == null) {
                    continue;
                }
                threadDump.addToLockedSynchronizers(lockInfo.toString());
            }
        } else {
            threadDump.setLockedSynchronizers(Collections.<String>emptyList());
        }
    }

    private static TThreadState getThreadState(ThreadInfo info) {
        return toTThreadState(info.getThreadState());
    }

}
