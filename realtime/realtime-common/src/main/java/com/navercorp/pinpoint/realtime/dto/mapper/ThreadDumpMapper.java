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
package com.navercorp.pinpoint.realtime.dto.mapper;

import com.navercorp.pinpoint.realtime.dto.ThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;

/**
 * @author youngjin.kim2
 */
@SuppressWarnings("DuplicatedCode")
public class ThreadDumpMapper {

    public static ThreadDump fromThrift(TThreadDump s) {
        final ThreadDump t = new ThreadDump();
        t.setThreadName(s.getThreadName());
        t.setThreadId(s.getThreadId());
        t.setBlockedTime(s.getBlockedTime());
        t.setBlockedCount(s.getBlockedCount());
        t.setWaitedTime(s.getWaitedTime());
        t.setWaitedCount(s.getWaitedCount());
        t.setLockName(s.getLockName());
        t.setLockOwnerId(s.getLockOwnerId());
        t.setLockOwnerName(s.getLockOwnerName());
        t.setInNative(s.isInNative());
        t.setSuspended(s.isSuspended());
        t.setThreadState(ThreadStateMapper.fromThrift(s.getThreadState()));
        t.setStackTrace(s.getStackTrace());
        t.setLockedMonitors(MapperUtils.mapList(s.getLockedMonitors(), MonitorInfoMapper::fromThrift));
        t.setLockedSynchronizers(s.getLockedSynchronizers());
        return t;
    }

    public static TThreadDump toThriftDetailed(ThreadDump s) {
        if (s == null) {
            return null;
        }
        final TThreadDump t = new TThreadDump();
        t.setThreadName(s.getThreadName());
        t.setThreadId(s.getThreadId());
        t.setBlockedTime(s.getBlockedTime());
        t.setBlockedCount(s.getBlockedCount());
        t.setWaitedTime(s.getWaitedTime());
        t.setWaitedCount(s.getWaitedCount());
        t.setLockName(s.getLockName());
        t.setLockOwnerId(s.getLockOwnerId());
        t.setLockOwnerName(s.getLockOwnerName());
        t.setInNative(s.isInNative());
        t.setSuspended(s.isSuspended());
        t.setThreadState(ThreadStateMapper.toThrift(s.getThreadState()));
        t.setStackTrace(s.getStackTrace());
        t.setLockedMonitors(MapperUtils.mapList(s.getLockedMonitors(), MonitorInfoMapper::toThrift));
        t.setLockedSynchronizers(s.getLockedSynchronizers());
        return t;
    }

    public static ThreadDump fromThrift(TThreadLightDump s) {
        final ThreadDump t = new ThreadDump();
        t.setThreadName(s.getThreadName());
        t.setThreadId(s.getThreadId());
        t.setThreadState(ThreadStateMapper.fromThrift(s.getThreadState()));
        return t;
    }

    public static TThreadLightDump toThriftLight(ThreadDump s) {
        if (s == null) {
            return null;
        }
        final TThreadLightDump t = new TThreadLightDump();
        t.setThreadName(s.getThreadName());
        t.setThreadId(s.getThreadId());
        t.setThreadState(ThreadStateMapper.toThrift(s.getThreadState()));
        return t;
    }

}
