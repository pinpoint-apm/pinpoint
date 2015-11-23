/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

/**
 * @Author Taejin Koo
 */
public class ThreadDumpUtils {

    public static TThreadDump createTThreadDump(Thread thread) {
        ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(thread);
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
        for (StackTraceElement element : stackTraceElements) {
            threadDump.addToStackTrace(element.toString());
        }
    }

    private static void setMonitorInfo(TThreadDump threadDump, ThreadInfo threadInfo) {
        MonitorInfo[] monitorInfos = threadInfo.getLockedMonitors();
        for (MonitorInfo each : monitorInfos) {
            TMonitorInfo tMonitorInfo = new TMonitorInfo();

            tMonitorInfo.setStackDepth(each.getLockedStackDepth());
            tMonitorInfo.setStackFrame(each.getLockedStackFrame().toString());

            threadDump.addToLockedMonitors(tMonitorInfo);
        }
    }

    private static void setLockInfo(TThreadDump threadDump, ThreadInfo threadInfo) {
        threadDump.setLockName(threadInfo.getLockName());
        threadDump.setLockOwnerId(threadInfo.getLockOwnerId());
        threadDump.setLockOwnerName(threadInfo.getLockOwnerName());

        LockInfo[] lockInfos = threadInfo.getLockedSynchronizers();
        for (LockInfo lockInfo : lockInfos) {
            threadDump.addToLockedSynchronizers(lockInfo.toString());
        }
    }

    private static TThreadState getThreadState(ThreadInfo info) {
        String stateName = info.getThreadState().name();

        for (TThreadState state : TThreadState.values()) {
            if (state.name().equalsIgnoreCase(stateName)) {
                return state;
            }
        }

        return null;
    }

}
