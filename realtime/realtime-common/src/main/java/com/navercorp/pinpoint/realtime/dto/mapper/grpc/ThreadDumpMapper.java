/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto.mapper.grpc;

import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.realtime.dto.MonitorInfo;
import com.navercorp.pinpoint.realtime.dto.ThreadDump;
import com.navercorp.pinpoint.realtime.dto.ThreadState;

import static com.navercorp.pinpoint.realtime.dto.mapper.grpc.MapperUtils.mapList;
import static com.navercorp.pinpoint.realtime.dto.mapper.grpc.MapperUtils.nonNullList;

/**
 * @author youngjin.kim2
 */
class ThreadDumpMapper {

    static ThreadDump from(PThreadDump s) {
        final ThreadDump t = new ThreadDump();
        t.setThreadId(s.getThreadId());
        t.setThreadName(s.getThreadName());
        t.setThreadState(ThreadState.fromValue(s.getThreadState().getNumber()));
        t.setBlockedCount(s.getBlockedCount());
        t.setBlockedTime(s.getBlockedTime());
        t.setInNative(s.getInNative());
        t.setLockedMonitors(mapList(s.getLockedMonitorList(), ThreadDumpMapper::from));
        t.setLockedSynchronizers(s.getLockedSynchronizerList());
        t.setLockName(s.getLockName());
        t.setLockOwnerId(s.getLockOwnerId());
        t.setLockOwnerName(s.getLockOwnerName());
        t.setWaitedTime(s.getWaitedTime());
        t.setWaitedCount(s.getWaitedCount());
        t.setSuspended(s.getSuspended());
        t.setStackTrace(s.getStackTraceList());
        return t;
    }

    static ThreadDump from(PThreadLightDump s) {
        final ThreadDump t = new ThreadDump();
        t.setThreadId(s.getThreadId());
        t.setThreadName(s.getThreadName());
        t.setThreadState(ThreadState.fromValue(s.getThreadState().getNumber()));
        return t;
    }

    static PThreadDump into(ThreadDump s) {
        assert s.getThreadState() != null;
        return PThreadDump.newBuilder()
                .setThreadId(s.getThreadId())
                .setThreadName(s.getThreadName())
                .setThreadStateValue(s.getThreadState().getValue())
                .setBlockedCount(s.getBlockedCount())
                .setBlockedTime(s.getBlockedTime())
                .setInNative(s.isInNative())
                .addAllLockedMonitor(mapList(s.getLockedMonitors(), ThreadDumpMapper::into))
                .addAllLockedSynchronizer(nonNullList(s.getLockedSynchronizers()))
                .setLockName(s.getLockName())
                .setLockOwnerId(s.getLockOwnerId())
                .setLockOwnerName(s.getLockOwnerName())
                .setWaitedTime(s.getWaitedTime())
                .setWaitedCount(s.getWaitedCount())
                .setSuspended(s.isSuspended())
                .addAllStackTrace(s.getStackTrace())
                .build();
    }

    static PThreadLightDump intoLight(ThreadDump s) {
        assert s.getThreadState() != null;
        return PThreadLightDump.newBuilder()
                .setThreadId(s.getThreadId())
                .setThreadName(s.getThreadName())
                .setThreadStateValue(s.getThreadState().getValue())
                .build();
    }

    private static MonitorInfo from(PMonitorInfo s) {
        final MonitorInfo t = new MonitorInfo();
        t.setStackDepth(s.getStackDepth());
        t.setStackFrame(s.getStackFrame());
        return t;
    }

    private static PMonitorInfo into(MonitorInfo s) {
        return PMonitorInfo.newBuilder()
                .setStackDepth(s.getStackDepth())
                .setStackFrame(s.getStackFrame())
                .build();
    }

}
