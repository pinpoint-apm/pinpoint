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

import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.thrift.dto.command.*;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author koo.taejin
 */
public class ThreadDumpService implements ProfilerRequestCommandService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public TBase<?, ?> requestCommandService(TBase tbase) {
        logger.info("{} execute {}.", this, tbase);

        TCommandThreadDump param = (TCommandThreadDump) tbase;
        TThreadDumpType type = param.getType();

        List<ThreadInfo> threadInfoList = null;
        if (TThreadDumpType.TARGET == type) {
            threadInfoList = getThreadInfo(param.getName());
        } else if (TThreadDumpType.PENDING == type) {
            threadInfoList = getThreadInfo(param.getPendingTimeMillis());
        } else {
            threadInfoList = Arrays.asList(getAllThreadInfo());
        }

        TCommandThreadDumpResponse response = new TCommandThreadDumpResponse();

        for (ThreadInfo info : threadInfoList) {
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

            response.addToThreadDumps(dump);
        }

        return response;
    }

    private TThreadState getThreadState(ThreadInfo info) {

        String stateName = info.getThreadState().name();

        for (TThreadState state : TThreadState.values()) {
            if (state.name().equalsIgnoreCase(stateName)) {
                return state;
            }
        }

        return null;
    }

    private List<ThreadInfo> getThreadInfo(String threadName) {
        List<ThreadInfo> result = new ArrayList<ThreadInfo>();

        if (threadName == null || threadName.trim().equals("")) {
            return Arrays.asList(getAllThreadInfo());
        }

        for (ThreadInfo threadIno : getAllThreadInfo()) {
            if (threadName.equals(threadIno.getThreadName())) {
                result.add(threadIno);
            }
        }

        return result;
    }

    // TODO : need to modify later
    private List<ThreadInfo> getThreadInfo(long pendingTimeMillis) {
        List<ThreadInfo> result = new ArrayList<ThreadInfo>();

        if (pendingTimeMillis <= 0) {
            return Arrays.asList(getAllThreadInfo());
        }

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
