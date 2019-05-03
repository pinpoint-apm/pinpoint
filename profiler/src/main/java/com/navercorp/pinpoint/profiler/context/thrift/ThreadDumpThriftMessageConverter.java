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

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.MonitorInfoMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;

/**
 * @author jaehong.kim
 */
public class ThreadDumpThriftMessageConverter implements MessageConverter<TThreadDump> {
    private final ThreadStateThriftMessageConverter threadStateMessageConverter = new ThreadStateThriftMessageConverter();

    @Override
    public TThreadDump toMessage(Object message) {
        if (message instanceof ThreadDumpMetricSnapshot) {
            final ThreadDumpMetricSnapshot threadDumpMetricSnapshot = (ThreadDumpMetricSnapshot) message;
            final TThreadDump threadDump = new TThreadDump();

            threadDump.setThreadName(threadDumpMetricSnapshot.getThreadName());
            threadDump.setThreadId(threadDumpMetricSnapshot.getThreadId());
            threadDump.setBlockedTime(threadDumpMetricSnapshot.getBlockedTime());
            threadDump.setBlockedCount(threadDumpMetricSnapshot.getBlockedCount());
            threadDump.setWaitedTime(threadDumpMetricSnapshot.getWaitedTime());
            threadDump.setWaitedCount(threadDumpMetricSnapshot.getWaitedCount());

            threadDump.setInNative(threadDumpMetricSnapshot.isInNative());
            threadDump.setSuspended(threadDumpMetricSnapshot.isSuspended());
            final TThreadState threadState = this.threadStateMessageConverter.toMessage(threadDumpMetricSnapshot.getThreadState());
            threadDump.setThreadState(threadState);

            for (String stackTrace : threadDumpMetricSnapshot.getStackTrace()) {
                threadDump.addToStackTrace(stackTrace);
            }

            for (MonitorInfoMetricSnapshot monitorInfoMetricSnapshot : threadDumpMetricSnapshot.getLockedMonitors()) {
                final TMonitorInfo tMonitorInfo = new TMonitorInfo();
                tMonitorInfo.setStackDepth(monitorInfoMetricSnapshot.getStackDepth());
                tMonitorInfo.setStackFrame(monitorInfoMetricSnapshot.getStackFrame());
                threadDump.addToLockedMonitors(tMonitorInfo);
            }

            threadDump.setLockName(threadDumpMetricSnapshot.getLockName());
            threadDump.setLockOwnerId(threadDumpMetricSnapshot.getLockOwnerId());
            threadDump.setLockOwnerName(threadDumpMetricSnapshot.getLockOwnerName());
            for (String lockedSynchronizer : threadDumpMetricSnapshot.getLockedSynchronizers()) {
                threadDump.addToLockedSynchronizers(lockedSynchronizer);
            }
            return threadDump;
        } else {
            return null;
        }
    }
}