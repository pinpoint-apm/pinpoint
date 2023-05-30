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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PMonitorInfo;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class ThriftToGrpcConverter {

    public static GeneratedMessageV3 convert(TBase<?, ?> s) {
        if (s instanceof TCmdActiveThreadDumpRes) {
            return convert((TCmdActiveThreadDumpRes) s);
        }
        if (s instanceof TCmdActiveThreadLightDumpRes) {
            return convert((TCmdActiveThreadLightDumpRes) s);
        }
        if (s instanceof TCommandEcho) {
            return convert((TCommandEcho) s);
        }
        if (s instanceof TCmdActiveThreadCountRes) {
            return convert((TCmdActiveThreadCountRes) s);
        }
        throw new RuntimeException("Failed to convert Thrift to gRPC: Unsupported");
    }

    private static PCmdActiveThreadDumpRes convert(TCmdActiveThreadDumpRes s) {
        return PCmdActiveThreadDumpRes.newBuilder()
                .setSubType(s.getSubType())
                .setType(s.getType())
                .setVersion(s.getVersion())
                .addAllThreadDump(mapList(s.getThreadDumps(), ThriftToGrpcConverter::convert))
                .build();
    }

    private static PActiveThreadDump convert(TActiveThreadDump s2) {
        final TThreadDump s3 = s2.getThreadDump();
        return PActiveThreadDump.newBuilder()
                .setStartTime(s2.getStartTime())
                .setLocalTraceId(s2.getLocalTraceId())
                .setSampled(s2.isSampled())
                .setTransactionId(s2.getTransactionId())
                .setEntryPoint(s2.getEntryPoint())
                .setThreadDump(PThreadDump.newBuilder()
                        .setThreadName(s3.getThreadName())
                        .setThreadId(s3.getThreadId())
                        .setBlockedTime(s3.getBlockedTime())
                        .setBlockedCount(s3.getBlockedCount())
                        .setWaitedTime(s3.getWaitedTime())
                        .setWaitedCount(s3.getWaitedCount())
                        .setLockName(s3.getLockName())
                        .setLockOwnerId(s3.getLockOwnerId())
                        .setLockOwnerName(s3.getLockOwnerName())
                        .setInNative(s3.isInNative())
                        .setSuspended(s3.isSuspended())
                        .setThreadState(PThreadState.forNumber(s3.getThreadState().getValue()))
                        .addAllStackTrace(nonNullList(s3.getStackTrace()))
                        .addAllLockedSynchronizer(nonNullList(s3.getLockedSynchronizers()))
                        .addAllLockedMonitor(mapList(s3.getLockedMonitors(), ThriftToGrpcConverter::convert))
                        .build())
                .build();
    }

    private static PMonitorInfo convert(TMonitorInfo s) {
        return PMonitorInfo.newBuilder()
                .setStackDepth(s.getStackDepth())
                .setStackFrame(s.getStackFrame())
                .build();
    }

    private static PCmdActiveThreadLightDumpRes convert(TCmdActiveThreadLightDumpRes s) {
        final PCmdActiveThreadLightDumpRes.Builder builder = PCmdActiveThreadLightDumpRes.newBuilder()
                .setSubType(s.getSubType())
                .setType(s.getType())
                .setVersion(s.getVersion())
                .addAllThreadDump(mapList(s.getThreadDumps(), ThriftToGrpcConverter::convert));
        return builder.build();
    }

    private static PActiveThreadLightDump convert(TActiveThreadLightDump s) {
        final TThreadLightDump s2 = s.getThreadDump();
        return PActiveThreadLightDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setSampled(s.isSampled())
                .setTransactionId(s.getTransactionId())
                .setEntryPoint(s.getEntryPoint())
                .setThreadDump(PThreadLightDump.newBuilder()
                        .setThreadName(s2.getThreadName())
                        .setThreadId(s2.getThreadId())
                        .setThreadState(PThreadState.forNumber(s2.getThreadState().getValue()))
                        .build())
                .build();
    }

    private static PCmdEchoResponse convert(TCommandEcho s) {
        return PCmdEchoResponse.newBuilder()
                .setMessage(s.getMessage())
                .build();
    }

    private static PCmdActiveThreadCountRes convert(TCmdActiveThreadCountRes s) {
        return PCmdActiveThreadCountRes.newBuilder()
                .addAllActiveThreadCount(nonNullList(s.getActiveThreadCount()))
                .setTimeStamp(s.getTimeStamp())
                .setHistogramSchemaType(s.getHistogramSchemaType())
                .build();
    }

    private static <T, R> List<R> mapList(List<T> src, Function<T, R> mapper) {
        if (src == null) {
            return List.of();
        }
        final List<R> res = new ArrayList<>(src.size());
        for (final T item: src) {
            res.add(mapper.apply(item));
        }
        return res;
    }

    private static <T> List<T> nonNullList(List<T> src) {
        return Objects.requireNonNullElse(src, List.of());
    }

}
