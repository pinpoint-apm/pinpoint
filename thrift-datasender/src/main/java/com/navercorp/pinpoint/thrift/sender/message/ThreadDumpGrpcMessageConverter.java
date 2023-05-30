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
package com.navercorp.pinpoint.thrift.sender.message;

import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.MonitorInfoMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;

/**
 * @author youngjin.kim2
 */
public class ThreadDumpGrpcMessageConverter implements MessageConverter<Object, PThreadDump> {

    private final MessageConverter<Object, PThreadState> threadStateConverter = new ThreadStateGrpcMessageConverter();

    @Override
    public PThreadDump toMessage(Object message) {
        if (message instanceof ThreadDumpMetricSnapshot) {
            return toMessage((ThreadDumpMetricSnapshot) message);
        } else {
            return null;
        }
    }

    private PThreadDump toMessage(ThreadDumpMetricSnapshot message) {
        final PThreadDump.Builder builder = PThreadDump.newBuilder()
                .setThreadId(message.getThreadId())
                .setBlockedTime(message.getBlockedTime())
                .setBlockedCount(message.getBlockedCount())
                .setWaitedTime(message.getWaitedTime())
                .setWaitedCount(message.getWaitedCount())
                .setLockOwnerId(message.getLockOwnerId())
                .setInNative(message.isInNative())
                .setSuspended(message.isSuspended());
        if (message.getThreadName() != null) {
            builder.setThreadName(message.getThreadName());
        }
        if (message.getLockName() != null) {
            builder.setLockName(message.getLockName());
        }
        if (message.getLockOwnerName() != null) {
            builder.setLockOwnerName(message.getLockOwnerName());
        }
        if (message.getThreadState() != null) {
            builder.setThreadState(this.threadStateConverter.toMessage(message.getThreadState()));
        }
        if (message.getStackTrace() != null) {
            builder.addAllStackTrace(message.getStackTrace());
        }
        if (message.getLockedMonitors() != null) {
            for (final MonitorInfoMetricSnapshot snapshot: message.getLockedMonitors()) {
                builder.addLockedMonitor(convert(snapshot));
            }
        }
        if (message.getLockedSynchronizers() != null) {
            builder.addAllLockedSynchronizer(message.getLockedSynchronizers());
        }
        return builder.build();
    }

    private PMonitorInfo convert(MonitorInfoMetricSnapshot snapshot) {
        final PMonitorInfo.Builder builder = PMonitorInfo.newBuilder()
                .setStackDepth(snapshot.getStackDepth());
        if (snapshot.getStackFrame() != null) {
            builder.setStackFrame(snapshot.getStackFrame());
        }
        return builder.build();
    }

}
