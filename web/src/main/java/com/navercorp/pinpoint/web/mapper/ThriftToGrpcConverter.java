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
package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.grpc.trace.PActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class ThriftToGrpcConverter {

    public static PCmdActiveThreadDumpRes convert(TCmdActiveThreadDumpRes s) {
        if (s == null) {
            return null;
        }

        final PCmdActiveThreadDumpRes.Builder builder = PCmdActiveThreadDumpRes.newBuilder();

        if (s.getSubType() != null) {
            builder.setSubType(s.getSubType());
        }
        if (s.getType() != null) {
            builder.setType(s.getType());
        }
        if (s.getVersion() != null) {
            builder.setVersion(s.getVersion());
        }
        if (s.getThreadDumps() != null) {
            builder.addAllThreadDump(mapList(s.getThreadDumps(), ThriftToGrpcConverter::convert));
        }
        return builder.build();
    }

    private static PActiveThreadDump convert(TActiveThreadDump s) {
        if (s == null) {
            return null;
        }
        final PActiveThreadDump.Builder builder = PActiveThreadDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setSampled(s.isSampled());
        if (s.getTransactionId() != null) {
            builder.setTransactionId(s.getTransactionId());
        }
        if (s.getEntryPoint() != null) {
            builder.setEntryPoint(s.getEntryPoint());
        }
        if (s.getThreadDump() != null) {
            builder.setThreadDump(convert(s.getThreadDump()));
        }
        return builder.build();
    }

    private static PThreadDump convert(TThreadDump s) {
        if (s == null) {
            return null;
        }
        final PThreadDump.Builder builder = PThreadDump.newBuilder()
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
            builder.addAllStackTrace(s.getStackTrace());
        }
        if (s.getLockedMonitors() != null) {
            builder.addAllLockedMonitor(mapList(s.getLockedMonitors(), ThriftToGrpcConverter::convert));
        }
        if (s.getLockedSynchronizers() != null) {
            builder.addAllLockedSynchronizer(s.getLockedSynchronizers());
        }
        return builder.build();
    }

    private static PMonitorInfo convert(TMonitorInfo s) {
        if (s == null) {
            return null;
        }
        final PMonitorInfo.Builder builder = PMonitorInfo.newBuilder()
                .setStackDepth(s.getStackDepth());
        if (s.getStackFrame() != null) {
            builder.setStackFrame(s.getStackFrame());
        }
        return builder.build();
    }

    public static PCmdActiveThreadLightDumpRes convert(TCmdActiveThreadLightDumpRes s) {
        if (s == null) {
            return null;
        }
        final PCmdActiveThreadLightDumpRes.Builder builder = PCmdActiveThreadLightDumpRes.newBuilder();
        if (s.getSubType() != null) {
            builder.setSubType(s.getSubType());
        }
        if (s.getType() != null) {
            builder.setType(s.getType());
        }
        if (s.getVersion() != null) {
            builder.setVersion(s.getVersion());
        }
        if (s.getThreadDumps() != null) {
            builder.addAllThreadDump(mapList(s.getThreadDumps(), ThriftToGrpcConverter::convert));
        }
        return builder.build();
    }

    private static PActiveThreadLightDump convert(TActiveThreadLightDump s) {
        if (s == null) {
            return null;
        }
        final PActiveThreadLightDump.Builder builder = PActiveThreadLightDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setSampled(s.isSampled());
        if (s.getTransactionId() != null) {
            builder.setTransactionId(s.getTransactionId());
        }
        if (s.getEntryPoint() != null) {
            builder.setEntryPoint(s.getEntryPoint());
        }
        if (s.getThreadDump() != null) {
            builder.setThreadDump(convert(s.getThreadDump()));
        }
        return builder.build();
    }

    private static PThreadLightDump convert(TThreadLightDump s) {
        if (s == null) {
            return null;
        }
        final PThreadLightDump.Builder builder = PThreadLightDump.newBuilder()
                .setThreadId(s.getThreadId());
        if (s.getThreadName() != null) {
            builder.setThreadName(s.getThreadName());
        }
        if (s.getThreadState() != null) {
            builder.setThreadState(PThreadState.forNumber(s.getThreadState().getValue()));
        }
        return builder.build();
    }

    private static <T, R> List<R> mapList(List<T> source, Function<T, R> mapper) {
        if (source == null) {
            return List.of();
        }
        final List<R> result = new ArrayList<>(source.size());
        for (T t : source) {
            result.add(mapper.apply(t));
        }
        return result;
    }

}
