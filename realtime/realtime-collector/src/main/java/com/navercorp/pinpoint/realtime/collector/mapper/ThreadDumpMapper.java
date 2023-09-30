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
package com.navercorp.pinpoint.realtime.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;

import static com.navercorp.pinpoint.realtime.collector.mapper.MapperUtils.mapList;

/**
 * @author youngjin.kim2
 */
class ThreadDumpMapper {

    static ThreadDumpBo from(PThreadDump s) {
        ThreadDumpBo t = new ThreadDumpBo();
        t.setThreadId(s.getThreadId());
        t.setThreadName(s.getThreadName());
        t.setThreadState(ThreadState.findByValue(s.getThreadState().getNumber()));
        t.setBlockedCount(s.getBlockedCount());
        t.setBlockedTime(s.getBlockedTime());
        t.setInNative(s.getInNative());
        t.setLockedMonitorInfoList(mapList(s.getLockedMonitorList(), ThreadDumpMapper::from));
        t.setLockedSynchronizerList(s.getLockedSynchronizerList());
        t.setLockName(s.getLockName());
        t.setLockOwnerId(s.getLockOwnerId());
        t.setLockOwnerName(s.getLockOwnerName());
        t.setWaitedTime(s.getWaitedTime());
        t.setWaitedCount(s.getWaitedCount());
        t.setSuspended(s.getSuspended());
        t.setStackTraceList(s.getStackTraceList());
        return t;
    }

    static ThreadDumpBo from(PThreadLightDump s) {
        ThreadDumpBo t = new ThreadDumpBo();
        t.setThreadId(s.getThreadId());
        t.setThreadName(s.getThreadName());
        t.setThreadState(ThreadState.findByValue(s.getThreadState().getNumber()));
        return t;
    }

    private static MonitorInfoBo from(PMonitorInfo s) {
        return new MonitorInfoBo(s.getStackDepth(), s.getStackFrame());
    }

}
