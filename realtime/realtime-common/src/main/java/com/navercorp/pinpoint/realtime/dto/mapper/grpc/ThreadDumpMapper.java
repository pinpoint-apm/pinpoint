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
import com.navercorp.pinpoint.grpc.trace.PThreadState;
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
        PThreadDump.Builder builder = PThreadDump.newBuilder()
                .setThreadId(s.getThreadId())
                .setBlockedTime(s.getBlockedTime())
                .setBlockedCount(s.getBlockedCount())
                .setWaitedTime(s.getWaitedTime())
                .setWaitedCount(s.getWaitedCount())
                .setLockOwnerId(s.getLockOwnerId())
                .setInNative(s.isInNative())
                .setSuspended(s.isSuspended());

        if (s.getThreadName() != null) {
            builder.setThreadName(s.getThreadName());
        }
        if (s.getLockName() != null) {
            builder.setLockName(s.getLockName());
        }
        if (s.getLockOwnerName() != null) {
            builder.setLockOwnerName(s.getLockOwnerName());
        }
        if (s.getThreadState() != null) {
            builder.setThreadState(PThreadState.forNumber(s.getThreadState().getValue()));
        }
        if (s.getStackTrace() != null) {
            builder.addAllStackTrace(nonNullList(s.getStackTrace()));
        }
        if (s.getLockedMonitors() != null) {
            builder.addAllLockedMonitor(mapList(s.getLockedMonitors(), ThreadDumpMapper::into));
        }
        if (s.getLockedSynchronizers() != null) {
            builder.addAllLockedSynchronizer(nonNullList(s.getLockedSynchronizers()));
        }
        return builder.build();
    }

    static PThreadLightDump intoLight(ThreadDump s) {
        PThreadLightDump.Builder builder = PThreadLightDump.newBuilder()
                .setThreadId(s.getThreadId());
        if (s.getThreadName() != null) {
            builder.setThreadName(s.getThreadName());
        }
        if (s.getThreadState() != null) {
            builder.setThreadState(PThreadState.forNumber(s.getThreadState().getValue()));
        }
        return builder.build();
    }

    private static MonitorInfo from(PMonitorInfo s) {
        final MonitorInfo t = new MonitorInfo();
        t.setStackDepth(s.getStackDepth());
        t.setStackFrame(s.getStackFrame());
        return t;
    }

    private static PMonitorInfo into(MonitorInfo s) {
        PMonitorInfo.Builder builder = PMonitorInfo.newBuilder()
                .setStackDepth(s.getStackDepth());
        if (s.getStackFrame() != null) {
            builder.setStackFrame(s.getStackFrame());
        }
        return builder.build();
    }

}
