/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.mapper.grpc.event;

import com.navercorp.pinpoint.common.server.bo.event.MonitorInfoBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
@Component
public class GrpcThreadDumpBoMapper {
    @Autowired
    private GrpcMonitorInfoBoMapper monitorInfoBoMapper;

    @Autowired
    private GrpcThreadStateMapper threadStateMapper;

    public ThreadDumpBo map(final PThreadDump threadDump) {
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
        threadDumpBo.setInNative(threadDump.getInNative());
        threadDumpBo.setSuspended(threadDump.getSuspended());

        final PThreadState threadState = threadDump.getThreadState();
        threadDumpBo.setThreadState(this.threadStateMapper.map(threadState));
        threadDumpBo.setStackTraceList(threadDump.getStackTraceList());

        if (!CollectionUtils.isEmpty(threadDump.getLockedMonitorList())) {
            final List<MonitorInfoBo> monitorInfoBoList = new ArrayList<>();
            for (PMonitorInfo monitorInfo : threadDump.getLockedMonitorList()) {
                final MonitorInfoBo monitorInfoBo = this.monitorInfoBoMapper.map(monitorInfo);
                monitorInfoBoList.add(monitorInfoBo);
            }
            threadDumpBo.setLockedMonitorInfoList(monitorInfoBoList);
        }

        threadDumpBo.setLockedSynchronizerList(threadDump.getLockedSynchronizerList());
        return threadDumpBo;
    }
}