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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.MonitorInfoMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;

/**
 * @author jaehong.kim
 */
public class GrpcThreadDumpMessageConverter implements MessageConverter<PThreadDump> {
    private final GrpcThreadStateMessageConverter threadStateMessageConverter = new GrpcThreadStateMessageConverter();

    @Override
    public PThreadDump toMessage(Object message) {
        if (message instanceof ThreadDumpMetricSnapshot) {
            final ThreadDumpMetricSnapshot threadDumpMetricSnapshot = (ThreadDumpMetricSnapshot) message;
            final PThreadDump.Builder threadDumpBuilder = PThreadDump.newBuilder();

            threadDumpBuilder.setThreadName(threadDumpMetricSnapshot.getThreadName());
            threadDumpBuilder.setThreadId(threadDumpMetricSnapshot.getThreadId());
            threadDumpBuilder.setBlockedTime(threadDumpMetricSnapshot.getBlockedTime());
            threadDumpBuilder.setBlockedCount(threadDumpMetricSnapshot.getBlockedCount());
            threadDumpBuilder.setWaitedTime(threadDumpMetricSnapshot.getWaitedTime());
            threadDumpBuilder.setWaitedCount(threadDumpMetricSnapshot.getWaitedCount());

            threadDumpBuilder.setInNative(threadDumpMetricSnapshot.isInNative());
            threadDumpBuilder.setSuspended(threadDumpMetricSnapshot.isSuspended());
            final PThreadState threadState = this.threadStateMessageConverter.toMessage(threadDumpMetricSnapshot.getThreadState());
            threadDumpBuilder.setThreadState(threadState);

            for (String stackTrace : threadDumpMetricSnapshot.getStackTrace()) {
                threadDumpBuilder.addStackTrace(stackTrace);
            }

            for (MonitorInfoMetricSnapshot monitorInfoMetricSnapshot : threadDumpMetricSnapshot.getLockedMonitors()) {
                final PMonitorInfo.Builder monitorInfoBuilder = PMonitorInfo.newBuilder();
                monitorInfoBuilder.setStackDepth(monitorInfoMetricSnapshot.getStackDepth());
                monitorInfoBuilder.setStackFrame(monitorInfoMetricSnapshot.getStackFrame());
                threadDumpBuilder.addLockedMonitor(monitorInfoBuilder.build());
            }

            if (threadDumpMetricSnapshot.getLockName() != null) {
                threadDumpBuilder.setLockName(threadDumpMetricSnapshot.getLockName());
            }
            threadDumpBuilder.setLockOwnerId(threadDumpMetricSnapshot.getLockOwnerId());
            if (threadDumpMetricSnapshot.getLockOwnerName() != null) {
                threadDumpBuilder.setLockOwnerName(threadDumpMetricSnapshot.getLockOwnerName());
            }
            for (String lockedSynchronizer : threadDumpMetricSnapshot.getLockedSynchronizers()) {
                threadDumpBuilder.addLockedSynchronizer(lockedSynchronizer);
            }
            return threadDumpBuilder.build();
        } else {
            return null;
        }
    }
}