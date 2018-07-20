/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDumpType;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author koo.taejin
 */
public class ThreadDumpService implements ProfilerRequestCommandService {

    private static final Set<TThreadState> THREAD_STATES = EnumSet.allOf(TThreadState.class);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public TBase<?, ?> requestCommandService(TBase tbase) {
        logger.info("{} execute {}.", this, tbase);

        TCommandThreadDump param = (TCommandThreadDump) tbase;
        TThreadDumpType type = param.getType();

        final List<ThreadInfo> threadInfoList = getThreadInfos(type, param);

        final List<TThreadDump> threadDumpList = getTThreadDumpList(threadInfoList);

        TCommandThreadDumpResponse response = new TCommandThreadDumpResponse();
        response.setThreadDumps(threadDumpList);
        return response;
    }

    private List<TThreadDump> getTThreadDumpList(List<ThreadInfo> threadInfoList) {
        final List<TThreadDump> threadDumpList = new ArrayList<TThreadDump>(threadInfoList.size());
        for (ThreadInfo info : threadInfoList) {
            final TThreadDump dump = toTThreadDump(info);
            threadDumpList.add(dump);
        }
        return threadDumpList;
    }

    private TThreadDump toTThreadDump(ThreadInfo info) {
        TThreadDump dump = new TThreadDump();

        dump.setThreadName(info.getThreadName());
        dump.setThreadId(info.getThreadId());
        dump.setBlockedTime(info.getBlockedTime());
        dump.setBlockedCount(info.getBlockedCount());
        dump.setWaitedTime(info.getWaitedTime());
        dump.setWaitedCount(info.getWaitedCount());

        dump.setLockName(info.getLockName());
        dump.setLockOwnerId(info.getLockOwnerId());
        dump.setLockOwnerName(info.getLockOwnerName());

        dump.setInNative(info.isInNative());
        dump.setSuspended(info.isSuspended());

        dump.setThreadState(getThreadState(info));

        StackTraceElement[] stackTraceElements = info.getStackTrace();
        for (StackTraceElement each : stackTraceElements) {
            dump.addToStackTrace(each.toString());
        }

        MonitorInfo[] monitorInfos = info.getLockedMonitors();
        for (MonitorInfo each : monitorInfos) {
            TMonitorInfo tMonitorInfo = new TMonitorInfo();

            tMonitorInfo.setStackDepth(each.getLockedStackDepth());
            tMonitorInfo.setStackFrame(each.getLockedStackFrame().toString());

            dump.addToLockedMonitors(tMonitorInfo);
        }

        LockInfo[] lockInfos = info.getLockedSynchronizers();
        for (LockInfo lockInfo : lockInfos) {
            dump.addToLockedSynchronizers(lockInfo.toString());
        }
        return dump;
    }

    private List<ThreadInfo> getThreadInfos(TThreadDumpType type, TCommandThreadDump param) {
        if (TThreadDumpType.TARGET == type) {
            return getThreadInfo(param.getName());
        } else if (TThreadDumpType.PENDING == type) {
            return getThreadInfo(param.getPendingTimeMillis());
        }

        return Arrays.asList(getAllThreadInfo());
    }

    private TThreadState getThreadState(ThreadInfo info) {

        final String stateName = info.getThreadState().name();

        for (TThreadState state : THREAD_STATES) {
            if (state.name().equalsIgnoreCase(stateName)) {
                return state;
            }
        }

        return null;
    }

    private List<ThreadInfo> getThreadInfo(String threadName) {
        if (!StringUtils.hasText(threadName)) {
            return Arrays.asList(getAllThreadInfo());
        }

        final List<ThreadInfo> result = new ArrayList<ThreadInfo>();
        for (ThreadInfo threadIno : getAllThreadInfo()) {
            if (threadName.equals(threadIno.getThreadName())) {
                result.add(threadIno);
            }
        }

        return result;
    }

    // TODO : need to modify later
    private List<ThreadInfo> getThreadInfo(long pendingTimeMillis) {
        if (pendingTimeMillis <= 0) {
            return Arrays.asList(getAllThreadInfo());
        }

        final List<ThreadInfo> result = new ArrayList<ThreadInfo>();
        for (ThreadInfo threadInfo : getAllThreadInfo()) {
            if (threadInfo.getBlockedTime() >= pendingTimeMillis) {
                result.add(threadInfo);
                continue;
            }

            if (threadInfo.getWaitedTime() >= pendingTimeMillis) {
                result.add(threadInfo);
            }
        }

        return result;
    }

    private ThreadInfo[] getAllThreadInfo() {
        ThreadInfo[] threadInfos = ThreadMXBeanUtils.dumpAllThread();

        return threadInfos;
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TCommandThreadDump.class;
    }

}
