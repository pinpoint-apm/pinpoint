/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.thrift.event;

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
@Component
public class ThriftThreadDumpBoMapper implements ThriftBoMapper<ThreadDumpBo, TThreadDump> {
    private final ThriftMonitorInfoBoMapper monitorInfoBoMapper;

    private final ThriftThreadStateMapper threadStateMapper;

    public ThriftThreadDumpBoMapper(ThriftMonitorInfoBoMapper monitorInfoBoMapper, ThriftThreadStateMapper threadStateMapper) {
        this.monitorInfoBoMapper = Objects.requireNonNull(monitorInfoBoMapper, "monitorInfoBoMapper");
        this.threadStateMapper = Objects.requireNonNull(threadStateMapper, "threadStateMapper");
    }

    public ThreadDumpBo map(final TThreadDump threadDump) {
        final ThreadDumpBo threadDumpBo = new ThreadDumpBo();
        threadDumpBo.setThreadName(threadDump.getThreadName());
        threadDumpBo.setThreadId(threadDump.getThreadId());
        threadDumpBo.setBlockedTime(threadDump.getBlockedTime());
        threadDumpBo.setBlockedCount(threadDump.getBlockedCount());
        threadDumpBo.setWaitedTime(threadDump.getWaitedTime());
        threadDumpBo.setWaitedCount(threadDump.getWaitedCount());
        threadDumpBo.setLockName(threadDump.getLockName());
        threadDumpBo.setLockOwnerId(threadDump.getLockOwnerId());
        threadDumpBo.setLockOwnerName(threadDump.getLockOwnerName());
        threadDumpBo.setInNative(threadDump.isInNative());
        threadDumpBo.setSuspended(threadDump.isSuspended());

        final TThreadState threadState = threadDump.getThreadState();
        threadDumpBo.setThreadState(this.threadStateMapper.map(threadState));
        threadDumpBo.setStackTraceList(threadDump.getStackTrace());

        if (!CollectionUtils.isEmpty(threadDump.getLockedMonitors())) {
            final List<MonitorInfoBo> monitorInfoBoList = new ArrayList<>();
            for (TMonitorInfo monitorInfo : threadDump.getLockedMonitors()) {
                final MonitorInfoBo monitorInfoBo = this.monitorInfoBoMapper.map(monitorInfo);
                monitorInfoBoList.add(monitorInfoBo);
            }
            threadDumpBo.setLockedMonitorInfoList(monitorInfoBoList);
        }

        threadDumpBo.setLockedSynchronizerList(threadDump.getLockedSynchronizers());
        return threadDumpBo;
    }
}